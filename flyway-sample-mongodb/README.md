# Flyway MongoDB Sample Project
## About
This is a sample project depending on the Flyway MongoDB schema migrator PoC. At the top level of
the repository, enter the following command to build and run the sample project.

```bash
user:~/.../flyway$ mvn exec:java -Dexec.mainClass=org.flywaydb.sample.mongodb.Main -P-InstallableDBTest -P-CommercialDBTest -pl flyway-sample-mongodb
```

## JavaScript migrations
Mongo migrations defined in JavaScript file are now supported.
[Database run command](https://docs.mongodb.com/v3.2/reference/method/db.runCommand/) can be used to
specify the changes. MongoFlyway is configured through MongoClientURI which must contain a
database name. This is where metadata collection will be created. Available migrations will be applied
on the database specified through Mongo URI, unless database is changed by using the `use(<dbName>)` command 
in the JS file. Single line `//` and multi-line `/*..*/` comments can be added as well.
