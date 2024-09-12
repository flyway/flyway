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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bson.Document;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.experimental.ConnectionType;
import org.flywaydb.core.experimental.DatabaseSupport;
import org.flywaydb.core.experimental.ExperimentalDatabase;
import org.flywaydb.core.experimental.MetaData;
import org.flywaydb.core.experimental.schemahistory.SchemaHistoryItem;
import org.flywaydb.core.experimental.schemahistory.SchemaHistoryModel;
import org.flywaydb.core.internal.configuration.models.ResolvedEnvironment;

public class ExperimentalMongoDB implements ExperimentalDatabase {
    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;

    @Override
    public DatabaseSupport supportsUrl(final String url) {
        if (url.startsWith("mongodb:") || url.startsWith("mongodb+srv:")) {
            return new DatabaseSupport(true, 1);
        }
        return new DatabaseSupport(false, 0);
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
    }

    @Override
    public MetaData getDatabaseMetaData() {
        final Document buildInfo = mongoDatabase.runCommand(new Document("buildInfo", 1));
        final String version = buildInfo.getString("version");
        return new MetaData("Mongo DB", version, ConnectionType.API, getCurrentSchema());
    }

    @Override
    public void createSchemaHistoryTable(final String tableName) {
    }

    @Override
    public boolean schemaHistoryTableExists(final String tableName) {
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
                    .installedOn(Timestamp.valueOf(document.getString("installed_on")).toLocalDateTime())
                    .installedBy(document.getString("installed_by"))
                    .executionTime(document.getInteger("execution_time"))
                    .success(document.getBoolean("success"))
                    .build()
            );
        }
        return new SchemaHistoryModel(items);
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
        return false;
    }

    @Override
    public void close() throws Exception {

    }

    private boolean isSchemaEmpty(final String schema) {
        for (final Document document : mongoClient.getDatabase(schema).listCollections()) {
            if (document.getString("name").startsWith("system")) {
                continue;
            }
            return false;

        }
        return true;
    }

    private String getDefaultSchema(final Configuration configuration) {
        String defaultSchemaName = configuration.getDefaultSchema();
        final String[] schemaNames = configuration.getSchemas();
        if (defaultSchemaName == null) {
            if (schemaNames.length > 0) {
                defaultSchemaName = schemaNames[0];
            }
        }
        return defaultSchemaName;
    }
}
