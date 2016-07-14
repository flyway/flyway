# Flyway MongoDB Sample Project
## About
This is an sample project depending on the Flyway MongoDB schema migrator PoC. At the top level of
the repository, enter the following command to build the sample project.

```bash
user:~/.../flyway$ mvn package dependency:copy-dependencies -P-InstallableDBTest -P-CommercialDBTest -pl flyway-sample-mongodb -am
```

and then to run the sample project, use:

```bash
user:~/.../flyway$ java -cp flyway-sample-mongodb/target/flyway-sample-mongodb-0-SNAPSHOT.jar:flyway-sample-mongodb/target/dependency/* org.flywaydb.sample.mongodb.Main
```