---
subtitle: Ignite
---
# Ignite (Thin)

## Supported Versions

- `N/A`

## Support Level

<table class="table">
    <tr>
        <th width="25%">Compatible</th>
        <td>&#10003;</td>
    </tr>
    <tr>
        <th width="25%">Certified</th>
        <td>&#10060;</td>
    </tr>
    <tr>
        <th width="25%">Guaranteed</th>
        <td>&#10060;</td>
    </tr>
</table>

Support for Ignite is provided only on a community-led basis, and is not formally supported by Redgate

## Drivers

<table class="table">
<thead>
</thead>
<tr>
<th>URL format</th>
<td><code>jdbc:ignite:thin://<i>host</i>:<i>port</i>/<i>database</i></code></td>
</tr>
<tr>
<th>SSL support</th>
<td>Not tested</td>
</tr>
<tr>
<th>Ships with Flyway Command-line</th>
<td>No</td>
</tr>
<tr>
<th>Maven Central coordinates</th>
<td><code>org.apache.ignite:ignite-core</code></td>
</tr>
<tr>
<th>Supported versions</th>
<td><code>N/A</code></td>
</tr>
<tr>
<th>Default Java class</th>
<td><code>org.apache.ignite.IgniteJdbcThinDriver</code></td>
</tr>
</table>


## Java Usage

Ignite support is a separate dependency for Flyway and will need to be added to your Java project to access these features.

### Maven

#### Community

```xml

<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-ignite</artifactId>
</dependency>
```

### Gradle

#### Community

```groovy
dependencies {
    compile "org.flywaydb:flyway-database-ignite"
}
```