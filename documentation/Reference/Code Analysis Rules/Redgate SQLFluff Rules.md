---
subtitle: Redgate SQLFluff Rules
---
{% include enterprise.html %}

## Accessing Redgate SQLFluff Rules
Flyway CLI ships with a packaged version of SQLFluff that includes additional rules written by Redgate. In order to access the packaged version of SQLFluff you will need to define the following environment variable:
```bash
export FLYWAY_ENV_NATIVE_SQLFLUFF=true
```
If you are using the packaged version of SQLFluff then you will see something like this in the CLI output
```
SqlFluff version 3.4.2 (Packaged)
```
These rules are under development so expect the list to grow.

You should be aware that [SQLFluff already comes with a variety of rules](https://docs.sqlfluff.com/en/stable/rules.html) 

---

# Rule: RG01 Drop Table
## Avoid using DROP TABLE statements.
This will cause data loss unless handled carefully. This rule will identify instances of this so you can verify that it is desired behavior in this case.
### Groups: `all`
### Dialects supported: `all`

---

# Rule: RG02 Table column count
## Ensure created tables do not exceed a maximum column count
Please note that this can only be checked when a table is created, if a table is altered to increase the number then this can't be verified as it requires knowledge of the existing table structure in the database.
### Groups: `all`
### Dialects supported: `all`
### Configuration
`max_columns`: the maximum permissible columns in a table. Defaults to 20 columns

---

# Rule RG03 DROP DATABASE statements
## Avoid using DROP DATABASE statements.
This will cause data loss unless handled carefully. This rule will identify instances of this so you can verify that it is desired behavior in this case.
### Groups: `all`
### Dialects supported: `all`

---

# Rule RG04 Multi Part naming
## Ensure Multi Part naming convention is followed
The complete name of any schema-based database object consists of up to four identifiers: the server name, database name, schema name, and object name. Within a database, you only need the object name itself so long as it is in the same schema but by specifying the schema, the database engine needs less searching to identify it.
### Groups: `all`
### Dialects supported: `all`
## Pattern
```
create table myschema.mytable(col int);
select * from myschema.mytable
```
## Anti-pattern
```
create table myschema.mytable(col int);
select * from mydb.myschema.mytable
```
### Configuration
`parts_allowed`: The number of parts allowed in object naming, defaults to `1,2`

---

# Rule RG05 Indexes in a separate script
## Ensure Indexes are created in a separate script
Creation of an index is likely to be a time and resource intensive activity so you may not want to do this when the database is already busy with the daily activity of running your business. This rule catches cases where index creation is included as part of other updates so you know to refactor the script to manage when the index creation occurs.
### Groups: `all`
### Dialects supported: `all`

---

# Rule: RG06 Delete without Where
## Check for Delete without Where clause
A DELETE without a WHERE clause will delete every row in the table, you might be clearing things out for some reason but it could also be an accident waiting to happen.
### Groups: `all`
### Dialects supported: `all`
### Pattern
`DELETE FROM tools WHERE type = "ballpein ;`
### Anti-pattern
`DELETE FROM tools;`

---

# Rule: RG07 GRANT statements
## Unintended or unauthorized GRANTs
These could be a privilege escalation path that should be closely monitored and managed.
### Groups: `all`
### Dialects supported: `all`

---

# Rule RG08 ALTER USER statements
## Check there is no ALTER USER statements 
These could be a privilege escalation path that should be closely monitored and managed.
### Groups: `all`
### Dialects supported: `postgres`, `snowflake`

---

# Rule: RG09 Update without Where
## Check for Update without Where clause
An UPDATE without a WHERE clause will modify every row in the table. It often leads to unintended data changes.
### Groups: `all`
### Dialects supported: `all`
### Pattern
`UPDATE customers SET status = 'inactive' WHERE status = 'pending';`
### Anti-pattern
`UPDATE customers SET status = 'inactive';`

