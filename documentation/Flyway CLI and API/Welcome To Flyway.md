---
title: Welcome To Flyway
---

<h1 class="text--center">
    <span class="icon--flyway color--red icon--6x display--block spaced-v"></span>
    <span class="text--redgate text--bigger">Flyway</span>
</h1>

<p>Flyway is an open-source database migration tool. It strongly favors simplicity and convention over
    configuration.</p>

<p>It is based around just 7 basic commands:
    <a href="Commands/migrate">Migrate</a>,
    <a href="Commands/clean">Clean</a>,
    <a href="Commands/info">Info</a>,
    <a href="Commands/validate">Validate</a>,
    <a href="Commands/undo">Undo</a>,
    <a href="Commands/baseline">Baseline</a> and
    <a href="Commands/repair">Repair</a>.
</p>

<p>Migrations can be written in <a href="Concepts/migrations#sql-based-migrations">SQL</a>
    (database-specific syntax (such as PL/SQL, T-SQL, ...) is supported)
    or <a href="Concepts/migrations#java-based-migrations">Java</a>
    (for advanced data transformations or dealing with LOBs).</p>

<p>It has a <a href="Usage/Command Line">Command-line client</a>.
    If you are on the JVM, we recommend using the <a href="Uusage/api">Java API</a>
    for migrating the database on application startup.
    Alternatively, you can also use the <a href="Usage/Maven Goal">Maven plugin</a>
    or <a href="Usage/Gradle Task">Gradle plugin</a>.</p>

<p>And if that's not enough, there are <a href="Usage/Community%20Plugins%20and%20Integrations">plugins</a>
    available for Spring Boot, Dropwizard, Grails, Play, SBT, Ant, Griffon, Grunt, Ninja and more!</p>

<p>Supported databases are
    <a href="Supported Databases/oracle">Oracle</a>,
    <a href="Supported Databases/SQL Server">SQL Server (including Amazon RDS and Azure SQL Database)</a>,
    <a href="Supported Databases/Azure Synapse">Azure Synapse (Formerly Data Warehouse)</a>,
    <a href="Supported Databases/db2">DB2</a>,
    <a href="Supported Databases/mysql">MySQL</a> (including Amazon RDS, Azure Database &amp; Google Cloud SQL),
    <a href="Supported Databases/aurora-mysql">Aurora MySQL</a>,
    <a href="Supported Databases/mariadb">MariaDB</a>,
    <a href="Supported Databases/Percona XtraDB Cluster">Percona XtraDB Cluster</a>,
    <a href="Supported Databases/testcontainers">Testcontainers</a>,
    <a href="Supported Databases/postgresql">PostgreSQL</a> (including Amazon RDS, Azure Database, Google Cloud SQL, TimescaleDB, YugabyteDB &amp; Heroku),
    <a href="Supported Databases/aurora-postgresql">Aurora PostgreSQL</a>,
    <a href="Supported Databases/redshift">Redshift</a>,
    <a href="Supported Databases/cockroachdb">CockroachDB</a>,
    <a href="Supported Databases/SAP HANA (Including SAP HANA Cloud)">SAP HANA</a>,
    <a href="Supported Databases/sybasease">Sybase ASE</a>,
    <a href="Supported Databases/informix">Informix</a>,
    <a href="Supported Databases/h2">H2</a>,
    <a href="Supported Databases/hsqldb">HSQLDB</a>,
    <a href="Supported Databases/derby">Derby</a>,
    <a href="Supported Databases/snowflake">Snowflake</a>,
    <a href="Supported Databases/sqlite">SQLite</a> and
    <a href="Supported Databases/firebird">Firebird</a>.</p>

## Tips
* If you haven't checked out the [Getting Started](Getting Started) section yet, do it now. You'll be up
    and running in no time!
* If you are looking for a GUI experience with more comprehensive authoring tools then take a look at <a href="https://documentation.red-gate.com/fd/flyway-desktop-138346953.html">Flyway Desktop</a> 
