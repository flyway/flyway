---
layout: documentation
menu: tut_erroroverrides
subtitle: 'Tutorial: Error Overrides'
redirect_from:
- /getStarted/erroroverrides/
- /documentation/getstarted/errorhandlers/
- /documentation/getstarted/advanced/erroroverrides/
---
# Tutorial: Error Overrides
{% include teams.html %}

This tutorial assumes you have successfully completed the [**First Steps: Maven**](/documentation/getstarted/firststeps/maven)
tutorial. **If you have not done so, please do so first.** This tutorial picks up where that one left off.

This brief tutorial will teach **how to use Error Overrides**. It will take you through the
steps on how to configure and use them.

## Introduction

**Error Overrides** are a great fit for situations where you may want to:
- treat an error as a warning as you know your migration will handle it correctly later
- treat a warning as an error as you prefer to fail fast to be able to fix the problem sooner

## Reviewing the status

After having completed the [First Steps: Maven](/documentation/getstarted/firststeps/maven), you can now execute

<pre class="console"><span>bar&gt;</span> mvn flyway:<strong>info</strong></pre>

This should give you the following status:

<pre class="console">[INFO] Database: jdbc:h2:file:./target/foobar (H2 1.4)
[INFO]
+-----------+---------+---------------------+------+---------------------+---------+
| Category  | Version | Description         | Type | Installed On        | State   |
+-----------+---------+---------------------+------+---------------------+---------+
| Versioned | 1       | Create person table | SQL  | 2017-12-22 15:26:39 | Success |
| Versioned | 2       | Add people          | SQL  | 2017-12-22 15:28:17 | Success |
+-----------+---------+---------------------+------+---------------------+---------+</pre>

## Adding a broken migration

In this tutorial we're going to simulate the case where a broken sql statement should be ignored.

So let's start by adding a new migration called `src/main/resources/db/migration/V3__Invalid.sql`:
```sql
broken sql statement;
```

If we migrate the database using
<pre class="console"><span>bar&gt;</span> mvn flyway:<strong>migrate</strong></pre> 
 
it will fail as expected:
<pre class="console">[ERROR] Migration V3__Invalid.sql failed
[ERROR] --------------------------------
[ERROR] SQL State  : 42001
[ERROR] Error Code : 42001
[ERROR] Message    : Syntax error in SQL statement "BROKEN[*] SQL STATEMENT "; expected "BACKUP, BEGIN, {"; SQL statement:
[ERROR] broken sql statement [42001-191]
[ERROR] Location   : /bar/src/main/resources/db/migration/V3__Invalid.sql (/bar/src/main/resources/db/migration/V3__Invalid.sql)
[ERROR] Line       : 1
[ERROR] Statement  : broken sql statement</pre> 

## Configuring an Error Override

Now let's configure an Error Override that will trap invalid statements in our migrations and simply log a warning 
instead of failing with an error.

```xml
<project xmlns="...">
    ...
    <build>
        <plugins>
            <plugin>
                <groupId>org.flywaydb</groupId>
                <artifactId>flyway-maven-plugin</artifactId>
                <version>{{ site.flywayVersion }}</version>
                <configuration>
                    <url>jdbc:h2:file:./target/foobar</url>
                    <user>sa</user>
                    <errorOverrides>
                        <errorOverride>42001:42001:W</errorOverride>
                    </errorOverrides>
                </configuration>
                <dependencies>
                    <dependency>
                        <groupId>com.h2database</groupId>
                        <artifactId>h2</artifactId>
                        <version>1.4.191</version>
                    </dependency>
                </dependencies>
            </plugin>
        </plugins>
    </build>
</project>
```

Finally clean and migrate again using
<pre class="console"><span>bar&gt;</span> mvn flyway:clean flyway:migrate</pre>

And we now get:

<pre class="console">[INFO] Database: jdbc:h2:file:./target/foobar (H2 1.4)
[INFO] Successfully validated 3 migrations (execution time 00:00.007s)
[INFO] Creating Schema History table: "PUBLIC"."flyway_schema_history"
[INFO] Current version of schema "PUBLIC": << Empty Schema >>
[INFO] Migrating schema "PUBLIC" to version 1 - Create person table
[INFO] Migrating schema "PUBLIC" to version 2 - Add people
[INFO] Migrating schema "PUBLIC" to version 3 - Invalid
<strong>[WARNING] Syntax error in SQL statement (SQL state: 42001, error code: 42001)</strong>
[INFO] Successfully applied 3 migrations to schema "PUBLIC" (execution time 00:00.039s)</pre>

And as we were expecting we now had a successful execution with a warning instead of an error.

## Summary

In this brief tutorial we saw how to
- configure Flyway to use error overrides

<p class="next-steps">
    <a class="btn btn-primary" href="/documentation/concepts/erroroverrides">Read the Error Overrides documentation <i class="fa fa-arrow-right"></i></a>
</p>