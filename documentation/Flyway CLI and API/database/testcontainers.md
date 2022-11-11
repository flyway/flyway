---
layout: documentation
menu: testcontainers
subtitle: TestContainers
---
# TestContainers

## Support Level

<table class="table">
    <tr>
        <th width="25%">Compatible</th>
        <td>✅</td>
    </tr>
    <tr>
        <th width="25%">Certified</th>
        <td>✅</td>
    </tr>
    <tr>
        <th width="25%">Guaranteed</th>
        <td>✅ {% include teams.html %}</td>
    </tr>
</table>

Support Level determines the degree of support available for this database ([learn more](/documentation/learnmore/database-support)). 

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
<code>org.testcontainers:cockroachdb:jar:1.14.3</code> <br/>
<code>org.testcontainers:db2:jar:1.14.3</code> <br/>
<code>org.testcontainers:mariadb:jar:1.14.3</code> <br/>
<code>org.testcontainers:mssqlserver:jar:1.14.3</code> <br/>
<code>org.testcontainers:mysql:jar:1.14.3</code> <br/>
<code>org.testcontainers:oracle-xe:jar:1.14.3</code> <br/>
<code>org.testcontainers:postgresql:jar:1.14.3</code> <br/>
Dependencies: <br/>
<code>org.testcontainers:jdbc:jar:1.14.3</code> <br/>
<code>org.testcontainers:database-commons:jar:1.14.3</code> <br/>
<code>org.testcontainers:testcontainers:jar:1.14.3</code></td>
</tr>
<tr>
<th>Supported versions</th>
<td><code>1.14.3</code></td>
</tr>
<tr>
<th>Default Java class</th>
<td><code>org.testcontainers.jdbc.ContainerDatabaseDriver</code></td>
</tr>
</table>

- See the [TestContainers documentation](https://www.testcontainers.org/modules/databases/jdbc/) for more information

### Compatibility

- See [TestContainers list of supported databases](https://www.testcontainers.org/modules/databases/) to check if your chosen database is compatible

### Example URL

```
jdbc:tc:postgresql:11-alpine://localhost:5432/databasename
```

## Limitations

- If Flyway doesn't ship with a database driver for your chosen database, you will still need to provide one in order to use it with TestContainers. For example, if you want to connect to a DB2 database with TestContainers you will still need to provide a DB2 driver whose Maven Central coordinates are <code>com.ibm.db2.jcc:11.5.0.0</code>

<p class="next-steps">
    <a class="btn btn-primary" href="/documentation/database/postgresql">PostgreSQL <i class="fa fa-arrow-right"></i></a>
</p>
