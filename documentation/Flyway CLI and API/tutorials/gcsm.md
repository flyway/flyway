---
layout: documentation
menu: tut_gcsm
subtitle: 'Tutorial: Integrating Google Cloud Secret Manager'
---
# Tutorial: Integrating Google Cloud Secret Manager
{% include teams.html %}

This brief tutorial will teach you **how to integrate Google Cloud Secret Manager into your Flyway process**.

## Introduction

Google Cloud Secret Manager (GCSM) is a cloud service for secrets management, allowing you to securely store and
provide access to sensitive information. You can learn more about it
[here](https://cloud.google.com/secret-manager). Flyway integrates with GCSM,
letting you securely store and provide access to any confidential Flyway parameters.

This tutorial will assume you already have a GCSM project and know how to configure secrets in it.
if you haven't used GCSM before, follow this [tutorial](https://cloud.google.com/secret-manager/docs/quickstart)
for creating a project containing some secrets.

## Configuring Flyway to access GCSM

There are two new parameters to configure in Flyway in order to set up the GCSM integration:

### [`gcsm.project`](/documentation/configuration/parameters/gcsmProject)

This is the name of the project you have created that contains the secrets.

### [`gcsm.secrets`](/documentation/configuration/parameters/gcsmSecrets)

This is a comma-separated list of secrets in Google Cloud Secret Manager which Flyway should try to read from.

The value of each secret must be structured like a Flyway configuration file. For example, if we wanted to store a
database password in a secret we would give the secret `flyway.password=<database_password>` as its value.

## Testing the integration

Our example will assume that we have:

- A secret in a project `quixotic-ferret-345678` with name `my-flyway-config` and the following contents:

```
flyway.url=<database_url>
flyway.user=<database_user>
flyway.password=<database_password>
```

- Any necessary Google Cloud authentication - eg. a credentials file and environment variable pointing to it.

If we now execute the following Flyway command:

```
flyway info -plugins.gcsm.project="quixotic-ferret-345678" -plugins.gcsm.secrets="my-flyway-config"
```

Flyway will connect to your database without needing the database credentials to be provided in plaintext.
Instead, Flyway will read in the specified secret and use its value to configure the database credentials and
display the overview of the schema history table that results from [`info`](/documentation/command/info).

## Summary

In this brief tutorial we saw how to:

- Integrate Google Cloud Secret Manager into Flyway to securely store and provide access to any confidential Flyway parameters
