---
layout: documentation
menu: secretsManagement
subtitle: Secrets Management
redirect_from: /documentation/awsSecretsManager/
---
# Secrets Management
{% include enterprise.html %}

A problem that organizations often encounter is where to store and how to access sensitive data such as the credentials for connecting to a database or their Flyway license key.

Flyway comes with support for the following secrets management solutions that enable you to successfully handle sensitive data:

- [AWS Secrets Manager](/documentation/configuration/secretsManagement#aws-secrets-manager)
- [Dapr Secret Store](/documentation/configuration/secretsManagement#dapr-secret-store)
- [Google Cloud Secret Manager](/documentation/configuration/secretsManagement#google-cloud-secret-manager)
- [Vault](/documentation/configuration/secretsManagement#hashicorp-vault)

## AWS Secrets Manager

[AWS Secrets Manager](https://aws.amazon.com/secrets-manager) offers a solution to the problem of handling database credentials. Secrets such as usernames and passwords can be stored in the Secrets Manager, and then accessed via an id known to authorized users. This keeps sensitive credentials out of application configuration.

### Driver
<table class="table">
<tr>
<th>Ships with Flyway Command-line</th>
<td>No</td>
</tr>
<tr>
<th>Maven Central coordinates</th>
<td><code>com.amazonaws.secretsmanager:aws-secretsmanager-jdbc:1.0.5</code></td>
</tr>
<tr>
<th>Supported versions</th>
<td><code>1.0.5</code> and later</td>
</tr>
</table>

### Supported databases
Secrets Manager support is currently provided by the [AWS Secrets Manager JDBC Library](https://github.com/aws/aws-secretsmanager-jdbc) for the following databases:
- MariaDB
- MySQL
- Oracle
- PostgreSQL
- SQL Server

### Configuring Flyway
To make Flyway pull credentials from the Secrets Manager, you need to perform the following steps:
- Ensure the AWS CLI is installed and configured to be able to access the Secrets Manager.
- Add the driver to your project dependencies, or add it to the drivers folder if using the CLI.
- If you've specified the driver class manually using `flyway.driver` then remove this configuration property.
- Modify your connection URL to replace `jdbc:` with `jdbc-secretsmanager:`.
  - e.g. `jdbc:mariadb://localhost:1234/example_db` -> `jdbc-secretsmanager:mariadb://localhost:1234/example_db`
- Change the `flyway.user` configuration property to contain the secret id.
- Remove the `flyway.password` configuration property.

Now you can run `migrate`, `info`, etc. and the credentials will be pulled out of the Secrets Manager.

## Dapr Secret Store

Flyway integrates with [Dapr's](https://docs.dapr.io/developing-applications/building-blocks/secrets/secrets-overview/) Secret Store in order to allow users to store Flyway configuration parameters securely. This can be used to securely read license keys without storing them in application configuration, and other configuration parameters can also be stored and read such as your database password or Flyway placeholders.

Parameters stored in secrets in Dapr are read with the highest priority and will override all other configurations.

### Example

Assume we have the following two secrets in Dapr:
- `secret1` which contains `flyway.url=jdbc:h2:mem:db`
- `secret2` which contains `flyway.user=sa`

In order to read these secrets you need to configure just the following Flyway parameters:
- [flyway.plugins.dapr.url](/documentation/configuration/parameters/daprUrl) - This is the REST API URL of your Dapr sidecar application e.g. `http://localhost:3500/v1.0/secrets/my-secrets-store`
- [flyway.plugins.dapr.secrets](/documentation/configuration/parameters/daprSecrets) - This is a comma-separated list of paths to secrets that contain Flyway configurations. In our case we would set `flyway.plugins.dapr.secrets=secret1,secret2`

After configuring the above parameters, we would be able to connect to a database in Flyway without configuring a database connection locally, as all the necessary configuration would be read from Dapr.

## Google Cloud Secret Manager

Flyway integrates with [Google Cloud Secret Manager](https://cloud.google.com/secret-manager/) (GCSM) in order to allow users to store Flyway configuration parameters securely. This can be used to securely read license keys without storing them in application configuration, and other configuration parameters can also be stored and read such as your database password or Flyway placeholders.

Parameters stored in secrets in GCSM are read with the highest priority and will override all other configurations.

### Example

Assume we have the following two secrets in GCSM:
- `secret1` which contains `flyway.url=jdbc:h2:mem:db`
- `secret2` which contains `flyway.user=sa`

In order to read these secrets you need to configure just the following Flyway parameters:
- [flyway.plugins.gcsm.project](/documentation/configuration/parameters/gcsmProject) - This is the name of your GCSM project e.g. `quixotic-ferret-345678`
- [flyway.plugins.gcsm.secrets](/documentation/configuration/parameters/gcsmSecrets) - This is a comma-separated list of paths to secrets that contain Flyway configurations. In our case we would set `flyway.plugins.gcsm.secrets=secret1,secret2`

After configuring the above parameters, we would be able to connect to a database in Flyway without configuring a database connection locally, as all the necessary configuration would be read from GCSM.

## HashiCorp Vault

Flyway integrates with [Vault's](https://www.vaultproject.io/) key-value engine in order to allow users to store Flyway configuration parameters securely. This can be used to securely read license keys without storing them in application configuration, and other configuration parameters can also be stored and read such as your database password or Flyway placeholders.

Parameters stored in secrets in Vault are read with the highest priority and will override all other configurations.

### Example

Assume we have the following two secrets in Vault:
- `test/1/config` which contains `flyway.url=jdbc:h2:mem:db` and uses key-value engine V1
- `test/2/config` which contains `flyway.user=sa` and uses key-value engine V2

In order to read these secrets you need to configure just the following Flyway parameters:
- [flyway.plugins.vault.url](/documentation/configuration/parameters/vaultUrl) - This is the REST API URL of your Vault server e.g. `http://localhost:8200/v1/`
- [flyway.plugins.vault.token](/documentation/configuration/parameters/vaultToken) - This is the Vault token required to access your secrets e.g. `s.abcdefghijklmnopqrstuvwx`
- [flyway.plugins.vault.secrets](/documentation/configuration/parameters/vaultSecrets) - This is a comma-separated list of paths to secrets that contain Flyway configurations. This must start with the name of the engine and end with the name of the secret. In our case we would set `flyway.plugins.vault.secrets=kv/test/1/config,kv/data/test/2/config`

After configuring the above parameters, we would be able to connect to a database in Flyway without configuring a database connection locally, as all the necessary configuration would be read from Vault.

<p class="next-steps">
    <a class="btn btn-primary" href="/documentation/configuration/placeholder">Placeholders<i class="fa fa-arrow-right"></i></a>
</p>
