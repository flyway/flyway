---
subtitle: Command-line
redirect_from: Commandsline/
---

The Flyway command-line tool is a standalone Flyway distribution. It runs on Windows, macOS and Linux and it is primarily meant for users who wish to migrate their database from the command-line without having to integrate Flyway into their applications nor having to install a build tool.

## Download and installation

### Windows

 <a style="text-decoration: none; background: rgb(204,0,0); padding: 6px 40px; border-radius: 10px; color: white; font-weight: bold;" href="{{site.enterpriseUrl}}/flyway-commandline/{{site.flywayVersion}}/flyway-commandline-{{site.flywayVersion}}-windows-x64.zip">flyway-commandline-{{site.flywayVersion}}-windows-x64.zip</a>
 
 <a class="note" href="{{site.enterpriseUrl}}/flyway-commandline/{{site.flywayVersion}}/flyway-commandline-{{site.flywayVersion}}-windows-x64.zip.md5">md5</a>
 <a class="note" href="{{site.enterpriseUrl}}/flyway-commandline/{{site.flywayVersion}}/flyway-commandline-{{site.flywayVersion}}-windows-x64.zip.sha1">sha1</a>
 
 Extract the archive and simply add the new `flyway-{{site.flywayVersion}}` directory to the `PATH` to make the `flyway` command available from anywhere on your system.

### macOS

<a style="text-decoration: none; background: rgb(204,0,0); padding: 6px 40px; border-radius: 10px; color: white; font-weight: bold;" href="{{site.enterpriseUrl}}/flyway-commandline/{{site.flywayVersion}}/flyway-commandline-{{site.flywayVersion}}-macosx-x64.tar.gz">flyway-commandline-{{site.flywayVersion}}-macosx-x64.tar.gz</a>

<a class="note" href="{{site.enterpriseUrl}}/flyway-commandline/{{site.flywayVersion}}/flyway-commandline-{{site.flywayVersion}}-macosx-x64.tar.gz.md5">md5</a>
<a class="note" href="{{site.enterpriseUrl}}/flyway-commandline/{{site.flywayVersion}}/flyway-commandline-{{site.flywayVersion}}-macosx-x64.tar.gz.sha1">sha1</a>

<a style="text-decoration: none; background: rgb(204,0,0); padding: 6px 40px; border-radius: 10px; color: white; font-weight: bold;" href="{{site.enterpriseUrl}}/flyway-commandline/{{site.flywayVersion}}/flyway-commandline-{{site.flywayVersion}}-macosx-arm64.tar.gz">flyway-commandline-{{site.flywayVersion}}-macosx-arm64.tar.gz</a>

<a class="note" href="{{site.enterpriseUrl}}/flyway-commandline/{{site.flywayVersion}}/flyway-commandline-{{site.flywayVersion}}-macosx-arm64.tar.gz.md5">md5</a>
<a class="note" href="{{site.enterpriseUrl}}/flyway-commandline/{{site.flywayVersion}}/flyway-commandline-{{site.flywayVersion}}-macosx-arm64.tar.gz.sha1">sha1</a>

### Linux

Download, extract and install by adding to `PATH` (requires `sudo` permissions):
  <pre class="console" style="overflow-x: auto"><span>$</span> wget -qO- {{site.enterpriseUrl}}/flyway-commandline/{{site.flywayVersion}}/<strong>flyway-commandline-{{site.flywayVersion}}-linux-x64.tar.gz</strong> | tar -xvz && sudo ln -s `pwd`/flyway-{{site.flywayVersion}}/flyway /usr/local/bin </pre>

Or simply download the archive:

<a style="text-decoration: none; background: rgb(204,0,0); padding: 6px 40px; border-radius: 10px; color: white; font-weight: bold;" href="{{site.enterpriseUrl}}/flyway-commandline/{{site.flywayVersion}}/flyway-commandline-{{site.flywayVersion}}-linux-x64.tar.gz">flyway-commandline-{{site.flywayVersion}}-linux-x64.tar.gz</a>

<a class="note" href="{{site.enterpriseUrl}}/flyway-commandline/{{site.flywayVersion}}/flyway-commandline-{{site.flywayVersion}}-linux-x64.tar.gz.md5">md5</a>
<a class="note" href="{{site.enterpriseUrl}}/flyway-commandline/{{site.flywayVersion}}/flyway-commandline-{{site.flywayVersion}}-linux-x64.tar.gz.sha1">sha1</a><br/><br/>

### Docker

(Linux only) Download, extract and install by adding to `PATH` (requires `sudo` permissions):
<pre class="console"><span>$</span> sudo sh -c 'echo "docker run --rm <strong>redgate/flyway:{{site.flywayVersion}} $*</strong>" > /usr/local/bin/flyway && chmod +x /usr/local/bin/flyway'</pre>
(All platforms) Or simply download the image:
<pre class="console"><span>&gt;</span> docker pull <strong>redgate/flyway:{{site.flywayVersion}}</strong></pre>
Go to Docker Hub for <a href="https://hub.docker.com/r/redgate/flyway/">detailed usage instructions</a>.

