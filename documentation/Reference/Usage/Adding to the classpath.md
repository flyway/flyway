---
pill: addingtotheclasspath
subtitle: Adding to the classpath
---

Flyway ships with support for a large number of databases and functionality, but due to limitations (such as licensing) it can't ship containing everything it supports. 
In these situations, Flyway will load the extra support/functionality if it is present on the classpath.

## How to add to the classpath

How you add to the classpath depends on how you are invoking Flyway.

### Command Line

When using the CLI, you can add `.jar` files to the classpath by either:
- Putting the libraries or drivers you want to include into a separate directory and then including it via the [`jarDirs`](</Configuration/Flyway Namespace/Flyway Jar Dirs Setting>) configuration parameter.
- A JDBC database driver can be put directly in the `drivers/` folder of your Flyway installation

<pre class="filetree"><i class="fa fa-folder-open"></i> flyway
  <i class="fa fa-folder-open"></i> assets
  <i class="fa fa-folder-open"></i> conf
  <i class="fa fa-folder-open"></i> drivers <i class="fa fa-long-arrow-left"></i> <- JDBC drivers go here
  <i class="fa fa-folder-open"></i> jre
  <i class="fa fa-folder-open"></i> lib
  <i class="fa fa-folder-open"></i> licenses
  <i class="fa fa-folder-open"></i> rules
  <i class="fa fa-file"></i> flyway
  <i class="fa fa-file"></i> flyway.cmd
</pre>

### API

When using the API, the jars you wish to include should be added as dependencies of the overall project, just as you would with any other java dependencies.

### Gradle

See [extending the gradle classpath](<Usage/Gradle Task#extending-the-default-classpath>).

### Maven

Simply add the library as a regular dependency of your maven project. e.g:

```
<dependencies>
    <dependency>
        <groupId>org.hsqldb</groupId>
        <artifactId>hsqldb</artifactId>
        <version>1.8.0.10</version>
    </dependency>
</dependencies>
```

## What can be added

The most common library to be added to Flyway is those that add JDBC driver support. For example the [Informix](<Database Driver Reference/Informix>) database is supported by Flyway, but the JDBC driver is not shipped with it. Therefore the `com.ibm.informix:jdbc:4.10.10.0` dependency needs to be added to the classpath to allow Flyway to work with it. See each database page for the JDBC driver they use and whether they are shipped with Flyway or not.


Other uses for adding libraries are [adding logging support](<Usage/Command Line#output>), adding [Java migrations](https://documentation.red-gate.com/flyway/flyway-concepts/migrations/java-based-migrations), and more.
