---
layout: documentation
menu: tut_vault
subtitle: 'Tutorial: Integrating Vault'
---
# Tutorial: Integrating Vault
{% include enterprise.html %}

This brief tutorial will teach you **how to integrate Vault into your Flyway process**.

## Introduction

HashiCorp Vault is a secrets management solution, allowing you to securely store and provide access to sensitive information. You can learn more about it [here](https://www.vaultproject.io/).

Flyway integrates with Vault's [key-value secret store](https://www.vaultproject.io/docs/secrets/kv), letting you securely store and provide access to any confidential Flyway parameters for any specified duration.

This tutorial will assume you already have a Vault instance and know how to configure secrets in it. For more information on configuring secrets in Vault along with a tutorial on using Flyway, see [this blog post](/blog/integrating-vault-to-secure-flyway-parameters).

## Configuring Flyway to access Vault

There are three new parameters to configure in Flyway in order to set up the Vault integration:

### [`vault.url`](/documentation/configuration/parameters/vaultUrl)

This is the REST API URL of your Vault server, and should include the API version.<br/>
_Note: Flyway currently only supports API version v1_

If you are using a [Vault dev server](https://learn.hashicorp.com/tutorials/vault/getting-started-dev-server) then an example of what configuring this in Flyway may look like is:<br/>
`flyway.plugins.vault.url=http://localhost:8200/v1/`

### [`vault.token`](/documentation/configuration/parameters/vaultToken)

This is the token required to access your secrets. You can read about generating tokens [here](https://www.vaultproject.io/docs/commands/token/create), including how to add a lifetime to your token in order to control the duration of its validity.

If we have a token `<vault_token>` then configuring this parameter involves adding the following to our Flyway configuration:<br/>
`flyway.plugins.vault.token=<vault_token>`

### [`vault.secrets`](/documentation/configuration/parameters/vaultSecrets)

This is a comma-separated list of secrets in Vault which Flyway should try to read from. Each secret must include the path to the secret, and must also start with the secret engine's name. The resulting form is:<br/>
`<engine_name>/<path>/<to>/<secret_name>`

An example secret would be `secret/data/flyway/testConfiguration` where `secret/data` is the V2 engine's name, `flyway` is the path and `testConfiguration` is the secret name.

The value of each secret must be structured like a Flyway configuration file. For example, if we wanted to stored a database password in a secret we would give the secret `flyway.password=<database_password>` as its value.

## Testing the integration

Our example will assume that we have:

- A Vault dev server running on `http://localhost:8200/v1/`
- A Vault token `<vault_token>`
- A secret in a V2 key-value engine `secret` with path `flyway` called `flyway_credentials` with the following contents:

```
flyway.url=<database_url>
flyway.user=<database_user>
flyway.password=<database_password>
```

If we now execute the following Flyway command: 

```
flyway info -plugins.vault.url="http://localhost:8200/v1/" -plugins.vault.token="<vault_token>" -plugins.vault.secrets="secret/data/flyway/flyway_credentials"
```

Flyway will connect to your database without needing the database credentials to be provided in plaintext. Instead, Flyway will read in the specified secret and use its value to configure the database credentials and display the overview of the schema history table that results from [`info`](/documentation/command/info).

## Summary

In this brief tutorial we saw how to:

- Integrate Vault into Flyway to securely store and provide access to any confidential Flyway parameters
