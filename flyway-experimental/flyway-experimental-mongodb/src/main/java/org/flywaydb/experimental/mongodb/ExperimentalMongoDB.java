/*-
 * ========================LICENSE_START=================================
 * flyway-experimental-mongodb
 * ========================================================================
 * Copyright (C) 2010 - 2024 Red Gate Software Ltd
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
package org.flywaydb.experimental.mongodb;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.sql.SQLException;
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
import org.bson.BsonDocument;
import org.bson.Document;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.experimental.ConnectionType;
import org.flywaydb.core.experimental.DatabaseSupport;
import org.flywaydb.core.experimental.ExperimentalDatabase;
import org.flywaydb.core.experimental.MetaData;
import org.flywaydb.core.experimental.schemahistory.SchemaHistoryItem;
import org.flywaydb.core.experimental.schemahistory.SchemaHistoryModel;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.flywaydb.core.internal.configuration.models.ResolvedEnvironment;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.util.AsciiTable;

public class ExperimentalMongoDB implements ExperimentalDatabase {
    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    private ArrayList<String> batch = new ArrayList<>();
    
    private String schemaHistoryTableName = null;

    @Override
    public DatabaseSupport supportsUrl(final String url) {
        if (url.startsWith("mongodb:") || url.startsWith("mongodb+srv:")) {
            return new DatabaseSupport(true, 1);
        }
        return new DatabaseSupport(false, 0);
    }

    @Override
    public boolean supportsDdlTransactions() {
        return false;
    }

    @Override
    public void initialize(final ResolvedEnvironment environment, final Configuration configuration) throws SQLException {
        final ConnectionString connectionString = new ConnectionString(configuration.getUrl());
        if (connectionString.getCredential() != null) {
            mongoClient = MongoClients.create(environment.getUrl());
        } else {
            final MongoCredential credential = MongoCredential.createScramSha256Credential(configuration.getUser(),
                "admin",
                configuration.getPassword().toCharArray());
            mongoClient = MongoClients.create(MongoClientSettings.builder()
                .applyToClusterSettings(builder -> builder.hosts(List.of(new ServerAddress(connectionString.getHosts()
                    .get(0)))))
                .credential(credential)
                .build());
        }
        mongoDatabase = mongoClient.getDatabase(getDefaultSchema(configuration));
        schemaHistoryTableName = configuration.getTable();
    }

    @Override
    public void doExecute(final String executionUnit, final boolean outputQueryResults) {
        try {
            final Document result = mongoDatabase.runCommand(BsonDocument.parse(executionUnit));
            if (outputQueryResults) {
                parseResults(result);
            }
        } catch (Exception e) {
            throw new FlywayException(e);
        }
    }

    @Override
    public MetaData getDatabaseMetaData() {
        final Document buildInfo = mongoDatabase.runCommand(new Document("buildInfo", 1));
        final String version = buildInfo.getString("version");
        return new MetaData("Mongo DB", version, ConnectionType.API, getCurrentSchema());
    }

    @Override
    public void createSchemaHistoryTable(final String tableName) {
        mongoDatabase.createCollection(tableName);
    }

    @Override
    public boolean schemaHistoryTableExists(final String tableName) {
        for (final Document document : mongoDatabase.listCollections()) {
            if (document.getString("name").equals(tableName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public SchemaHistoryModel getSchemaHistoryModel(final String tableName) {
        final MongoCollection<Document> collection = mongoDatabase.getCollection(tableName);
        final ArrayList<SchemaHistoryItem> items = new ArrayList<>();
        for (final Document document : collection.find()) {
            items.add(
                SchemaHistoryItem
                    .builder()
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
                    .build()
            );
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
        mongoDatabase.getCollection(tableName).insertOne(document);
    }

    @Override
    public String getCurrentSchema() {
        return mongoDatabase.getName();
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
        return schemaHistoryModel.getSchemaHistoryItems().stream()
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
    public void addToBatch(final String executionUnit) {
        batch.add(executionUnit);
    }

    @Override
    public BiFunction<Configuration, ParsingContext, Parser> getParser() {
        return null;
    }

    @Override
    public String getCurrentUser() {
        return "";
    }

    @Override
    public void startTransaction() {
        throw new UnsupportedOperationException("Transactions are currently not supported in MongoDB, set executeInTransaction to false");
    }

    @Override
    public void commitTransaction() {

    }

    @Override
    public void rollbackTransaction() {

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
    public void removeFailedSchemaHistoryItems(final String tableName) {
        final Document document = new Document().append("success", false);
        mongoDatabase.getCollection(tableName).deleteMany(document);
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
}
