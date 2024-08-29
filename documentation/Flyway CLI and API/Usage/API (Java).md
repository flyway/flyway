---
pill: overview
subtitle: API
redirect_from: /documentation/api/
---
# API

Flyway brings the largest benefits when **integrated within an application**. By integrating Flyway
you can ensure that the application and its **database will always be compatible**, with no manual
intervention required. Flyway checks the version of the database and applies new migrations automatically
**before** the rest of the application starts. This is important, because the database must first
be migrated to a state the rest of the code can work with.

## Supported Java Versions

- `Java 17`

## Download
<table class="table">
    <tr>
        <th>Maven</th>
        <td>
            <pre class="prettyprint">&lt;repositories&gt;
    ...
    &lt;repository&gt;
        &lt;id&gt;redgate&lt;/id&gt;
        &lt;url&gt;https://download.red-gate.com/maven/release&lt;/url&gt;
    &lt;/repository&gt;
    ...
&lt;/repositories&gt;
&lt;dependencies&gt;
    ...
    &lt;dependency&gt;
        &lt;groupId&gt;<strong>com.redgate.flyway</strong>&lt;/groupId&gt;
        &lt;artifactId&gt;flyway-core&lt;/artifactId&gt;
        &lt;version&gt;{{ site.flywayVersion }}&lt;/version&gt;
    &lt;/dependency&gt;
     &lt;!-- If you need Teams features then you'll need this additional dependency --&gt;
     &lt;dependency&gt;
        &lt;groupId&gt;<strong>com.redgate.flyway</strong>&lt;/groupId&gt;
        &lt;artifactId&gt;flyway-proprietary&lt;/artifactId&gt;
        &lt;version&gt;{{ site.flywayVersion }}&lt;/version&gt;
    &lt;/dependency&gt;   
    ...
&lt;/dependencies&gt;</pre>
        </td>
    </tr>
    <tr>
        <th>Gradle</th>
        <td>
            <pre class="prettyprint">repositories {
    mavenCentral()
    maven {
        url "https://download.red-gate.com/maven/release"
    }
}
dependencies {
    implementation "<strong>com.redgate.flyway</strong>:flyway-core:{{ site.flywayVersion }}"
}</pre>
        </td>
    </tr>
    <tr>
        <th>Binary</th>
        <td>
            <a style="text-decoration: none; background: rgb(204,0,0); padding: 6px 40px; border-radius: 10px; color: white; font-weight: bold;" href="https://flywaydb.org/download/thankyou?dl={{site.enterpriseUrl}}/flyway-core/{{site.flywayVersion}}/flyway-core-{{site.flywayVersion}}.jar"><i class="fa fa-download"></i> flyway-core-{{site.flywayVersion}}.jar</a>
            <a class="note" href="{{site.enterpriseUrl}}/flyway-core/{{site.flywayVersion}}/flyway-core-{{site.flywayVersion}}.jar.md5">md5</a>
            <a class="note" href="{{site.enterpriseUrl}}/flyway-core/{{site.flywayVersion}}/flyway-core-{{site.flywayVersion}}.jar.sha1">sha1</a>
        </td>
    </tr>
    <tr>
        <th>Licensing</th>
        <td>
            By downloading Flyway Community you confirm that you have read and agree to the terms of the <a href="https://www.red-gate.com/assets/purchase/assets/subscription-license.pdf">Redgate EULA</a>.
        </td>
    </tr>
</table>


Please note, the `groupId` changed at Flyway V10.0.0 from `org.flywaydb.enterprise` to `com.redgate.flyway`.

