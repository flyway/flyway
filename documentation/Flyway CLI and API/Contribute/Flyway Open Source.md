---
pill: Flyway Open Source
subtitle: Open source
---
Flyway remains at it's heart an open source project that is available [on Github](https://github.com/flyway/flyway).

If you were looking for the Redgate edition of Flyway, see [this download page](Usage/Command-Line). This brings additional functionality that is either paid-for or we are unable to release to the open source project.
# Download

## Open Source Flyway
### Windows

- [flyway-commandline-{{site.flywayVersion}}-windows-x64.zip](https://repo1.maven.org/maven2/org/flywaydb/flyway-commandline/{{site.flywayVersion}}/flyway-commandline-{{site.flywayVersion}}-windows-x64.zip)
  - [md5](https://repo1.maven.org/maven2/org/flywaydb/flyway-commandline/{{site.flywayVersion}}/flyway-commandline-{{site.flywayVersion}}-windows-x64.zip.md5)
  - [sha1](https://repo1.maven.org/maven2/org/flywaydb/flyway-commandline/{{site.flywayVersion}}/flyway-commandline-{{site.flywayVersion}}-windows-x64.zip.sha1)

Extract the archive and simply add the new `flyway-{{site.flywayVersion}}` directory to the `PATH` to make the `flyway` command available from anywhere on your system.

### macOS

- [flyway-commandline-{{site.flywayVersion}}-macosx-x64.tar.gz](https://repo1.maven.org/maven2/org/flywaydb/flyway-commandline/{{site.flywayVersion}}/flyway-commandline-{{site.flywayVersion}}-macosx-x64.tar.gz)
  - [md5](https://repo1.maven.org/maven2/org/flywaydb/flyway-commandline/{{site.flywayVersion}}/flyway-commandline-{{site.flywayVersion}}-macosx-x64.tar.gz.md5)
  - [sha1](https://repo1.maven.org/maven2/org/flywaydb/flyway-commandline/{{site.flywayVersion}}/flyway-commandline-{{site.flywayVersion}}-macosx-x64.tar.gz.sha1)

### Linux

Download, extract and install by adding to `PATH` (requires `sudo` permissions):
 <pre class="console" style="overflow-x: auto"><span>$</span> wget -qO- https://repo1.maven.org/maven2/org/flywaydb/flyway-commandline/{{site.flywayVersion}}/<strong>flyway-commandline-{{site.flywayVersion}}-linux-x64.tar.gz</strong> | tar -xvz && sudo ln -s `pwd`/flyway-{{site.flywayVersion}}/flyway /usr/local/bin </pre>

Or simply download the archive:

- [flyway-commandline-{{site.flywayVersion}}-linux-x64.tar.gz](https://repo1.maven.org/maven2/org/flywaydb/flyway-commandline/{{site.flywayVersion}}/flyway-commandline-{{site.flywayVersion}}-linux-x64.tar.gz)
    - [md5](https://repo1.maven.org/maven2/org/flywaydb/flyway-commandline/{{site.flywayVersion}}/flyway-commandline-{{site.flywayVersion}}-linux-x64.tar.gz.md5)
    - [sha1](https://repo1.maven.org/maven2/org/flywaydb/flyway-commandline/{{site.flywayVersion}}/flyway-commandline-{{site.flywayVersion}}-linux-x64.tar.gz.sha1)

### Docker
(Linux only) Download, extract and install by adding to `PATH` (requires `sudo` permissions):

 <pre class="console"><span>$</span> sudo sh -c 'echo "docker run --rm <strong>flyway/flyway:{{site.flywayVersion}} $*</strong>" > /usr/local/bin/flyway && chmod +x /usr/local/bin/flyway'</pre>

(All platforms) Or simply download the image:

 <pre class="console"><span>&gt;</span> docker pull <strong>flyway/flyway:{{site.flywayVersion}}</strong></pre>

Go to Docker Hub for [Flyway](https://hub.docker.com/r/flyway/flyway/) for detailed usage instructions.

### Older Versions
- Older versions, packages without JRE and sources are available from [Maven Central](https://repo1.maven.org/maven2/org/flywaydb/flyway-commandline)
- Older Docker images are available from [boxfuse/flyway](https://hub.docker.com/r/boxfuse/flyway/)

# Contribute
You can find information about how to contribute to the Flyway Open Source project in the [Contribute - Code](Contribute/Code) section.
