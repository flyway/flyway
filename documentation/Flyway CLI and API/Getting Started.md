---
menu: gs_overview
title: Getting Started
redirect_from: /getStarted/
---
# Getting Started

Welcome to **Flyway**, database migrations made easy.

* [Why Database Migrations](https://documentation.red-gate.com/display/FD/Why+database+migrations)
* [How Flyway Works](https://documentation.red-gate.com/display/FD/Quickstart+-+How+Flyway+Works)

Ready to get started? Take a **5 minute** tutorial:

## Command Line

For non-JVM users and environments without build tools

`> flyway migrate -url=... -user=... -password=...`

* {% include quickstart-cli.html %}
* [Quickstart: Docker](https://documentation.red-gate.com/display/FD/Quickstart+-+Docker)

## API Usage

Migrate directly from within your application

```
Flyway flyway = Flyway.configure().dataSource(url, user, password).load();
flyway.migrate();
```

[Quickstart: API](https://documentation.red-gate.com/display/FD/Quickstart+-+API)

## Maven usage

Seamless integration with Maven 2/3 builds

`> mvn flyway:migrate -Dflyway.url=... -Dflyway.user=... -Dflyway.password=...`

{% include quickstart-maven.html %}

## Gradle usage

Seamless integration with Gradle builds

`> gradle flywayMigrate -Dflyway.url=... -Dflyway.user=... -Dflyway.password=...`

[Quickstart: Gradle](https://documentation.red-gate.com/display/FD/Quickstart+-+Gradle)

