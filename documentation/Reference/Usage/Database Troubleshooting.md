---
subtitle: Database troubleshooting
---

There are some common errors that can be encountered when Flyway is finding the correct database specified in your url.
These happen before Flyway is able to connect to your database and are not related to any connection issues to your database.

## Issues with the Flyway Database Modules

If Flyway is unable to find a plugin that matches the url provided, you will see an error that looks like `No Flyway database plugin found to handle: jdbc:foo://localhost:port:`.

### Is the url formatted correctly?
If the url is formatted incorrectly, Flyway will not be able to match the url to the database it is associated with. For example, the database type may be incorrectly spelt, or it matches an unsupported database. 
Check the database reference page for your database to find the correct url format

### Are you using a community database?
If you are using a community database, such as YugabyteDB, you will need to have `communityDBSupportEnabled` set to true in your configuration.

### (CLI) Is the database plugin in the CLI folder
The database plugin Flyway is trying to use may have been deleted and is no longer in the CLI folder. By default, these are stored in the `lib/flyway` folder.
If the database is not present, you can find individual modules on our Maven or with a fresh installation of the CLI.

### (API) Has the database module been added as a dependency?
Database modules need to be added as a dependency for Flyway to work. Check the database page to see how to add a database module as a dependency.

## Issues with database drivers

If Flyway is unable to find a database driver that matches the url provided, you will see an error that looks like `No JDBC driver found to handle: jdbc:foo://localhost:port`.

### (CLI) Are the drivers installed?
While most database drivers are shipped with Flyway, we are unable to ship drivers for certain databases. 
These include Google BigQuery, MongoDB and the community databases. 
Refer to the database reference page for the drivers that you will need to install. These should be added to the `drivers` folder.

### (API) Are the drivers added as a dependency?
Flyway does not include the driver dependencies as part of `flyway-core` or any of the database modules. 
The drivers will need to be added separately as a dependency of your project. Refer to the database reference page for the driver needed.