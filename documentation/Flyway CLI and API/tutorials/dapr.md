---
layout: documentation
menu: tut_dapr
subtitle: 'Tutorial: Integrating Dapr'
---
# Tutorial: Integrating Dapr
{% include teams.html %}

This brief tutorial will teach you **how to integrate Dapr into your Flyway process**.

## Introduction

Dapr is an application runtime which has a secrets management component, allowing you to securely store and 
provide access to sensitive information. You can learn more about it 
[here](https://docs.dapr.io/developing-applications/building-blocks/secrets/secrets-overview/).

Flyway integrates with Dapr's [Secret Store](https://docs.dapr.io/developing-applications/building-blocks/secrets), 
letting you securely store and provide access to any confidential Flyway parameters.

This tutorial will assume you already have a Dapr server instance and know how to configure secrets in it,
and a local installation of the Dapr application sidecar. 

## Configuring Flyway to access Dapr

There are two new parameters to configure in Flyway in order to set up the Dapr integration:

### [`dapr.url`](/documentation/configuration/parameters/daprUrl)

This is the REST API URL of your Dapr application sidecar. Typically you will initialise the sidecar with a command 
such as `dapr run --app-id my-app --dapr-http-port 3500` where you specify the port the sidecar will listen to. 
The Secret Store REST API is then accessible at `http://localhost:3500/v1.0/secrets/my-secrets-store` where
`my-secrets-store` is the specific store name. The API is the same regardless of the underlying storage mechanism.

### [`dapr.secrets`](/documentation/configuration/parameters/daprSecrets)

This is a comma-separated list of secrets in Dapr Secret Store which Flyway should try to read from. 

The value of each secret must be structured like a Flyway configuration file. For example, if we wanted to store a 
database password in a secret we would give the secret `flyway.password=<database_password>` as its value.

## Testing the integration

Our example will assume that we have:

- A Dapr application sidecar running on `http://localhost:3500/v1.0/`
- A secret in a store `my-secrets-store` with name `my-flyway-config` and the following contents:

```
flyway.url=<database_url>
flyway.user=<database_user>
flyway.password=<database_password>
```

If we now execute the following Flyway command: 

```
flyway info -plugins.dapr.url="http://localhost:3500/v1.0/secrets/my-secrets-store" -plugins.dapr.secrets="my-flyway-config"
```

Flyway will connect to your database without needing the database credentials to be provided in plaintext. 
Instead, Flyway will read in the specified secret and use its value to configure the database credentials and 
display the overview of the schema history table that results from [`info`](/documentation/command/info).

## Summary

In this brief tutorial we saw how to:

- Integrate Dapr Secret Store into Flyway to securely store and provide access to any confidential Flyway parameters
