---
layout: documentation
menu: yugabytedb
subtitle: YugabyteDB
---
# YugabyteDB

## Supported Versions

- `2.7`
- `2.6`
- `2.5`
- `2.4`

## Support Level

<table class="table">
    <tr>
        <th width="25%">Compatible</th>
        <td>✅</td>
    </tr>
    <tr>
        <th width="25%">Certified</th>
        <td>❌</td>
    </tr>
    <tr>
        <th width="25%">Guaranteed</th>
        <td>❌</td>
    </tr>
</table>

Support Level determines the degree of support available for this database ([learn more](/documentation/learnmore/database-support)). 

## Driver

<table class="table">
<tr>
<th>URL format</th>
<td><code>jdbc:postgresql://<i>host</i>:<i>port</i>/<i>database</i></code></td>
</tr>
<tr>
<th>SSL support</th>
<td>Yes - add <code>?ssl=true</code></td>
</tr>
<tr>
<th>Ships with Flyway Command-line</th>
<td>Yes</td>
</tr>
<tr>
<th>Maven Central coordinates</th>
<td><code>org.postgresql:postgresql:42.2.14</code></td>
</tr>
<tr>
<th>Supported versions</th>
<td><code>9.3-1104-jdbc4</code> and later</td>
</tr>
<tr>
<th>Default Java class</th>
<td><code>org.postgresql.Driver</code></td>
</tr>
</table>

## Notes

YugabyteDB is a variant of PostgreSQL and Flyway usage is the same for the two databases. For more details, 
please refer to the [PostgreSQL](/documentation/database/postgresql) page.

## Limitations

- AWS SecretsManager is not supported with YugabyteDB.

<p class="next-steps">
    <a class="btn btn-primary" href="/documentation/database/aurora-postgresql">Aurora PostgreSQL <i class="fa fa-arrow-right"></i></a>
</p>
