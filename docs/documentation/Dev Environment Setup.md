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

Flyway is built to language level 17 with JDK 21.

So grab the latest version from the [AdoptOpenJDK website](https://adoptopenjdk.net/releases.html?variant=OpenJDK21).

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




