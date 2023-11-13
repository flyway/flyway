---
subtitle: SQLite
---
# SQLite
- **Verified Versions:** 3.0, 3.7
- **Maintainer:** Redgate

## Supported Versions and Support Levels
For information regarding the supported version and support levels available,
please see [Supported Databases for Flyway](https://documentation.red-gate.com/flyway/learn-more-about-flyway/system-requirements/supported-databases-for-flyway)

For information regarding the Flyway features available, please see [Flyway feature glossary](https://documentation.red-gate.com/flyway/learn-more-about-flyway/feature-glossary)


## Driver

| Item                               | Java (Xerial)                            |
|------------------------------------|------------------------------------------|
| **URL format**                     | <code>jdbc:sqlite:<i>database</i></code> |
| **Ships with Flyway Command-line** | Yes                                      |
| **Maven Central coordinates**      | `org.xerial:sqlite-jdbc`                 |
| **Supported versions**             | `3.7` and later                          |
| **Default Java class**             | `org.sqlite.JDBC`                        |

## SQL Script Syntax

- [Standard SQL syntax](Concepts/migrations#sql-based-migrations#syntax) with statement delimiter **;**
- Triggers with `BEGIN ... END;` block

### Compatibility

- DDL exported by SQLite can be used unchanged in a Flyway migration
- Any SQLite SQL script executed by Flyway, can be executed by the SQLite tools (after the placeholders have been replaced)

### Example

<pre class="prettyprint">/* Single line comment */
CREATE TABLE test_data (
  value VARCHAR(25) NOT NULL PRIMARY KEY
);

/*
Multi-line
comment
*/

-- Sql-style comment

-- Placeholder
INSERT INTO ${tableName} (name) VALUES ('Mr. T');

CREATE TRIGGER update_customer_address UPDATE OF address ON customers
  BEGIN
    UPDATE orders SET address = new.address WHERE customer_name = old.name;
  END;</pre>

## Limitations

- No concurrent migration as SQLite does not support `SELECT ... FOR UPDATE` locking
- No support for multiple schemas or changing the current schema, as SQLite doesn't support schemas
- No support for `CREATE TRANSACTION` and `COMMIT` statements within a migration, as SQLite doesn't support nested transactions
