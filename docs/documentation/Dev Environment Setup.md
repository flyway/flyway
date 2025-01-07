---
layout: default
title: Dev Environment Setup
permalink: /documentation/setup
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

So grab the latest version from the [AdoptOpenJDK website](https://adoptopenjdk.net/releases.html?variant=OpenJDK17).

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

Download the v17 JRE package suitable for your OS files from the [Adoptium website](https://adoptium.net/temurin/releases/?version=17):
and place them in your local directory.

by invoking:


    mvn install:install-file -DgroupId=net.adoptopenjdk \
        -DartifactId=jre \
        -Dclassifier=windows-x64 \
        -Dtype=zip \
        -Dversion=17.0.6 \
        -Dpackaging=zip \
        -Dfile=path/to/OpenJDK17U-jre_x64_windows_hotspot_17.0.6_10.zip



    mvn install:install-file -DgroupId=net.adoptopenjdk \
        -DartifactId=jre \
        -Dclassifier=linux-x64 \
        -Dtype=tar.gz \
        -Dversion=17.0.6 \
        -Dpackaging=tar.gz \
        -Dfile=path/to/OpenJDK17U-jre_x64_linux_hotspot_17.0.6_10.tar.gz



    mvn install:install-file -DgroupId=net.adoptopenjdk \
        -DartifactId=jre \
        -Dclassifier=macos-x64 \
        -Dtype=tar.gz \
        -Dversion=17.0.6 \
        -Dpackaging=tar.gz \
        -Dfile=path/to/OpenJDK17U-jre_x64_mac_hotspot_17.0.6_10.tar.gz



