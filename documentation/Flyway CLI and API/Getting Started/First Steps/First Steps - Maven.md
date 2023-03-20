---
subtitle: Flyway Maven Plugin - First Steps
redirect_from: /Getting Started/First Steps/First Steps - Maven/
---
# First Steps: Maven

This brief tutorial will teach **how to get up and running with the Flyway Maven Plugin**. It will take you through the
steps on how to configure it and how to write and execute your first few database migrations.

This tutorial should take you about **5 minutes** to complete.

## Prerequisites
- Java 8, 9, 10, 11 or 12
- Maven 3.x

## Creating the project

We're going to create our project using the Maven Archetype Plugin by issuing the following command:
<pre class="console"><span>&gt;</span> mvn archetype:generate -B ^
    -DarchetypeGroupId=org.apache.maven.archetypes ^
    -DarchetypeArtifactId=maven-archetype-quickstart ^
    -DarchetypeVersion=1.1 ^
    -DgroupId=foo ^
    -DartifactId=bar ^
    -Dversion=1.0-SNAPSHOT ^
    -Dpackage=foobar</pre>
    
We are now ready to get started. Let's jump into our project:
<pre class="console"><span>&gt;</span> cd bar</pre>

## Integrating Flyway

Let's integrate Flyway and the H2 database into our new `pom.xml` and configure Flyway so it can successfully connect to H2:
```xml
<project xmlns="...">
    <build>
        <plugins>
            <plugin>
                <groupId>org.flywaydb</groupId>
                <artifactId>flyway-maven-plugin</artifactId>
                <version>{{ site.flywayVersion }}</version>
                <configuration>
                    <url>jdbc:h2:file:./target/foobar</url>
                    <user>sa</user>
                </configuration>
                <dependencies>
                    <dependency>
                        <groupId>com.h2database</groupId>
                        <artifactId>h2</artifactId>
                        <version>1.4.197</version>
                    </dependency>
                </dependencies>
            </plugin>
        </plugins>
    </build>
</project>
```

## Creating the first migration

We create the migration directory `src/main/resources/db/migration`.
    
Followed by a first migration called `src/main/resources/db/migration/V1__Create_person_table.sql`:
```sql
create table PERSON (
    ID int not null,
    NAME varchar(100) not null
);
```

## Migrating the database

It's now time to execute Flyway to migrate our database:
<pre class="console"><span>bar&gt;</span> mvn flyway:<strong>migrate</strong></pre>

If all went well, you should see the following output:
<pre class="console">[INFO] Database: jdbc:h2:file:./target/foobar (H2 1.4)
[INFO] Successfully validated 1 migration (execution time 00:00.009s)
[INFO] Creating Schema History table: "PUBLIC"."flyway_schema_history"
[INFO] Current version of schema "PUBLIC": << Empty Schema >>
[INFO] Migrating schema "PUBLIC" to version 1 - Create person table
[INFO] Successfully applied 1 migration to schema "PUBLIC" (execution time 00:00.038s)</pre>

## Adding a second migration

If we now add a second migration called `src/main/resources/db/migration/V2__Add_people.sql`:
```sql
insert into PERSON (ID, NAME) values (1, 'Axel');
insert into PERSON (ID, NAME) values (2, 'Mr. Foo');
insert into PERSON (ID, NAME) values (3, 'Ms. Bar');
```

and execute it by issuing:
<pre class="console"><span>bar&gt;</span> mvn flyway:<strong>migrate</strong></pre>

We now get:
<pre class="console">[INFO] Database: jdbc:h2:file:./target/foobar (H2 1.4)
[INFO] Successfully validated 2 migrations (execution time 00:00.012s)
[INFO] Current version of schema "PUBLIC": 1
[INFO] Migrating schema "PUBLIC" to version 2 - Add people
[INFO] Successfully applied 1 migration to schema "PUBLIC" (execution time 00:00.018s)</pre>

## Summary

In this brief tutorial we saw how to
- integrate the Flyway Maven plugin into a project
- configure it so it can talk to our database
- write our first couple of migrations

These migrations were then successfully found and executed.
