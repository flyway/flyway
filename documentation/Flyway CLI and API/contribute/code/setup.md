---
layout: code
pill: codeSetup
subtitle: Dev Environment Setup - Code
---
# Code: Dev Environment Setup

To contribute to Flyway you will need to set up your development environment so that you can build and run Flyway.

For this you will need to set up Git, a JDK, Maven and your IDE.

## Git

Flyway uses Git for version control. Download the latest version from the [Git homepage](https://git-scm.com/).

Make sure the directory containing the binaries has been added the `PATH`. If you downloaded an installer this
should have been taken care of for you.

## JDK

Even though Flyway depends on JDK 8 or later, Flyway is built using OpenJDK 11.

So grab the latest version from the [AdoptOpenJDK website](https://adoptopenjdk.net/releases.html?variant=openjdk11).

After the installation is complete
- set up an environment variable called `JAVA_HOME` that points to your JDK installation directory
- add the `bin` directory under `JAVA_HOME` to the `PATH`

## Maven

Flyway is built with Maven 3. So grab the latest version from the [Apache website](http://maven.apache.org/download.html).

After the installation is complete
- set up an environment variable called `M2_HOME` that points to your Maven installation directory
- add the `bin` directory under `M2_HOME` to the `PATH`

## IDE

We use IntelliJ for development. You can grab the latest version from the [JetBrains website](http://www.jetbrains.com/idea/).

Eclipse should be fine too. However Eclipse has different
defaults for code formatting and import reordering. Keep this in mind so merge conflicts can be reduced to a
minimum.

## JREs

In order to build the platform-specific packages of the command-line tool, you need the JRE for each platform.
Unfortunately these are not available through Maven Central and must be added manually to your local Maven
repository.

Download the following files from the [AdoptOpenJDK website](https://adoptopenjdk.net/archive.html?variant=openjdk11):

- [Windows x64 JRE 11.0.2](https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.2%2B9/OpenJDK11U-jre_x64_windows_hotspot_11.0.2_9.zip)
- [Linux x64 JRE 11.0.2](https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.2%2B9/OpenJDK11U-jre_x64_linux_hotspot_11.0.2_9.tar.gz)
- [macOS x64 JRE 11.0.2](https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.2%2B9/OpenJDK11U-jre_x64_mac_hotspot_11.0.2_9.tar.gz)

and place them in your local maven repository as:

- `~/.m2/repository/net/adoptopenjdk/jre/11.0.2/jre-11.0.2-windows-x64.zip`
- `~/.m2/repository/net/adoptopenjdk/jre/11.0.2/jre-11.0.2-linux-x64.tar.gz`
- `~/.m2/repository/net/adoptopenjdk/jre/11.0.2/jre-11.0.2-macos-x64.tar.gz`

by invoking:

<pre class="console">mvn install:install-file -DgroupId=net.adoptopenjdk \
-DartifactId=jre \
-Dclassifier=windows-x64 \
-Dtype=zip \
-Dversion=11.0.2 \
-Dpackaging=zip \
-Dfile=path/to/OpenJDK11U-jre_x64_windows_hotspot_11.0.2_9.zip</pre>

<pre class="console">mvn install:install-file -DgroupId=net.adoptopenjdk \
-DartifactId=jre \
-Dclassifier=linux-x64 \
-Dtype=tar.gz \
-Dversion=11.0.2 \
-Dpackaging=tar.gz \
-Dfile=path/to/OpenJDK11U-jre_x64_linux_hotspot_11.0.2_9.tar.gz</pre>

<pre class="console">mvn install:install-file -DgroupId=net.adoptopenjdk \
-DartifactId=jre \
-Dclassifier=macos-x64 \
-Dtype=tar.gz \
-Dversion=11.0.2 \
-Dpackaging=tar.gz \
-Dfile=path/to/OpenJDK11U-jre_x64_mac_hotspot_11.0.2_9.tar.gz</pre>

<p class="next-steps">
    <a class="btn btn-primary" href="/documentation/contribute/code/submit">Submit your changes <i class="fa fa-arrow-right"></i></a>
</p>
