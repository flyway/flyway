/*
 * Copyright (C) Red Gate Software Ltd 2010-2023
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
module org.flywaydb.core
{
    requires java.logging;
    requires java.sql;
    requires static commons.logging;
    requires com.google.gson;

    uses org.flywaydb.core.extensibility.Plugin;

    provides org.flywaydb.core.extensibility.Plugin with
            org.flywaydb.core.internal.database.cockroachdb.CockroachDBDatabaseType,
            org.flywaydb.core.internal.database.redshift.RedshiftDatabaseType,
            org.flywaydb.core.internal.database.db2.DB2DatabaseType,
            org.flywaydb.core.internal.database.derby.DerbyDatabaseType,
            org.flywaydb.core.internal.database.h2.H2DatabaseType,
            org.flywaydb.core.internal.database.hsqldb.HSQLDBDatabaseType,
            org.flywaydb.core.internal.database.informix.InformixDatabaseType,
            org.flywaydb.core.internal.database.postgresql.PostgreSQLDatabaseType,
            org.flywaydb.core.internal.database.saphana.SAPHANADatabaseType,
            org.flywaydb.core.internal.database.snowflake.SnowflakeDatabaseType,
            org.flywaydb.core.internal.database.sqlite.SQLiteDatabaseType,
            org.flywaydb.core.internal.database.sybasease.SybaseASEJConnectDatabaseType,
            org.flywaydb.core.internal.database.sybasease.SybaseASEJTDSDatabaseType,
            org.flywaydb.core.internal.database.base.TestContainersDatabaseType,
            org.flywaydb.core.internal.schemahistory.BaseAppliedMigration,
            org.flywaydb.core.internal.resource.CoreResourceTypeProvider,
            org.flywaydb.core.internal.database.postgresql.PostgreSQLConfigurationExtension,
            org.flywaydb.core.internal.command.clean.CleanModeConfigurationExtension,
            org.flywaydb.core.internal.configuration.resolvers.EnvironmentVariableResolver,
            org.flywaydb.core.internal.proprietaryStubs.CommandExtensionStub,
            org.flywaydb.core.api.output.InfoHtmlRenderer,
            org.flywaydb.core.internal.reports.json.InfoResultDeserializer,
            org.flywaydb.core.api.output.MigrateHtmlRenderer,
            org.flywaydb.core.internal.reports.json.MigrateResultDeserializer,
            org.flywaydb.core.api.output.HoldingRenderer,
            org.flywaydb.core.api.output.DashboardRenderer,
            org.flywaydb.core.extensibility.LicenseRgDomainChecker,
            org.flywaydb.core.api.migration.baseline.BaselineAppliedMigration,
            org.flywaydb.core.api.migration.baseline.BaselineMigrationConfigurationExtension,
            org.flywaydb.core.api.migration.baseline.BaselineMigrationResolver,
            org.flywaydb.core.api.migration.baseline.BaselineResourceTypeProvider;

    opens org.flywaydb.core.internal;
    opens org.flywaydb.core.internal.reports.json;

    exports org.flywaydb.core;
    exports org.flywaydb.core.api;
    exports org.flywaydb.core.api.configuration;
    exports org.flywaydb.core.extensibility;
}