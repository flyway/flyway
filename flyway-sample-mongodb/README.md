# Flyway MongoDB Sample Project
## About
This is an sample project depending on the Flyway MongoDB schema migrator PoC. At the top level of
the repository, enter the following command to build and run the sample project.

```bash
user:~/.../flyway$ mvn exec:java -Dexec.mainClass=org.flywaydb.sample.mongodb.Main -P-InstallableDBTest -P-CommercialDBTest -pl flyway-sample-mongodb
```