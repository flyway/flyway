---
subtitle: Redgate SQLFluff Rules Library
---
{% include enterprise.html %}

## Accessing Redgate SQLFluff Rules
Flyway CLI ships with a packaged version of SQLFluff that includes additional rules written by Redgate.

If you are using the Redgate bundled version of SQLFluff then you will see something like this in the CLI output
```
SqlFluff version 3.4.2 (Redgate Bundle)
```
These rules are under development so expect the list to grow.

You should be aware that [SQLFluff already comes with a variety of rules](https://docs.sqlfluff.com/en/stable/rules.html) 

## Using the `redgate` Group

All Redgate-provided rules belong to the `redgate` group.

You can enable the `redgate` rule group and selectively disable specific rules you don’t want by using a configuration like the example below.

However, please keep in mind that new rules may be added to this group in future releases. As a result, including the entire group and upgrading to a newer version could unintentionally introduce changes that affect your existing setup.

```ini
[sqlfluff]
rules = redgate
exclude_rules = RG04,RG05
```

Also note that the configuration below will **not** work as expected. SQLFluff will exclude the entire `redgate` rule group, rather than keeping only the specific rules you want.

```ini
[sqlfluff]
rules = RG01,RG02
exclude_rules = redgate
```

---
{% include anchor.html link="RG01"%}
# Rule: RG01 Drop Table
## Avoid using DROP TABLE statements.
This will cause data loss unless handled carefully. This rule will identify instances of this so you can verify that it is desired behavior in this case.
### Groups: `all`, `redgate`
### Dialects supported: `all`

---
{% include anchor.html link="RG02"%}
# Rule: RG02 Table column count
## Ensure created tables do not exceed a maximum column count
Please note that this can only be checked when a table is created, if a table is altered to increase the number then this can't be verified as it requires knowledge of the existing table structure in the database.
### Groups: `all`, `redgate`
### Dialects supported: `all`
### Configuration
`max_columns`: the maximum permissible columns in a table. Defaults to 20 columns

---
{% include anchor.html link="RG03"%}
# Rule RG03 DROP DATABASE statements
## Avoid using DROP DATABASE statements.
This will cause data loss unless handled carefully. This rule will identify instances of this so you can verify that it is desired behavior in this case.
### Groups: `all`, `redgate`
### Dialects supported: `all`

---
{% include anchor.html link="RG04"%}
# Rule RG04 Multi Part naming
## Ensure Multi Part naming convention is followed
The complete name of any schema-based database object consists of up to four identifiers: the server name, database name, schema name, and object name. Within a database, you only need the object name itself so long as it is in the same schema but by specifying the schema, the database engine needs less searching to identify it.
### Groups: `all`, `redgate`
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
{% include anchor.html link="RG05"%}
# Rule RG05 Indexes in a separate script
## Ensure Indexes are created in a separate script
Creation of an index is likely to be a time and resource intensive activity so you may not want to do this when the database is already busy with the daily activity of running your business. This rule catches cases where index creation is included as part of other updates so you know to refactor the script to manage when the index creation occurs.
### Groups: `all`, `redgate`
### Dialects supported: `all`

---
{% include anchor.html link="RG06"%}
# Rule: RG06 Delete without Where
## Check for Delete without Where clause
A DELETE without a WHERE clause will delete every row in the table, you might be clearing things out for some reason but it could also be an accident waiting to happen.
### Groups: `all`, `redgate`
### Dialects supported: `all`
### Pattern
`DELETE FROM tools WHERE type = "ballpein ;`
### Anti-pattern
`DELETE FROM tools;`

---
{% include anchor.html link="RG07"%}
# Rule: RG07 GRANT statements
## Unintended or unauthorized GRANTs
These could be a privilege escalation path that should be closely monitored and managed.
### Groups: `all`, `redgate`
### Dialects supported: `all`

---
{% include anchor.html link="RG08"%}
# Rule RG08 ALTER USER statements
## Check there is no ALTER USER statements 
These could be a privilege escalation path that should be closely monitored and managed.
### Groups: `all`, `redgate`
### Dialects supported: `postgres`, `snowflake`

---
{% include anchor.html link="RG09"%}
# Rule: RG09 Update without Where
## Check for Update without Where clause
An UPDATE without a WHERE clause will modify every row in the table. It often leads to unintended data changes.
### Groups: `all`, `redgate`
### Dialects supported: `all`
### Pattern
`UPDATE customers SET status = 'inactive' WHERE status = 'pending';`
### Anti-pattern
`UPDATE customers SET status = 'inactive';`

---
{% include anchor.html link="RG10"%}
# Rule: RG10 Modify Data Type
## Check for data type modifications in ALTER TABLE statements
A data type modification in an existing table may lead to unintended changes or the loss of data.
### Groups: `all`, `redgate`
### Dialects supported: `all`
### Pattern
`ALTER TABLE employees ADD hire_date DATE;`
`ALTER TABLE orders DROP COLUMN old_status;`
### Anti-Pattern
`ALTER TABLE customers MODIFY COLUMN last_name VARCHAR(50);`

---
{% include anchor.html link="RG11"%}
# Rule: RG11 CREATE ROLE statements
## Check for CREATE ROLE statements
A CREATE ROLE statement may require review to ensure proper access restrictions.
### Groups: `all`, `redgate`
### Dialects supported: `all`

---
{% include anchor.html link="RG12"%}
# Rule: RG12 ALTER PUBLIC ROLE
## Check for modifications to the PUBLIC role
Altering privileges for PUBLIC is discouraged, consider using explicit roles.
### Groups: `all`, `redgate`
### Dialects supported: `all`

---
{% include anchor.html link="RG13"%}
# Rule: RG13 TRUNCATE Statement
## Check for TRUNCATE statements
TRUNCATE statements will remove all rows of data in the table
### Groups: `all`, `redgate`
### Dialects supported: `all`

---
{% include anchor.html link="RG14"%}
# Rule: RG14 Drop Column
## Check for DROP COLUMN statements
This will cause data loss unless handled carefully. This rule will identify instances of this so you can verify that it is desired behavior in this case.
### Groups: `all`, `redgate`
### Dialects supported: `all`
