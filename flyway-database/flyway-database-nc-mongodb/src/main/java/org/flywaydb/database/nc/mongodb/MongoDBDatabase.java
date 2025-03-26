/*-
 * ========================LICENSE_START=================================
 * flyway-database-nc-mongodb
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.database.nc.mongodb;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Updates;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.concurrent.TimeUnit;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.experimental.AbstractExperimentalDatabase;
import org.flywaydb.core.experimental.ConnectionType;
import org.flywaydb.core.experimental.DatabaseSupport;
import org.flywaydb.core.experimental.MetaData;
import org.flywaydb.core.experimental.schemahistory.SchemaHistoryItem;
import org.flywaydb.core.experimental.schemahistory.SchemaHistoryModel;
import org.flywaydb.core.extensibility.LicenseGuard;
import org.flywaydb.core.extensibility.TLSConnectionHelper;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.flywaydb.core.internal.configuration.models.ResolvedEnvironment;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.util.AsciiTable;
import org.flywaydb.core.internal.util.DockerUtils;
import org.flywaydb.core.internal.util.FileUtils;
import org.flywaydb.core.internal.util.FlywayDbWebsiteLinks;
import org.flywaydb.nc.executors.NonJdbcExecutorExecutionUnit;

public class MongoDBDatabase extends AbstractExperimentalDatabase <NonJdbcExecutorExecutionUnit> {
    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    private String schemaHistoryTableName = null;
    private MongoshCredential mongoshCredential = null;
    private ClientSession clientSession;
    private Boolean doesSchemaHistoryTableExist;

    @Override
    public DatabaseSupport supportsUrl(final String url) {
        if (url.startsWith("mongodb:")
            || url.startsWith("mongodb+srv:")
            || url.startsWith("jdbc:mongodb:")
            || url.startsWith("jdbc:mongodb+srv:")) {
            return new DatabaseSupport(true, 1);
        }
        return new DatabaseSupport(false, 0);
    }

    @Override
    public List<String> supportedVerbs() {
        return List.of("info", "validate", "migrate", "clean", "undo", "baseline", "repair");
    }

    @Override
    public boolean isOnByDefault(final Configuration configuration) {
        boolean isOSS = "OSS".equals(LicenseGuard.getTierAsString(configuration));
        return isOSS || checkMongoshInstalled(true);
    }

    @Override
    public boolean supportsTransactions() {
        return !mongoClient.getClusterDescription().getServerDescriptions().get(0).isStandAlone();
    }

    @Override
    public void initialize(final ResolvedEnvironment environment, final Configuration configuration) {
        if (configuration.getSqlMigrationSuffixes().length > 1) {
            throw new FlywayException("Multiple `sqlMigrationSuffixes` currently not supported for MongoDB: "
                + Arrays.toString(configuration.getSqlMigrationSuffixes()));
        }
        final String migrationSuffix = configuration.getSqlMigrationSuffixes()[0];
        if (!".js".equals(migrationSuffix) && !".json".equals(migrationSuffix)) {
            throw new FlywayException(
                "`sqlMigrationSuffixes` is not configured with an accepted MongoDB suffix ('.js' or '.json'): "
                    + migrationSuffix);
        }
        connectionType = ".json".equals(migrationSuffix) ? ConnectionType.API : ConnectionType.EXECUTABLE;

        if (environment.getUrl().startsWith("jdbc:")) {
            LOG.info("JDBC prefix stripped from url: " + redactUrl(environment.getUrl()));
            environment.setUrl(environment.getUrl().replaceFirst("jdbc:", ""));
        }

        if (connectionType == ConnectionType.EXECUTABLE) {
            checkMongoshInstalled(false);
            mongoshCredential = new MongoshCredential(environment.getUrl(),
                environment.getUser(),
                environment.getPassword());
        }

        final ConnectionString connectionString = new ConnectionString(configuration.getUrl());

        if (Boolean.TRUE.equals(connectionString.getSslEnabled())) {
            TLSConnectionHelper.get(configuration)
                .forEach(x -> x.prepareForTLSConnection(connectionString.getConnectionString(),
                    connectionType,
                    this,
                    configuration));
        } else {
            LOG.debug("SSL is not enabled in the current connection configuration");
        }

        if (connectionString.getCredential() != null) {
            mongoClient = MongoClients.create(MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .applicationName(APPLICATION_NAME)
                .build());
        } else {
            final MongoCredential credential = MongoCredential.createCredential(configuration.getUser(),
                "admin",
                configuration.getPassword().toCharArray());
            mongoClient = MongoClients.create(MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .applicationName(APPLICATION_NAME)
                .credential(credential)
                .build());
        }
        mongoDatabase = mongoClient.getDatabase(getDefaultSchema(configuration));
        currentSchema = mongoDatabase.getName();
        clientSession = mongoClient.startSession();
        schemaHistoryTableName = configuration.getTable();
        metaData = getDatabaseMetaData();
    }

    @Override
    public void doExecute(final NonJdbcExecutorExecutionUnit executionUnit, final boolean outputQueryResults) {
        switch (connectionType) {
            case API:
                try {
                    final Document result = mongoDatabase.runCommand(clientSession, BsonDocument.parse(executionUnit.getScript()));
                    if (outputQueryResults) {
                        parseResults(result);
                    }
                } catch (Exception e) {
                    throw new FlywayException(e);
                }
                return;
            case EXECUTABLE:
                doExecuteWithMongosh(executionUnit.getScript(), outputQueryResults);
                return;
            default:
                throw new FlywayException("No support for this connection type");
        }
    }

    @Override
    public MetaData getDatabaseMetaData() {
        if (this.metaData != null) {
            return metaData;
        }

        final Document buildInfo = mongoDatabase.runCommand(new Document("buildInfo", 1));
        final String version = buildInfo.getString("version");
        return new MetaData("MongoDB", version, connectionType, getCurrentSchema());
    }

    @Override
    public void createSchemaHistoryTable(Configuration configuration) {
        mongoDatabase.createCollection(configuration.getTable());
        doesSchemaHistoryTableExist = true;
    }

    @Override
    public boolean schemaHistoryTableExists(final String tableName) {
        if (doesSchemaHistoryTableExist != null) {
            return doesSchemaHistoryTableExist;
        }
        for (final Document document : mongoDatabase.listCollections()) {
            if (document.getString("name").equals(tableName)) {
                doesSchemaHistoryTableExist = true;
                return true;
            }
        }
        doesSchemaHistoryTableExist = false;
        return false;
    }

    @Override
    public SchemaHistoryModel getSchemaHistoryModel(final String tableName) {
        final MongoCollection<Document> collection = mongoDatabase.getCollection(tableName);
        final ArrayList<SchemaHistoryItem> items = new ArrayList<>();
        for (final Document document : collection.find()) {
            items.add(SchemaHistoryItem.builder()
                .installedRank(document.getInteger("installed_rank"))
                .version(document.getString("version"))
                .description(document.getString("description"))
                .type(document.getString("type"))
                .script(document.getString("script"))
                .checksum(document.getInteger("checksum"))
                .installedOn(fromTimestampString(document.getString("installed_on")))
                .installedBy(document.getString("installed_by"))
                .executionTime(document.getInteger("execution_time"))
                .success(document.getBoolean("success"))
                .build());
        }
        return new SchemaHistoryModel(items);
    }

    private LocalDateTime fromTimestampString(final String timestamp) {
        String pattern = "yyyy-MM-dd HH:mm:ss.";
        pattern = pattern + "S".repeat(timestamp.length() - pattern.length());
        return LocalDateTime.parse(timestamp, DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH));
    }

    @Override
    public void appendSchemaHistoryItem(final SchemaHistoryItem item, final String tableName) {
        final Document document = new Document().append("installed_rank", item.getInstalledRank())
            .append("version", item.getVersion())
            .append("description", item.getDescription())
            .append("type", item.getType())
            .append("script", item.getScript())
            .append("checksum", item.getChecksum())
            .append("installed_on", Timestamp.from(Instant.now()).toString())
            .append("installed_by", item.getInstalledBy())
            .append("execution_time", item.getExecutionTime())
            .append("success", item.isSuccess());
        mongoDatabase.getCollection(tableName).insertOne(clientSession, document);
    }

    @Override
    public Boolean allSchemasEmpty(final String[] schemas) {
        return Arrays.stream(schemas).filter(this::isSchemaExists).allMatch(this::isSchemaEmpty);
    }

    @Override
    public boolean isSchemaExists(final String schema) {
        for (final String names : mongoClient.listDatabaseNames()) {
            if (names.equals(schema)) {
                return true;
            }
        }

        final SchemaHistoryModel schemaHistoryModel = getSchemaHistoryModel(schemaHistoryTableName);
        return schemaHistoryModel.getSchemaHistoryItems()
            .stream()
            .filter(x -> Objects.equals(x.getType(), "SCHEMA"))
            .flatMap(x -> Arrays.stream(x.getScript().replace("\"", "").split(",")))
            .anyMatch(x -> x.equals(schema));
    }

    @Override
    public void createSchemas(final String... schemas) {

    }

    @Override
    public void close() throws Exception {

    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public boolean isSchemaEmpty(final String schema) {
        for (final Document document : mongoClient.getDatabase(schema).listCollections()) {
            if (document.getString("name").startsWith("system")) {
                continue;
            }
            return false;
        }
        return true;
    }

    @Override
    public String getDefaultSchema(final Configuration configuration) {
        final String defaultSchema = ConfigUtils.getCalculatedDefaultSchema(configuration);
        return defaultSchema == null ? "test" : defaultSchema;
    }

    @Override
    public String getDatabaseType() {
        return "MongoDB";
    }

    @Override
    public void doExecuteBatch() {
        mongoDatabase.runCommand(BsonDocument.parse(String.join(";", batch)));
        batch.clear();
    }

    @Override
    public BiFunction<Configuration, ParsingContext, Parser> getParser() {
        return null;
    }

    @Override
    public String getCurrentUser() {
        return null;
    }

    @Override
    public void startTransaction() {
        if (connectionType == ConnectionType.EXECUTABLE) {
            LOG.warn(
                "Transactions are currently not supported for '.js' Migrations. Migrations will be executed outside of a transaction. "
                    + "Set `executeInTransaction` to false to remove this warning.");
            return;
        }
        if (supportsTransactions()) {
            clientSession.startTransaction();
        } else {
            LOG.warn(
                "Transactions are not supported on a standalone MongoDB database. Migrations will be executed outside of a transaction. "
                    + "Set `executeInTransaction` to false to remove this warning.");
        }
    }

    @Override
    public void commitTransaction() {
        if (supportsTransactions()) {
            clientSession.commitTransaction();
        }
    }

    @Override
    public void rollbackTransaction() {
        if (supportsTransactions()) {
            clientSession.abortTransaction();
        }
    }

    @Override
    public void doCleanSchema(final String schema) {
        final MongoDatabase schemaDatabase = mongoClient.getDatabase(schema);
        schemaDatabase.listCollections().forEach(i -> {
            final String name = i.getString("name");
            if (!name.startsWith("system.")) {
                schemaDatabase.getCollection(name).drop();
            }
        });
    }

    @Override
    public void doDropSchema(final String schema) {
        final MongoDatabase schemaDatabase = mongoClient.getDatabase(schema);
        schemaDatabase.drop();
    }

    @Override
    public void removeFailedSchemaHistoryItems(final String tableName) {
        final Document document = new Document().append("success", false);
        mongoDatabase.getCollection(tableName).deleteMany(document);
    }

    @Override
    public void updateSchemaHistoryItem(final SchemaHistoryItem item, final String tableName) {
        final Document query = new Document().append("installed_rank", item.getInstalledRank());
        final Bson updates = Updates.combine(Updates.set("checksum", item.getChecksum()),
            Updates.set("description", item.getDescription()),
            Updates.set("type", item.getType()));
        mongoDatabase.getCollection(tableName).updateOne(query, updates);
    }

    private void parseResults(final Document result) {
        if (result.containsKey("cursor")) {
            final Document results = (Document) result.get("cursor");
            final Collection<Document> rows = results.getList("firstBatch", Document.class);
            final ArrayList<String> columns = new ArrayList<>(rows.stream().findFirst().get().keySet());

            final List<List<String>> processedRows = rows.stream()
                .map(x -> x.values().stream().map(Object::toString).toList())
                .toList();
            final AsciiTable queryResults = new AsciiTable(columns, processedRows, true, "", "");
            LOG.info(queryResults.render());
        }
    }

    private void doExecuteWithMongosh(final String executionUnit, final boolean outputQueryResults) {
        final List<String> commands = new ArrayList<>(List.of("mongosh", mongoshCredential.url()));
        if (mongoshCredential.username() != null) {
            commands.addAll(List.of("--username", mongoshCredential.username()));
        }
        if (mongoshCredential.password() != null) {
            commands.addAll(List.of("--password", mongoshCredential.password()));
        }
        commands.addAll(List.of("--eval", executionUnit));

        final var processBuilder = new ProcessBuilder(commands);
        /* Required to stop system-stubs throwing Exception */
        processBuilder.environment();

        try {
            LOG.debug("Executing mongosh");
            final Process process = processBuilder.start();
            final boolean exited = process.waitFor(5, TimeUnit.MINUTES);
            if (!exited) {
                throw new FlywayException("Mongosh execution timeout. Consider using smaller migrations");
            }
            final int exitCode = process.exitValue();

            if (exitCode != 0) {
                final String stdErr = FileUtils.copyToString(new InputStreamReader(process.getErrorStream(),
                    StandardCharsets.UTF_8)).strip();
                throw new FlywayException(stdErr + " (ExitCode: " + exitCode + ")");
            }
        } catch (Exception e) {
            if (e.getMessage().contains("The filename or extension is too long")) {
                throw new FlywayException("Mongosh execution failed. Consider using smaller migrations");
            }
            throw new FlywayException(e);
        }
    }

    private boolean checkMongoshInstalled(final boolean silent) {
        List<String> commands = Arrays.asList("mongosh", "--version");
        LOG.debug("Executing " + String.join(" ", commands));
        final ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.environment();
        try {
            processBuilder.start();
        } catch (final Exception e) {
            if (DockerUtils.isContainer()) {
                if (silent) {
                    return false;
                }
                throw new FlywayException(
                    "Mongosh is not installed on this docker image. Please use the Mongo docker image on our repository: "
                        + FlywayDbWebsiteLinks.OSS_DOCKER_REPOSITORY);
            }
            if (silent) {
                return false;
            }
            throw new FlywayException(
                "Mongosh is required for .js migrations and is not currently installed. Information on how to install Mongosh can be found here: "
                    + FlywayDbWebsiteLinks.MONGOSH);
        }

        return true;
    }
}
