---
pill: gradle_overview
subtitle: Gradle Plugin
redirect_from: /documentation/gradle/
---
# Gradle Plugin

The Flyway Gradle plugin supports **Gradle 7.6.x*** and **Gradle 8.x**
running on **Java 17**.

*Older versions fail due to some dependencies having multi-release jars with Java 19 code. 
This is a known [Gradle issue](https://github.com/gradle/gradle/issues/24390). 

## Installation
### Flyway Community Edition
This includes Teams & Enterprise features subject to license.

See [Upgrading to Teams or Enterprise](Learn More/Upgrading to Flyway Teams or Enterprise) to find out about the edition contents

<code>build.gradle</code>
<table class="table">
    <tr>
        <td>
            <pre class="prettyprint">repositories {
    mavenCentral()
    maven {
        url "https://download.red-gate.com/maven/release"
    }
}
plugins {
    id "<strong>com.redgate.flyway</strong>" version "{{ site.flywayVersion }}"
}</pre>
        </td>
    </tr>
    <tr>
        <td>
By downloading Flyway Community Gradle Plugin you confirm that you have read and agree to the terms of the <a href="https://www.red-gate.com/assets/purchase/assets/subscription-license.pdf?_ga=2.265045707.556964523.1656332792-1685764737.1620948215">Redgate EULA</a>.
        </td>
    </tr>
</table>

For older versions, see [Accessing Older Versions of Flyway Engine](https://documentation.red-gate.com/flyway/release-notes-and-older-versions/accessing-older-versions-of-flyway-engine)

### Open Source Edition

<code>build.gradle</code>
<table class="table">
    <tr>
        <td>
            <pre class="prettyprint">plugins {
    id "org.flywaydb.flyway" version "{{ site.flywayVersion }}"
}</pre>
        </td>
    </tr>
</table>


## Tasks

<table class="table table-bordered table-hover">
    <thead>
    <tr>
        <th><strong>Name</strong></th>
        <th><strong>Description</strong></th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td><a href="Usage/Gradle Task/Gradle Task Flywaymigrate">flywayMigrate</a></td>
        <td>Migrates the database</td>
    </tr>
    <tr>
        <td><a href="Usage/Gradle Task/Gradle Task Flywayclean">flywayClean</a></td>
        <td>Drops all objects in the configured schemas</td>
    </tr>
    <tr>
        <td><a href="Usage/Gradle Task/Gradle Task Flywayinfo">flywayInfo</a></td>
        <td>Prints the details and status information about all the migrations</td>
    </tr>
    <tr>
        <td><a href="Usage/Gradle Task/Gradle Task Flywayvalidate">flywayValidate</a></td>
        <td>Validates the applied migrations against the ones available on the classpath</td>
    </tr>
    <tr>
        <td><a href="Usage/Gradle Task/Gradle Task Flywayundo">flywayUndo</a> {% include teams.html %}</td>
        <td>Undoes the most recently applied versioned migration</td>
    </tr>
    <tr>
        <td><a href="Usage/Gradle Task/Gradle Task Flywaybaseline">flywayBaseline</a></td>
        <td>Baselines an existing database, excluding all migrations up to and including baselineVersion</td>
    </tr>
    <tr>
        <td><a href="Usage/Gradle Task/Gradle Task Flywayrepair">flywayRepair</a></td>
        <td>Repairs the schema history table</td>
    </tr>
    </tbody>
</table>


## Configuration

The Flyway Gradle plugin can be configured in a wide variety of following ways, which can all be combined at will.

### Build script (single database)

The easiest way is to simply define a Flyway section in your `build.gradle`:

```groovy
flyway {
    url = 'jdbc:h2:mem:mydb'
    user = 'myUsr'
    password = 'mySecretPwd'
    schemas = ['schema1', 'schema2', 'schema3']
    placeholders = [
        'keyABC': 'valueXYZ',
        'otherplaceholder': 'value123'
    ]
}
```

### Build script (multiple databases)

To migrate multiple database you have the option to extend the various Flyway tasks in your `build.gradle`:

```groovy
task migrateDatabase1(type: org.flywaydb.gradle.task.FlywayMigrateTask) {
    url = 'jdbc:h2:mem:mydb1'
    user = 'myUsr1'
    password = 'mySecretPwd1'
}

task migrateDatabase2(type: org.flywaydb.gradle.task.FlywayMigrateTask) {
    url = 'jdbc:h2:mem:mydb2'
    user = 'myUsr2'
    password = 'mySecretPwd2'
}
```

### Java migrations and callbacks

When using Java migrations and callbacks with the gradle Flyway plugin, you need to ensure that the classes have been compiled before running the `flywayMigrate` (or `flywayClean` etc) task.

You can do this by explicitly running the `classes` task before `flywayMigrate` e.g. `gradle classes flywayMigrate`.

Alternatively you can make the `flywayMigrate` task depend on classes.

```groovy
dependencies {
    implementation "org.flywaydb:flyway-core:${flywayVersion}"
}

flyway {
    url = 'jdbc:h2:mem:mydb'
    user = 'myUsr'
    password = 'mySecretPwd'
    locations = ['classpath:db/migration']
}

// we need to build classes before we can migrate
flywayMigrate.dependsOn classes
```

### Extending the default classpath

By default the Flyway Gradle plugin uses a classpath consisting of the following Gradle configurations for loading drivers, migrations, resolvers, callbacks, etc.:
 - **Gradle 4.x and newer:**  `compileClasspath`, `runtimeClasspath`, `testCompileClasspath` and `testRuntimeClasspath`
 - **Gradle 3.x:**  `compileClasspath`, `runtime`, `testCompileClasspath` and `testRuntime`

You can optionally extend this default classpath with your own custom configurations in `build.gradle` as follows:

```groovy
// Start by defining a custom configuration like 'provided', 'migration' or similar
configurations {
    flywayMigration
}

// Declare your dependencies as usual for each configuration
dependencies {
    implementation "org.flywaydb:flyway-core:${flywayVersion}"
    flywayMigration "com.mygroupid:my-lib:1.2.3"
}

flyway {
    url = 'jdbc:h2:mem:mydb'
    user = 'myUsr'
    password = 'mySecretPwd'
    schemas = ['schema1', 'schema2', 'schema3']
    placeholders = [
        'keyABC': 'valueXYZ',
        'otherplaceholder': 'value123'
    ]
    // Include your custom configuration here in addition to any default ones you want included
    configurations = [ 'compileClasspath', 'flywayMigration' ]
}
```

For details on how to setup and use custom Gradle configurations, see the [official Gradle documentation](https://docs.gradle.org/current/dsl/org.gradle.api.artifacts.ConfigurationContainer.html).

### Adding dependencies on Flyway Database Types

For some Flyway database types, like [Cloud Spanner](/supported databases/Google Cloud Spanner) and [SQL Server](<Supported Databases/SQL Server Database>), you'll need to add a dependency to the database type in a `buildscript` closure to get your Gradle commands to work properly. This puts the database type on the build classpath, and not the project classpath.

Here is an example `build.gradle`:

```groovy
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath "org.flywaydb:flyway-mysql:{{ site.flywayVersion }} "
    }
}
```

Without this you may see an error like the following: `No database found to handle jdbc:...`

### Working directory

Some databases can take a relative path inside the JDBC url (such as to specify a file to write to). When running the Flyway gradle plugin, this is relative to `~/.gradle/` not the configuration location. This may not be what you expected, so you may want to specify the path more explicitly such as in the following example:

```
flyway {
    url = "jdbc:h2:file:${System.getProperty('user.dir')}/<database>"
    user = <user>
}
```

### Gradle properties

The plugin can also be configured using Gradle properties. Their can be passed either directly via the command-line:

<pre class="console"><span>&gt;</span> gradle -Pflyway.user=myUsr -Pflyway.schemas=schema1,schema2 -Pflyway.placeholders.keyABC=valXYZ</pre>

or via a `gradle.properties` file:

<pre class="prettyprint">flyway.user=myUser
flyway.password=mySecretPwd

# List are defined as comma-separated values
flyway.schemas=schema1,schema2,schema3

# Individual placeholders are prefixed by flyway.placeholders.
flyway.placeholders.keyABC=valueXYZ
flyway.placeholders.otherplaceholder=value123</pre>

They can they be accessed as follows from your `build.gradle`:

<pre class="prettyprint">project.ext['flyway.user']='myUsr'
project.ext['flyway.password']='mySecretPwd'
project.ext['flyway.schemas']='schema1,schema2,schema3'
project.ext['flyway.placeholders.keyABC']='valueXYZ'
project.ext['flyway.placeholders.otherplaceholder']='value123'</pre>

### Environment Variables

To make it ease to work with cloud and containerized environments, Flyway also supports configuration via
[environment variables](Configuration/Environment Variables).

### System properties

Configuration can also be supplied directly via the command-line using JVM system properties:

<pre class="console"><span>&gt;</span> gradle -Dflyway.user=myUser -Dflyway.schemas=schema1,schema2 -Dflyway.placeholders.keyABC=valueXYZ</pre>

### Config files

[Config files](Configuration/Configuration Files) are supported by the Flyway Gradle plugin.

Flyway will search for and automatically load the `<user-home>/flyway.conf` config file if present.

It is also possible to point Flyway at one or more additional config files. This is achieved by
supplying the System property `flyway.configFiles` as follows:

<pre class="console"><span>&gt;</span> gradle <strong>-Dflyway.configFiles=</strong>path/to/myAlternativeConfig.conf flywayMigrate</pre>

To pass in multiple files, separate their names with commas:

<pre class="console"><span>&gt;</span> gradle <strong>-Dflyway.configFiles</strong>=path/to/myAlternativeConfig.conf,other.conf flywayMigrate</pre>

Relative paths are relative to the directory containing your `build.gradle` file.

Alternatively you can also use the `FLYWAY_CONFIG_FILES` environment variable for this.
When set it will take preference over the command-line parameter.

<pre class="console"><span>&gt;</span> export <strong>FLYWAY_CONFIG_FILES</strong>=path/to/myAlternativeConfig.conf,other.conf</pre>

By default Flyway loads configuration files using UTF-8. To use an alternative encoding, pass the system property `flyway.configFileEncoding`
    as follows:
<pre class="console"><span>&gt;</span> gradle <strong>-Dflyway.configFileEncoding=</strong>ISO-8859-1 flywayMigrate</pre>

This is also possible via the `flyway` section of your `build.gradle` or via Gradle properties, as described above.

Alternatively you can also use the `FLYWAY_CONFIG_FILE_ENCODING` environment variable for this.
    When set it will take preference over the command-line parameter.

<pre class="console"><span>&gt;</span> export <strong>FLYWAY_CONFIG_FILE_ENCODING</strong>=ISO-8859-1</pre>

### Overriding order

The Flyway Gradle plugin has been carefully designed to load and override configuration in a sensible order.

Settings are loaded in the following order (higher items in the list take precedence over lower ones):
1. System properties
2. Environment variables
3. Custom config files
4. Gradle properties
5. Flyway configuration section in `build.gradle`
6. `<user-home>/flyway.conf`
7. Flyway Gradle plugin defaults

The means that if for example `flyway.url` is both present in a config file and passed as `-Dflyway.url=` from the command-line,
the JVM system property passed in via the command-line will take precedence and be used.
