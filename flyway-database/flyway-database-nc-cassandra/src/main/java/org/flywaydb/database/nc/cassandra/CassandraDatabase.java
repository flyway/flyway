/*-
 * ========================LICENSE_START=================================
 * flyway-database-nc-cassandra
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
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
package org.flywaydb.database.nc.cassandra;

import static org.flywaydb.core.internal.logging.PreviewFeatureWarning.NATIVE_CONNECTORS;
import static org.flywaydb.core.internal.logging.PreviewFeatureWarning.logPreviewFeature;
import static org.flywaydb.core.internal.util.DeprecationUtils.DeprecatedFeatures.CASSANDRA_URL;
import static org.flywaydb.core.internal.util.DeprecationUtils.printDeprecationNotice;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.DriverException;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

public class CassandraDatabase extends NativeConnectorsNonJdbc {
    private static final String URL_PREFIX = "cassandra:";
    private static final Set<String> SYSTEM_KEYSPACES = Set.of(
        "system", "system_auth", "system_schema", "system_distributed",
        "system_traces", "system_views", "system_virtual_schema");

    private CqlSession session;
    private String keyspace;
    private String user;

    @Override
    public boolean isOnByDefault(final Configuration configuration) {
        return true;
    }

    @Override
    protected String getDefaultSchema(final Configuration configuration) {
        final String defaultSchema = ConfigUtils.getCalculatedDefaultSchema(configuration);
        if (defaultSchema != null) {
            return defaultSchema;
        }
        return keyspace;
    }

    @Override
    public DatabaseSupport supportsUrl(final String url) {
        if (url.startsWith(URL_PREFIX) || url.startsWith("jdbc:cassandra:")) {
            return new DatabaseSupport(true, 1);
        }
        return new DatabaseSupport(false, 0);
    }
    
    @Override
    public boolean supportsTransactions() {
        return false;
    }

    @Override
    public void initialize(final ResolvedEnvironment environment, final Configuration configuration) {
        logPreviewFeature(NATIVE_CONNECTORS + " for " + getDatabaseType());
        connectionType = ConnectionType.API;

        if (environment.getUrl().startsWith("jdbc:")) {
            LOG.debug("Stripping JDBC prefix from url: " + redactUrl(environment.getUrl()));
            printDeprecationNotice(CASSANDRA_URL);
            environment.setUrl(environment.getUrl().replaceFirst("jdbc:", ""));
        }

        final URI uri = URI.create(environment.getUrl());
        user = environment.getUser();
        final String password = environment.getPassword();

        final String host = uri.getHost();
        final int port = uri.getPort() != -1 ? uri.getPort() : 9042;
        keyspace = uri.getPath() != null && uri.getPath().length() > 1 ? uri.getPath().substring(1) : null;
        final String datacenter = extractQueryParam(uri.getQuery(), "localdatacenter");

        final DriverConfigLoader configLoader = DriverConfigLoader.programmaticBuilder()
            .withDuration(DefaultDriverOption.REQUEST_TIMEOUT, Duration.ofSeconds(10))
            .build();

        final CqlSessionBuilder builder = CqlSession.builder()
            .withConfigLoader(configLoader)
            .addContactPoint(new InetSocketAddress(host, port));

        if (datacenter != null) {
            builder.withLocalDatacenter(datacenter);
        }

        if (user != null && password != null) {
            builder.withAuthCredentials(user, password);
        }

        if (keyspace != null && !keyspace.isEmpty()) {
            builder.withKeyspace(keyspace);
        }

        session = builder.build();
        isClosed = false;

        currentSchema = getDefaultSchema(configuration);
    }

    private String extractQueryParam(final String query, final String key) {
        if (query == null) {
            return null;
        }
        for (final String param : query.split("&")) {
            if (param.startsWith(key + "=")) {
                return param.substring(key.length() + 1);
            }
        }
        return null;
    }

    @Override
    public void doExecute(final NonJdbcExecutorExecutionUnit executionUnit, final boolean outputQueryResults) {
        try {
            session.execute(executionUnit.getScript());
        } catch (final Exception e) {
            throw new FlywayException(e);
        }
    }

    @Override
    public String getDatabaseType() {
        return "Cassandra";
    }

    @Override
    public MetaData getDatabaseMetaData() {
        if (this.metaData != null) {
            return metaData;
        }

        final Row row = session.execute("SELECT release_version FROM system.local").one();
        final String serverVersion = row != null ? row.getString("release_version") : "unknown";

        metaData = new MetaData(
            getDatabaseType(),
            "Cassandra",
            new DatabaseVersionImpl(serverVersion),
            serverVersion,
            getCurrentSchema(),
            connectionType);

        return metaData;
    }

    @Override
    public void createSchemaHistoryTable(final Configuration configuration) {
        final String tableName = quote(currentSchema, configuration.getTable());

        session.execute("CREATE TABLE " + tableName + " (\n"
            + "    installed_rank INT,\n"
            + "    version VARCHAR,\n"
            + "    partition VARCHAR,\n"
            + "    description VARCHAR,\n"
            + "    type VARCHAR,\n"
            + "    script VARCHAR,\n"
            + "    checksum INT,\n"
            + "    installed_by VARCHAR,\n"
            + "    installed_on TIMESTAMP,\n"
            + "    execution_time INT,\n"
            + "    success BOOLEAN,\n"
            + "    PRIMARY KEY ((partition), installed_rank))");

        session.execute("CREATE INDEX " + doQuote(configuration.getTable() + "_s_idx")
            + " ON " + tableName + " (" + doQuote("success") + ")");
    }

    @Override
    public boolean schemaHistoryTableExists(final String tableName) {
        final PreparedStatement ps = session.prepare(
            "SELECT count(*) FROM system_schema.tables WHERE keyspace_name=? AND table_name=?");
        final Row row = session.execute(ps.bind(currentSchema, tableName)).one();
        return row != null && row.getLong(0) > 0;
    }

    @Override
    public SchemaHistoryModel getSchemaHistoryModel(final String tableName) {
        final String table = quote(currentSchema, tableName);

        final String query = "SELECT " + doQuote("installed_rank")
            + ", " + doQuote("version")
            + ", " + doQuote("description")
            + ", " + doQuote("type")
            + ", " + doQuote("script")
            + ", " + doQuote("checksum")
            + ", " + doQuote("installed_on")
            + ", " + doQuote("installed_by")
            + ", " + doQuote("execution_time")
            + ", " + doQuote("success")
            + " FROM " + table
            + " WHERE partition='flyway'";

        final List<SchemaHistoryItem> items = new ArrayList<>();

        try {
            final ResultSet rs = session.execute(query);
            for (final Row row : rs) {
                final Instant installedOnInstant = row.getInstant("installed_on");
                final LocalDateTime installedOn = installedOnInstant != null
                    ? LocalDateTime.ofInstant(installedOnInstant, ZoneId.systemDefault())
                    : null;

                items.add(SchemaHistoryItem.builder()
                    .installedRank(row.getInt("installed_rank"))
                    .version(row.getString("version"))
                    .description(row.getString("description"))
                    .type(row.getString("type"))
                    .script(row.getString("script"))
                    .checksum(row.isNull("checksum") ? null : row.getInt("checksum"))
                    .installedOn(installedOn)
                    .installedBy(row.getString("installed_by"))
                    .executionTime(row.getInt("execution_time"))
                    .success(row.getBoolean("success"))
                    .build());
            }

            return new SchemaHistoryModel(items);
        } catch (final DriverException ignored) {
            return new SchemaHistoryModel();
        }
    }

    @Override
    public void appendSchemaHistoryItem(final SchemaHistoryItem item, final String tableName) {
        final String table = quote(currentSchema, tableName);

        final String insertSql = "INSERT INTO " + table
            + " (" + doQuote("installed_rank")
            + ", " + doQuote("version")
            + ", " + doQuote("partition")
            + ", " + doQuote("description")
            + ", " + doQuote("type")
            + ", " + doQuote("script")
            + ", " + doQuote("checksum")
            + ", " + doQuote("installed_by")
            + ", " + doQuote("installed_on")
            + ", " + doQuote("execution_time")
            + ", " + doQuote("success")
            + ")"
            + " VALUES (?, ?, 'flyway', ?, ?, ?, ?, ?, toTimestamp(now()), ?, ?)";

        try {
            final PreparedStatement ps = session.prepare(insertSql);
            session.execute(ps.bind(
                item.getInstalledRank(),
                item.getVersion(),
                item.getDescription(),
                item.getType(),
                item.getScript(),
                item.getChecksum(),
                item.getInstalledBy(),
                item.getExecutionTime(),
                item.isSuccess()));
        } catch (final Exception e) {
            throw new FlywayException(e);
        }
    }

    @Override
    public void updateSchemaHistoryItem(final SchemaHistoryItem item, final String tableName) {
        final String table = quote(currentSchema, tableName);

        final String updateSql = "UPDATE " + table
            + " SET " + doQuote("checksum") + "=?"
            + ", " + doQuote("description") + "=?"
            + ", " + doQuote("type") + "=?"
            + " WHERE " + doQuote("installed_rank") + "=?"
            + " AND partition='flyway'";

        try {
            final PreparedStatement ps = session.prepare(updateSql);
            session.execute(ps.bind(
                item.getChecksum(),
                item.getDescription(),
                item.getType(),
                item.getInstalledRank()));
        } catch (final Exception e) {
            throw new FlywayException(e);
        }
    }

    @Override
    public void removeFailedSchemaHistoryItems(final String tableName) {
        final String table = quote(currentSchema, tableName);

        try {
            final ResultSet rs = session.execute(
                "SELECT " + doQuote("installed_rank") + " FROM " + table
                    + " WHERE " + doQuote("success") + " = false ALLOW FILTERING");

            final PreparedStatement deletePs = session.prepare(
                "DELETE FROM " + table + " WHERE partition='flyway' AND " + doQuote("installed_rank") + " = ?");

            for (final Row row : rs) {
                session.execute(deletePs.bind(row.getInt("installed_rank")));
            }
        } catch (final Exception e) {
            throw new FlywayException(e);
        }
    }

    @Override
    public boolean isSchemaEmpty(final String schema) {
        final PreparedStatement ps = session.prepare(
            "SELECT count(*) FROM system_schema.tables WHERE keyspace_name=?");
        final Row row = session.execute(ps.bind(schema)).one();
        return row == null || row.getLong(0) == 0;
    }

    @Override
    public boolean isSchemaExists(final String schema) {
        final PreparedStatement ps = session.prepare(
            "SELECT count(*) FROM system_schema.keyspaces WHERE keyspace_name=?");
        final Row row = session.execute(ps.bind(schema)).one();
        return row != null && row.getLong(0) > 0;
    }

    @Override
    public void createSchemas(final String... schemas) {
        for (final String schema : schemas) {
            session.execute("CREATE KEYSPACE IF NOT EXISTS " + doQuote(schema)
                + " WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : '1' }");
        }
    }

    @Override
    public BiFunction<Configuration, ParsingContext, Parser> getParser() {
        return CassandraParser::new;
    }

    @Override
    public void doCleanSchema(final String schema) {
        if (SYSTEM_KEYSPACES.contains(schema)) {
            throw new FlywayException("Clean not supported for system keyspace " + doQuote(schema) + "!");
        }

        try {
            final PreparedStatement keyspaceQuery = session.prepare(
                "SELECT index_name FROM system_schema.indexes WHERE keyspace_name=?");

            for (final Row row : session.execute(keyspaceQuery.bind(schema))) {
                session.execute("DROP INDEX " + quote(schema, row.getString("index_name")));
            }

            final PreparedStatement viewQuery = session.prepare(
                "SELECT view_name FROM system_schema.views WHERE keyspace_name=?");
            for (final Row row : session.execute(viewQuery.bind(schema))) {
                session.execute("DROP MATERIALIZED VIEW " + quote(schema, row.getString("view_name")));
            }

            final PreparedStatement aggregateQuery = session.prepare(
                "SELECT aggregate_name FROM system_schema.aggregates WHERE keyspace_name=?");
            for (final Row row : session.execute(aggregateQuery.bind(schema))) {
                session.execute("DROP AGGREGATE " + quote(schema, row.getString("aggregate_name")));
            }

            final PreparedStatement functionQuery = session.prepare(
                "SELECT function_name FROM system_schema.functions WHERE keyspace_name=?");
            for (final Row row : session.execute(functionQuery.bind(schema))) {
                session.execute("DROP FUNCTION " + quote(schema, row.getString("function_name")));
            }

            final PreparedStatement tableQuery = session.prepare(
                "SELECT table_name FROM system_schema.tables WHERE keyspace_name=?");
            for (final Row row : session.execute(tableQuery.bind(schema))) {
                session.execute("DROP TABLE " + quote(schema, row.getString("table_name")));
            }

            final PreparedStatement typeQuery = session.prepare(
                "SELECT type_name FROM system_schema.types WHERE keyspace_name=?");
            for (final Row row : session.execute(typeQuery.bind(schema))) {
                session.execute("DROP TYPE " + quote(schema, row.getString("type_name")));
            }
        } catch (final FlywayException e) {
            throw e;
        } catch (final Exception e) {
            throw new FlywayException(e);
        }
    }

    @Override
    public void doDropSchema(final String schema) {
        try {
            session.execute("DROP KEYSPACE " + doQuote(schema));
        } catch (final Exception e) {
            throw new FlywayException(e);
        }
    }

    @Override
    public String getCurrentUser() {
        return user;
    }

    @Override
    public void startTransaction() {
    }

    @Override
    public void commitTransaction() {
    }

    @Override
    public void rollbackTransaction() {
    }

    @Override
    public void close() {
        if (session != null) {
            session.close();
            super.close();
        }
    }
}
