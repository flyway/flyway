/*-
 * ========================LICENSE_START=================================
 * flyway-database-nc-couchbase
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
package org.flywaydb.database.nc.couchbase;

import static com.couchbase.client.java.query.QueryScanConsistency.REQUEST_PLUS;
import static org.flywaydb.core.internal.logging.PreviewFeatureWarning.logPreviewFeature;

import com.couchbase.client.core.env.SecurityConfig;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.query.QueryOptions;
import com.couchbase.client.java.query.QueryResult;
import com.couchbase.client.java.transactions.TransactionQueryOptions;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.flywaydb.core.internal.configuration.models.ResolvedEnvironment;
import org.flywaydb.core.internal.nc.ConnectionType;
import org.flywaydb.core.internal.nc.DatabaseSupport;
import org.flywaydb.core.internal.nc.DatabaseVersionImpl;
import org.flywaydb.core.internal.nc.MetaData;
import org.flywaydb.core.internal.nc.schemahistory.SchemaHistoryItem;
import org.flywaydb.core.internal.nc.schemahistory.SchemaHistoryModel;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.nc.NativeConnectorsNonJdbc;
import org.flywaydb.nc.executors.NonJdbcExecutorExecutionUnit;

public class CouchbaseDatabase extends NativeConnectorsNonJdbc {
    private static final String URL_PREFIX = "couchbases:";
    private static final String DEFAULT_SCOPE = "_default";
    
    private Cluster cluster;
    private Bucket bucket;
    private Scope scope;
    private String currentFQCNPrefix;

    @Override
    public boolean isOnByDefault(final Configuration configuration) {
        return true;
    }

    @Override
    protected String getDefaultSchema(final Configuration configuration) {
        final String defaultSchema = ConfigUtils.getCalculatedDefaultSchema(configuration);

        if (defaultSchema == null) {
            throw new FlywayException("Couchbase connection failed: schema configuration is missing");
        }

        return defaultSchema;
    }

    @Override
    public DatabaseSupport supportsUrl(final String url) {
        if (url.startsWith(URL_PREFIX)) {
            return new DatabaseSupport(true, 1);
        }
        return new DatabaseSupport(false, 0);
    }

    @Override
    public List<String> supportedVerbs() {
        return List.of("info", "migrate", "clean", "undo", "baseline", "validate", "repair");
    }

    @Override
    public boolean supportsTransactions() {
        return true;
    }

    @Override
    public void initialize(final ResolvedEnvironment environment, final Configuration configuration) {
        logPreviewFeature(getDatabaseType() + " Support");

        initializeConnectionType(configuration);

        ClusterEnvironment env = ClusterEnvironment.builder()
            .securityConfig(SecurityConfig.enableTls(true))
            .build();

        cluster = Cluster.connect(environment.getUrl(),
            ClusterOptions.clusterOptions(environment.getUser(), environment.getPassword()).environment(env));

        initializeBucketAndScope(getDefaultSchema(configuration));
    }

    private void initializeBucketAndScope(String defaultSchema) {
        currentSchema = defaultSchema;

        bucket = cluster.bucket(getBucketFromSchema(defaultSchema));
        scope = bucket.scope(getScopeFromSchema(defaultSchema));
        currentFQCNPrefix = "`" + bucket.name() + "`.`" + scope.name() + "`";
    }

    private void initializeConnectionType(final Configuration configuration) {
        connectionType = ConnectionType.API;
    }

    @Override
    public void doExecute(final NonJdbcExecutorExecutionUnit executionUnit, final boolean outputQueryResults) {
        try {
            scope.query(executionUnit.getScript(), QueryOptions.queryOptions().scanConsistency(REQUEST_PLUS));

            // Wait for metadata change to propagate before executing the next statement
            // REQUEST_PLUS only applies to data queries. It doesn't help for scenarios like an INSERT follows a CREATE COLLECTION
            Thread.sleep(300);
        } catch (Exception e) {
            throw new FlywayException(e);
        }
    }

    @Override
    public String getDatabaseType() {
        return "Couchbase";
    }


    @Override
    public MetaData getDatabaseMetaData() {
        if (this.metaData != null) {
            return metaData;
        }

        QueryResult result = cluster.query("SELECT VERSION() AS version;");
        String serverVersion = result.rowsAsObject().get(0).getString("version");

        metaData =  new MetaData(
            getDatabaseType(),
            "Couchbase",
            new DatabaseVersionImpl(serverVersion),
            serverVersion,
            getCurrentSchema(),
            connectionType
        );

        return metaData;
    }

    @Override
    public void createSchemaHistoryTable(final Configuration configuration) {
        try {
            String fqcn = String.format("%s.%s", currentFQCNPrefix, configuration.getTable());
            scope.query("CREATE COLLECTION " + fqcn);

            // Allow time for collection to become visible
            Thread.sleep(300);
            
            scope.query("CREATE PRIMARY INDEX ON " + fqcn);
        } catch (Exception e) {
            throw new FlywayException(e);
        }
    }

    @Override
    public boolean schemaHistoryTableExists(final String tableName) {
        String sql = "SELECT * FROM system:keyspaces  WHERE `bucket` = $bucket AND `scope` = $scope AND `name` = $collection";

        QueryResult result = cluster.query(sql, QueryOptions.queryOptions().scanConsistency(REQUEST_PLUS).parameters(JsonObject.create()
                .put("bucket", bucket.name())
                .put("scope", scope.name())
                .put("collection", tableName)));

        return !result.rowsAsObject().isEmpty();
    }

    @Override
    public SchemaHistoryModel getSchemaHistoryModel(final String tableName) {
        String fqcn = String.format("%s.%s", currentFQCNPrefix, tableName);

        String query = "SELECT installed_rank, version, description, type, script, checksum, " +
            "installed_on, installed_by, execution_time, success FROM " + fqcn;

        List<SchemaHistoryItem> items = new ArrayList<>();

        try {
            QueryResult result = cluster.query(query, QueryOptions.queryOptions().scanConsistency(REQUEST_PLUS));

            for (JsonObject row : result.rowsAsObject()) {
                items.add(SchemaHistoryItem.builder()
                    .installedRank(row.getInt("installed_rank"))
                    .version(row.getString("version"))
                    .description(row.getString("description"))
                    .type(row.getString("type"))
                    .script(row.getString("script"))
                    .checksum(row.getInt("checksum"))
                    .installedOn(fromTimestampString(row.getString("installed_on")))
                    .installedBy(row.getString("installed_by"))
                    .executionTime(row.getInt("execution_time"))
                    .success(row.getBoolean("success"))
                    .build());
            }

            return new SchemaHistoryModel(items);
        } catch (Exception ignored) {
            return new SchemaHistoryModel();
        }
    }

    private LocalDateTime fromTimestampString(final String timestamp) {
        String pattern = "yyyy-MM-dd HH:mm:ss.";
        pattern = pattern + "S".repeat(timestamp.length() - pattern.length());
        return LocalDateTime.parse(timestamp, DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH));
    }

    @Override
    public void appendSchemaHistoryItem(final SchemaHistoryItem item, final String tableName) {
        String fqcn = String.format("%s.%s", currentFQCNPrefix, tableName);

        JsonObject params = JsonObject.create()
            .put("installed_rank", item.getInstalledRank())
            .put("version", item.getVersion())
            .put("description", item.getDescription())
            .put("type", item.getType())
            .put("script", item.getScript())
            .put("checksum", item.getChecksum())
            .put("installed_on", Timestamp.from(Instant.now()).toString())
            .put("installed_by", item.getInstalledBy())
            .put("execution_time", item.getExecutionTime())
            .put("success", item.isSuccess());

        String query = "INSERT INTO " + fqcn + " (KEY, VALUE) VALUES (UUID(), " + params.toString() + ");";

        try {
            if (batch.isEmpty()) {
                scope.query(query, QueryOptions.queryOptions().scanConsistency(REQUEST_PLUS));
            } else {
                batch.add(new NonJdbcExecutorExecutionUnit(query, ""));
            }

        } catch (Exception e) {
            throw new FlywayException(e);
        }
    }

    @Override
    public boolean isSchemaEmpty(final String schema) {
        String bucket = getBucketFromSchema(schema);
        String scope = getScopeFromSchema(schema);

        String sql = "SELECT * FROM system:keyspaces  WHERE `bucket` = $bucket AND `scope` = $scope";

        QueryResult result = cluster.query(sql, QueryOptions.queryOptions().scanConsistency(REQUEST_PLUS).parameters(JsonObject.create()
                .put("bucket", bucket)
                .put("scope", scope)));

        return result.rowsAsObject().isEmpty();
    }

    @Override
    public boolean isSchemaExists(final String schema) {
        String bucket = getBucketFromSchema(schema);
        String scope = getScopeFromSchema(schema);

        String sql = "SELECT * FROM system:scopes  WHERE `bucket` = $bucket";

        QueryResult result = cluster.query(sql, QueryOptions.queryOptions().scanConsistency(REQUEST_PLUS).parameters(JsonObject.create()
            .put("bucket", bucket)));

        if (result.rowsAsObject().isEmpty()) {
            return false;
        } else {
            if (DEFAULT_SCOPE.equals(scope)) {
                return true;
            } else {
                return result.rowsAsObject().stream()
                    .anyMatch(row -> scope.equals(row.getObject("scopes").getString("name")));
            }
        }
    }

    @Override
    public void createSchemas(final String... schemas) {
        for (String schema : schemas) {
            String bucket = getBucketFromSchema(schema);
            String scope = getScopeFromSchema(schema);

            try {
                if (!isSchemaExists(bucket)) {
                    throw new FlywayException("Bucket creation is currently not supported");
                }

                if (!DEFAULT_SCOPE.equals(scope)) {
                    String fullScope = String.format("`%s`.`%s`", bucket, scope);
                    cluster.query("CREATE SCOPE " + fullScope, QueryOptions.queryOptions().scanConsistency(REQUEST_PLUS));
                }

            } catch (Exception e) {
                throw new FlywayException(e);
            }
        }
    }

    @Override
    public BiFunction<Configuration, ParsingContext, Parser> getParser() {
        return null;
    }

    @Override
    public void doExecuteBatch() {
        if (batch.isEmpty()) {
            return;
        }

        try {
            cluster.transactions().run(ctx -> {
                for (NonJdbcExecutorExecutionUnit executionUnit : batch) {
                    ctx.query(scope,
                        executionUnit.getScript(),
                        TransactionQueryOptions.queryOptions().scanConsistency(REQUEST_PLUS));
                }
            });
        } catch (Exception e) {
            throw new FlywayException(e);
        } finally {
            batch.clear();
        }
    }

    @Override
    public boolean transactionAsBatch() {
        return true;
    }

    @Override
    public String getCurrentUser() {
        return null;
    }

    @Override
    public void startTransaction() {
        batch.clear();
    }

    @Override
    public void commitTransaction() {
        batch.clear();
    }

    @Override
    public void rollbackTransaction() {
        batch.clear();
    }

    @Override
    public void doCleanSchema(final String schema) {
        String bucket = getBucketFromSchema(schema);
        String scope = getScopeFromSchema(schema);

        try {
            String sql = "SELECT * FROM system:keyspaces  WHERE `bucket` = $bucket AND `scope` = $scope";

            QueryResult result = cluster.query(sql, QueryOptions.queryOptions().scanConsistency(REQUEST_PLUS).parameters(JsonObject.create()
                .put("bucket", bucket)
                .put("scope", scope)));

            for (JsonObject row : result.rowsAsObject()) {
                String collectionName =  row.getObject("keyspaces").getString("name");

                String fqcn = String.format("`%s`.`%s`.%s", bucket, scope, collectionName);

                cluster.query("DROP COLLECTION " + fqcn);
            }
        } catch (Exception e) {
            throw new FlywayException(e);
        }
    }

    @Override
    public void doDropSchema(final String schema) {
        String bucket = getBucketFromSchema(schema);
        String scope = getScopeFromSchema(schema);

        try {
            if (!DEFAULT_SCOPE.equals(scope)) {
                String fullScope = String.format("`%s`.`%s`", bucket, scope);
                cluster.query("DROP SCOPE " + fullScope, QueryOptions.queryOptions().scanConsistency(REQUEST_PLUS));
            }
        } catch (Exception e) {
            throw new FlywayException(e);
        }
    }

    @Override
    public void removeFailedSchemaHistoryItems(final String tableName) {
        try {
            String fqcn = String.format("%s.%s", currentFQCNPrefix, tableName);
            scope.query("DELETE FROM " + fqcn + " WHERE success = false", QueryOptions.queryOptions().scanConsistency(REQUEST_PLUS));
        } catch (Exception e) {
            throw new FlywayException(e);
        }
    }

    @Override
    public void updateSchemaHistoryItem(final SchemaHistoryItem item, final String tableName) {
        try {
            String fqcn = String.format("%s.%s", currentFQCNPrefix, tableName);

            String sql = "UPDATE " + fqcn
                + " SET checksum = $checksum, description = $description, type = $type WHERE installed_rank = $installed_rank LIMIT 1";

            JsonObject params = JsonObject.create()
                .put("installed_rank", item.getInstalledRank())
                .put("checksum", item.getChecksum())
                .put("description", item.getDescription())
                .put("type", item.getType());

            scope.query(sql, QueryOptions.queryOptions().scanConsistency(REQUEST_PLUS).parameters(params));

        } catch (Exception e) {
            throw new FlywayException(e);
        }
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public void close() throws Exception {
        if (cluster != null) {
            cluster.close();
        }
    }

    private String getBucketFromSchema(String schema) {
        int index = schema.indexOf(".");
        if (index != -1) {
            return schema.substring(0, index);
        } else {
            return schema;
        }
    }

    private String getScopeFromSchema(String schema) {
        int index = schema.indexOf(".");
        if (index != -1) {
            return schema.substring(index+1);
        } else {
            return DEFAULT_SCOPE;
        }
    }

}
