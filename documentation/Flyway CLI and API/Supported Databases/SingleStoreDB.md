---
subtitle: SingleStoreDB
---

# SingleStoreDB

## Supported Versions

- `7.1` {% include teams.html %}
- `7.8` {% include teams.html %}

**Note: SingleStoreDB is currently only available to Teams and Enterprise users**

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

## Drivers

<table class="table">
<thead>
<tr>
<th></th>
<th>SingleStoreDB</th>
</tr>
</thead>
<tr>
<th>URL format</th>
<td><code>jdbc:singlestore://<i>host</i>:<i>port</i>/<i>database</i></code></td>
</tr>
<tr>
<th>SSL support</th>
<td>Yes - add <code>?useSsl=true</code></td>
</tr>
<tr>
<th>Ships with Flyway Command-line</th>
<td>Yes</td>
</tr>
<tr>
<th>Maven Central coordinates</th>
<td><code>com.singlestore:singlestore-jdbc-client</code></td>
</tr>
<tr>
<th>Supported versions</th>
<td><code>1.1.4</code> and later</td>
</tr>
<tr>
<th>Default Java class</th>
<td><code>com.singlestore.jdbc.Driver</code></td>
</tr>
</table>

## Java Usage

SingleStoreDB support is a separate dependency for Flyway and will need to be added to your Java project to access these features.

### Maven

#### Teams

```xml

<dependency>
    <groupId>org.flywaydb.enterprise</groupId>
    <artifactId>flyway-singlestore</artifactId>
    <version>{{ site.flywayVersion }}</version>
</dependency>
```
You will also need to [configure the repository](Usage/api-java)
### Gradle

#### Teams

```groovy
dependencies {
    compile "org.flywaydb.enterprise:flyway-singlestore"
}
```
You will also need to [configure the repository](Usage/api-java)
