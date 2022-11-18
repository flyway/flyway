---
subtitle: Authentication
---

# Authentication

In order to log in to your database, the typical approach is to set your username and password in the Flyway [configuration file](Configuration/Configuration Files). This however has some concerns:

- These properties are stored in plain text - anyone can see your credentials
- Your credentials must be supplied in every configuration file you use
- You may not have access to these credentials, and someone else needs to configure them securely

Flyway comes with additional authentication mechanisms that tackle these concerns.

## Environment Variables

By storing your username and password in the environment variables `FLYWAY_USER` and `FLYWAY_PASSWORD` respectively, they can be configured once and used across multiple Flyway configurations. They can also be set by someone who has the relevant access, so they do not end up being leaked to any unwarranted parties.

## Database Specific Authentication

### Oracle
- [Oracle Wallet](Supported Databases/oracle#oracle-wallet)

### SQL Server and Azure Synapse
- [Windows Authentication](Supported Databases/SQL Server#windows-authentication)
- [Azure Active Directory](Supported Databases/SQL Server#azure-active-directory)
- [Kerberos](Supported Databases/SQL Server#kerberos) {% include teams.html %}

### MySQL
- [MySQL Option Files](Supported Databases/mysql#option-files) {% include teams.html %}

### PostgreSQL
- [SCRAM](Supported Databases/postgresql#scram)
- [pgpass](Supported Databases/postgresql#pgpass) {% include teams.html %}

### Snowflake
- [Key-based Authentication](Supported Databases/snowflake#key-based-authentication)

## Platform Specific Authentication

### AWS IAM

Configure the JDBC URL to point to an AWS RDS instance:
`flyway.url=jdbc:mysql://<RDS_INSTANCE_HOSTNAME>:<RDS_INSTANCE_PORT>`

Set `flyway.user` to be the database user and `flyway.password` to be the IAM authentication token: `flyway.password=<AuthToken>`

You can read more about AWS IAM in Java applications [here](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/UsingWithRDS.IAMDBAuth.Connecting.Java.html).