## Directory structure

The Flyway download, once extracted, now becomes a directory with the following structure:

```
flyway
    conf/       Configuration file(s)
    drivers/    JDBC Drivers
    jre/
    lib/
    licenses/
    sql/        SQL migrations
    flyway      macOS/Linux executable
    flyway.cmd  Windows executable
```

## Usage

`> flyway [options] command`

## Command Line parameters

For command line parameters see [Command-line Parameters](<Command-line Parameters>).

## Commands

For a complete list of commands see [Commands](<Commands>).

## JDBC drivers

In order to connect with your database, Flyway needs the appropriate JDBC driver to be available in its `drivers` directory.

To see if Flyway ships with the JDBC driver for your database, visit the *Driver* section of the documentation page for your database. For example, here is the [Oracle Drivers section](<Database Driver Reference/oracle database#driver>).

If Flyway does not ship with the JDBC driver, you will need to download the driver and place it in the `drivers` directory yourself. Instructions on where to download drivers from are also in the *Driver* section of the documentation page for each database, under `Maven Central coordinates`.

## Configuration

The Flyway Command-line tool can be configured in a wide variety of ways.

You can find out more in the [Configuration](Configuration) section of the documents

- [Configuration files](https://documentation.red-gate.com/flyway/flyway-concepts/flyway-projects)
- [Environment variables](<Environment Variables>)

### Command-line Arguments

Finally, Flyway can also be configured by passing [parameters](Configuration) directly from the command-line:

```powershell
flyway -user=myuser -schemas=schema1,schema2 -placeholders.keyABC=valueXYZ migrate
```

### Escaping command-line arguments

Some command-line arguments will need care as specific characters may be interpreted differently depending on the
shell you are working in. The `url` parameter is particularly affected when it contains extra parameters with
equals `=` and ampersands `&`. For example:

**bash**, **macOS terminal** and **Windows command line**: use double-quotes:

<pre class="console"><span>&gt;</span> flyway info -url="jdbc:snowflake://ab12345.snowflakecomputing.com/?db=demo_db&user=foo"</pre>

**Powershell**: use double-quotes inside single-quotes:

<pre class="console"><span>&gt;</span> ./flyway info -url='"jdbc:snowflake://ab12345.snowflakecomputing.com/?db=demo_db&user=foo"'</pre>

### Command-line Namespacing

Flyway commandline arguments follow the same namespacing as the configuration files, with the `flyway` part assumed.
For example, the `flyway.url` configuration parameter can be set on the commandline as `flyway -url=jdbc://xxx`.
To avoid conflicts, many plugins and extensions use a different namespace, such as `flyway.init` or `flyway.diff`.
When configuring these, there are two options;

**Option 1**: Use the full namespace on the commandline:

```powershell
flyway init -init.databaseType=sqlserver 
```

**Option 2**: Use scoped namespacing:

*When the namespace is the same as the verb, you can configure parameters for that verb directly following that verb and
have the namespace assumed;*

```powershell
flyway init -databaseType=sqlserver 
```

*this will be interpreted as `flyway.init.databaseType=sqlserver`. When using scoped namespacing, you must have the
parameters follow the verb and proceeding other verbs.*

This will fail, as the `info` verb is not expecting the `databaseType` parameter:

```powershell
flyway init info -databaseType=sqlserver 
```

to allow for this to work, you have to ensure a logical order; for the above, either of the following would work;

```powershell
flyway info init -databaseType=sqlserver 
```

```powershell
flyway init -databaseType=sqlserver info 
```

In both cases, the `databaseType` comes after the `init` verb and before any other verb,
so it is assumed to be `flyway.init.databaseType=sqlserver

#### Backwards Compatibility:

Scoped namespacing is automatically disabled when ANY namespace is used, including parameters set under the environments 
namespace. This is to maintain backwards compatibility with scripts written for older versions of Flyway. To clarify, 
the following two examples will work;

```powershell
flyway init -databaseType=sqlserver -projectName=myProject
```

```powershell
flyway init -init.databaseType=sqlserver -init.projectName=myProject 
```

but the following will not;

```powershell
flyway init -init.databaseType=sqlserver -projectName=myProject 
```

because the `databaseType` parameter is declared in a namespace, scoped namespacing is disabled and thus `projectName` is assumed to be 
`flyway.projectName` and not `flyway.init.projectName`.

### Configuration from standard input

See [Config Files](<Command-line Parameters/Config Files Parameter>)

### Overriding order

See [Configuration Precedence](<Configuration Precedence>) for details of the configuration mechanism priority.

### Java Arguments

If you need to pass custom arguments to Flyway's JVM, you can do so by setting the `JAVA_ARGS` environment variable, either at runtime or persistently on the host system.
They will then automatically be taken into account when launching Flyway. This is particularly useful when needing to set JVM system properties.

Runtime example (Windows command line)
<pre class="console"><span>&gt;</span> set JAVA_ARGS=-Xms308M -Xmx432M</pre>
The corresponding system environment variable would be
Name: JAVA_ARGS
Value: -Xms308M -Xmx432M

See Oracle's documentation for a full list of available [JAVA_ARGS](https://docs.oracle.com/en/java/javase/21/docs/specs/man/java.html).

## Output

By default, all debug, info and warning output is sent to `stdout`. All errors are sent to `stderr`.

Flyway will automatically detect and use any logger class that it finds on its classpath that derives from any of the following:

- the Apache Commons Logging framework `org.apache.commons.logging.Log` (including Log4j v1)
- SLF4J `org.slf4j.Logger`
- Log4J v2 `org.apache.logging.log4j.Logger`

Alternatively, you can use the [loggers](<Configuration/Flyway Namespace/Flyway Loggers Setting>) configuration parameter to specify an exact desired logging framework to use.

The simplest way to make use of Flyway's automatic detection is to put all the necessary JAR files in Flyway's `lib` folder and any configuration in the Flyway root folder.
For example, if you wished to use `log4j` v2 with the Flyway command line, you would achieve this by placing the log4j JAR files and the corresponding configuration file `log4j2.xml` like this:

<pre class="filetree"><i class="fa fa-folder-open"></i> flyway-{{site.flywayVersion}}
  <i class="fa fa-folder"></i> conf
  <i class="fa fa-folder"></i> drivers
  <i class="fa fa-folder"></i> jre
  <i class="fa fa-folder-open"></i> lib
    <span><i class="fa fa-file-text"></i> log4j-api-2.17.1.jar</span>       <i class="fa fa-long-arrow-left"></i> log4j v2 jar
    <span><i class="fa fa-file-text"></i> log4j-core-2.17.1.jar</span>      <i class="fa fa-long-arrow-left"></i> log4j v2 jar
  <i class="fa fa-folder"></i> licenses
  <i class="fa fa-folder"></i> sql
  <span><i class="fa fa-file"></i> log4j2.xml</span>                   <i class="fa fa-long-arrow-left"></i> log4j configuration
</pre>

Similarly, to use `Logback` add the relevant files like this:

<pre class="filetree"><i class="fa fa-folder-open"></i> flyway-{{site.flywayVersion}}
  <i class="fa fa-folder"></i> conf
  <i class="fa fa-folder"></i> drivers
  <i class="fa fa-folder"></i> jre
  <i class="fa fa-folder-open"></i> lib
    <span><i class="fa fa-file-text"></i> logback-classic.1.1.7.jar</span> <i class="fa fa-long-arrow-left"></i> Logback jar
    <span><i class="fa fa-file-text"></i> logback-core-1.1.7.jar</span>    <i class="fa fa-long-arrow-left"></i> Logback jar
    <span><i class="fa fa-file-text"></i> slf4j-api-1.7.21.jar</span>      <i class="fa fa-long-arrow-left"></i> Logback dependency
  <i class="fa fa-folder"></i> licenses
  <i class="fa fa-folder"></i> sql
  <span><i class="fa fa-file"></i> logback.xml</span>                 <i class="fa fa-long-arrow-left"></i> Logback configuration
</pre>

If you are building Flyway into a larger application, this means you do not need to explicitly wire up any logging as it will automatically detect one of these frameworks.

### P6Spy

P6Spy is another approach to logging which operates at the driver or datasource level, and Flyway has integration with this. You can read about setting it up [here](https://p6spy.readthedocs.io/en/latest/install.html#generic-instructions) and configuring it [here](https://p6spy.readthedocs.io/en/latest/configandusage.html#configuration-and-usage).

### Debug output

Add `-X` to the argument list to also print debug output. If this gives you too much information, you can filter it
with normal command-line tools, for example:

**bash, macOS terminal**

<pre class="console"><span>&gt;</span> flyway migrate -X <strong>| grep -v 'term-to-filter-out'</strong></pre>

**Powershell**

<pre class="console"><span>&gt;</span> flyway migrate -X <strong>| sls -Pattern 'term-to-filter-out' -NoMatch</strong></pre>

**Windows command line**

<pre class="console"><span>&gt;</span> flyway migrate -X <strong>| findstr /v /c:"term-to-filter-out"</strong></pre>

### Writing to a file

Add `-outputFile=/my/output.txt` to the argument list to also write output to the specified file.

## Open Source Flyway

This project is a core part of Flyway and you can find more information about it in [Flyway Open Source](<Usage/Flyway Open Source>)
