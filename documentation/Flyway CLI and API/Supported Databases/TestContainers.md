---
subtitle: Testcontainers
---
# Testcontainers

## Support Level

<table class="table">
    <tr>
        <th width="25%">Compatible</th>
        <td>&#10003;</td>
    </tr>
    <tr>
        <th width="25%">Certified</th>
        <td>&#10003;</td>
    </tr>
    <tr>
        <th width="25%">Guaranteed</th>
        <td>&#10003; {% include teams.html %}</td>
    </tr>
</table>

Support Level determines the degree of support available for this database ([learn more](Learn More/Database Support Levels)). 

## Driver
<table class="table">
<tr>
<th>URL format</th>
<td><code>jdbc:tc:</code> instead of <code>jdbc:</code> for your database</td>
</tr>
<tr>
<th>Ships with Flyway Command-line</th>
<td>No</td>
</tr>
<tr>
<th>Maven Central coordinates</th>
<td>Database specific JARs: <br/>
<code>org.testcontainers:cockroachdb:jar</code> <br/>
<code>org.testcontainers:db2:jar</code> <br/>
<code>org.testcontainers:mariadb:jar</code> <br/>
<code>org.testcontainers:mssqlserver:jar</code> <br/>
<code>org.testcontainers:mysql:jar</code> <br/>
<code>org.testcontainers:oracle-xe:jar</code> <br/>
<code>org.testcontainers:postgresql:jar</code> <br/>
<code>org.testcontainers:tidb:jar</code> <br/>
<code>org.testcontainers:yugabytedb:jar</code> <br/>
Dependencies: <br/>
<code>org.testcontainers:jdbc:jar</code> <br/>
<code>org.testcontainers:database-commons:jar</code> <br/>
<code>org.testcontainers:testcontainers:jar</code></td>
</tr>
<tr>
<th>Supported versions</th>
<td><code>1.17.6</code></td>
</tr>
<tr>
<th>Default Java class</th>
<td><code>org.testcontainers.jdbc.ContainerDatabaseDriver</code></td>
</tr>
</table>

- See the [Testcontainers documentation](https://www.testcontainers.org/modules/databases/jdbc/) for more information

### Compatibility

- See [Testcontainers list of supported databases](https://www.testcontainers.org/modules/databases/) to check if your chosen database is compatible

### Example URL

```
jdbc:tc:postgresql:11-alpine://localhost:5432/databasename
```

## Limitations

- If Flyway doesn't ship with a database driver for your chosen database, you will still need to provide one in order to use it with Testcontainers. For example, if you want to connect to a DB2 database with TestContainers you will still need to provide a DB2 driver whose Maven Central coordinates are <code>com.ibm.db2.jcc:11.5.0.0</code>