For older versions see [Accessing Older Versions of Flyway Engine](https://documentation.red-gate.com/flyway/release-notes-and-older-versions/accessing-older-versions-of-flyway-engine)

### Open Source Edition
<table class="table">
    <tr>
        <th>Maven</th>
        <td>
            <pre class="prettyprint">&lt;dependencies&gt;
    ...
    &lt;dependency&gt;
        &lt;groupId&gt;org.flywaydb&lt;/groupId&gt;
        &lt;artifactId&gt;flyway-core&lt;/artifactId&gt;
        &lt;version&gt;{{ site.flywayVersion }}&lt;/version&gt;
    &lt;/dependency&gt;
    ...
&lt;/dependencies&gt;</pre>
        </td>
    </tr>
    <tr>
        <th>Gradle</th>
        <td>
            <pre class="prettyprint">dependencies {
    implementation "org.flywaydb:flyway-core:{{ site.flywayVersion }}"
}</pre>
        </td>
    </tr>
    <tr>
        <th>Binary</th>
        <td>
            <a style="text-decoration: none; background: rgb(204,0,0); padding: 6px 40px; border-radius: 10px; color: white; font-weight: bold;" href="https://flywaydb.org/download/thankyou?dl=https://repo1.maven.org/maven2/org/flywaydb/flyway-core/{{site.flywayVersion}}/flyway-core-{{site.flywayVersion}}.jar"><i class="fa fa-download"></i> flyway-core-{{site.flywayVersion}}.jar</a>
            <a class="note" href="https://repo1.maven.org/maven2/org/flywaydb/flyway-core/{{site.flywayVersion}}/flyway-core-{{site.flywayVersion}}.jar.md5">md5</a>
            <a class="note" href="https://repo1.maven.org/maven2/org/flywaydb/flyway-core/{{site.flywayVersion}}/flyway-core-{{site.flywayVersion}}.jar.sha1">sha1</a>
        </td>
    </tr>
    <tr>
        <th>Sources</th>
        <td>
            <a style="text-decoration: none; background: rgb(204,0,0); padding: 6px 40px; border-radius: 10px; color: white; font-weight: bold;" href="https://flywaydb.org/download/thankyou?dl=https://repo1.maven.org/maven2/org/flywaydb/flyway-core/{{site.flywayVersion}}/flyway-core-{{site.flywayVersion}}-sources.jar"><i class="fa fa-download"></i> flyway-core-{{site.flywayVersion}}-sources.jar</a>
            <a class="note" href="https://repo1.maven.org/maven2/org/flywaydb/flyway-core/{{site.flywayVersion}}/flyway-core-{{site.flywayVersion}}-sources.jar.md5">md5</a>
            <a class="note" href="https://repo1.maven.org/maven2/org/flywaydb/flyway-core/{{site.flywayVersion}}/flyway-core-{{site.flywayVersion}}-sources.jar.sha1">sha1</a>
        </td>
    </tr>
</table>


## The Flyway Class

The central piece of Flyway's database migration infrastructure is the
**[org.flywaydb.core.Flyway](https://javadoc.io/doc/org.flywaydb/flyway-core/latest/org/flywaydb/core/Flyway.html)**
class. It is your **one-stop shop** for working with Flyway programmatically. It serves both as a
**configuration** and a **launching** point for all of Flyway's functions.

### Programmatic Configuration (Java)

Flyway is super easy to use programmatically:

```java
import org.flywaydb.core.Flyway;

...
Flyway flyway = Flyway.configure().dataSource(url, user, password).load();
flyway.migrate();

// Start the rest of the application (incl. Hibernate)
...
```

See [configuration](Configuration/parameters) for a full list of supported configuration parameters.

### JDBC Drivers

You will need to include the relevant JDBC driver for your chosen database as a dependency in your Java project.
For instance in your `pom.xml` for a Maven project. The version of the JDBC driver supported for each database is specified in the 'Supported Databases' list in the left hand side navigation menu.

### Spring Configuration

As an alternative to the programmatic configuration, here is how you can configure and start Flyway in a classic
Spring application using XML bean configuration:

```xml
<bean id="flywayConfig" class="org.flywaydb.core.api.configuration.ClassicConfiguration">
    <property name="dataSource" ref="..."/>
    ...
</bean>

<bean id="flyway" class="org.flywaydb.core.Flyway" init-method="migrate">
    <constructor-arg ref="flywayConfig"/>
</bean>

<!-- The rest of the application (incl. Hibernate) -->
<!-- Must be run after Flyway to ensure the database is compatible with the code -->
<bean id="sessionFactory" class="..." depends-on="flyway">
    ...
</bean>
```
