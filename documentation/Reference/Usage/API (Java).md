---
subtitle: API
redirect_from: /documentation/api/
---

Flyway brings the largest benefits when **integrated within an application**. By integrating Flyway
you can ensure that the application and its **database will always be compatible**, with no manual
intervention required. Flyway checks the version of the database and applies new migrations automatically
**before** the rest of the application starts. This is important, because the database must first
be migrated to a state the rest of the code can work with.

## Supported Java Versions

- `Java JDK 17+`
- Flyway is built with language level 17.

## Download

### Redgate Edition

#### Maven

```xml
<repositories>
  ...
  <repository>
    <id>redgate</id>
    <url>https://download.red-gate.com/maven/release</url>
  </repository>
  ...
</repositories>
<dependencies>
  ...
  <dependency>
    <groupId>com.redgate.flyway</groupId>
    <artifactId>flyway-core</artifactId>
    <version>{{ site.flywayVersion }}</version>
  </dependency>
  <!-- If you need Teams or Enterprise features then you'll need these additional dependencies -->
  <dependency>
    <groupId>com.redgate.flyway</groupId>
    <artifactId>flyway-proprietary</artifactId>
    <version>{{ site.flywayVersion }}</version>
  </dependency>
  <dependency>
    <groupId>com.redgate.flyway</groupId>
    <artifactId>flyway-redgate-licensing</artifactId>
    <version>{{ site.flywayVersion }}</version>
  </dependency>
  ...
</dependencies>
```

#### Gradle

```groovy
repositories {
    mavenCentral()
    maven {
        url "https://download.red-gate.com/maven/release"
    }
}
dependencies {
    implementation "com.redgate.flyway:flyway-core:{{ site.flywayVersion }}"
    // If you need Teams or Enterprise features then you'll need these additional dependencies
    implementation "com.redgate.flyway:flyway-proprietary:{{ site.flywayVersion }}"
    implementation "com.redgate.flyway:flyway-redgate-licensing:{{ site.flywayVersion }}"
}
```

#### Binary

<a style="text-decoration: none; background: rgb(204,0,0); padding: 6px 40px; border-radius: 10px; color: white; font-weight: bold;" href="{{site.enterpriseUrl}}/flyway-core/{{site.flywayVersion}}/flyway-core-{{site.flywayVersion}}.jar"><i class="fa fa-download"></i> flyway-core-{{site.flywayVersion}}.jar</a>
<a class="note" href="{{site.enterpriseUrl}}/flyway-core/{{site.flywayVersion}}/flyway-core-{{site.flywayVersion}}.jar.md5">md5</a>
<a class="note" href="{{site.enterpriseUrl}}/flyway-core/{{site.flywayVersion}}/flyway-core-{{site.flywayVersion}}.jar.sha1">sha1</a>

#### Licensing

By downloading Flyway Community you confirm that you have read and agree to the terms of the <a href="https://www.red-gate.com/assets/purchase/assets/subscription-license.pdf">Redgate EULA</a>.

---

Please note, the `groupId` changed at Flyway V10.0.0 from `org.flywaydb.enterprise` to `com.redgate.flyway` and published as a convenience in both locations up till Flyway V10.22.0

For older versions see [Accessing Older Versions of Flyway Engine](https://documentation.red-gate.com/flyway/release-notes-and-older-versions/accessing-older-versions-of-flyway-engine)

### Open Source Edition

#### Maven

```xml
<dependencies>
  ...
  <dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
    <version>{{ site.flywayVersion }}</version>
  </dependency>
  ...
</dependencies>
```

#### Gradle

```groovy
dependencies {
    implementation "org.flywaydb:flyway-core:{{ site.flywayVersion }}"
}
```

#### Binary

<a style="text-decoration: none; background: rgb(204,0,0); padding: 6px 40px; border-radius: 10px; color: white; font-weight: bold;" href="https://repo1.maven.org/maven2/org/flywaydb/flyway-core/{{site.flywayVersion}}/flyway-core-{{site.flywayVersion}}.jar"><i class="fa fa-download"></i> flyway-core-{{site.flywayVersion}}.jar</a>
<a class="note" href="https://repo1.maven.org/maven2/org/flywaydb/flyway-core/{{site.flywayVersion}}/flyway-core-{{site.flywayVersion}}.jar.md5">md5</a>
<a class="note" href="https://repo1.maven.org/maven2/org/flywaydb/flyway-core/{{site.flywayVersion}}/flyway-core-{{site.flywayVersion}}.jar.sha1">sha1</a>

#### Sources

<a style="text-decoration: none; background: rgb(204,0,0); padding: 6px 40px; border-radius: 10px; color: white; font-weight: bold;" href="https://repo1.maven.org/maven2/org/flywaydb/flyway-core/{{site.flywayVersion}}/flyway-core-{{site.flywayVersion}}-sources.jar"><i class="fa fa-download"></i> flyway-core-{{site.flywayVersion}}-sources.jar</a>
<a class="note" href="https://repo1.maven.org/maven2/org/flywaydb/flyway-core/{{site.flywayVersion}}/flyway-core-{{site.flywayVersion}}-sources.jar.md5">md5</a>
<a class="note" href="https://repo1.maven.org/maven2/org/flywaydb/flyway-core/{{site.flywayVersion}}/flyway-core-{{site.flywayVersion}}-sources.jar.sha1">sha1</a>

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

See [configuration](Configuration) for a full list of supported configuration parameters.

### JDBC Drivers

You will need to include the relevant JDBC driver for your chosen database as a dependency in your Java project.
For instance in your `pom.xml` for a Maven project. The version of the JDBC driver supported for each database is specified in the 'Database Driver Reference' list in the navigation menu.

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
