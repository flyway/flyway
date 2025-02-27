---
subtitle: Google Cloud Spanner
---

- **Verified Versions:** Latest
- **Maintainer:** {% include redgate-badge.html %}

## Supported Versions and Support Levels

{% include database-boilerplate.html %}

## Driver

| Item                               | Details                                                                                                                                                                                                                                                                   |
| ---------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **URL format**                     | `jdbc:cloudspanner:/projects/project_name/instances/instance_name/databases/database_name?credentials=path/to/keyfile.json` <br>**Connecting to an emulator:**  <br>`jdbc:cloudspanner://host:9020/projects/project_name/instances/instance_name/databases/database_name` |
| **SSL support**                    | Yes                                                                                                                                                                                                                                                                       |
| **Ships with Flyway Command-line** | Yes                                                                                                                                                                                                                                                                       |
| **Maven Central coordinates**      | `com.google.cloud:google-cloud-spanner-jdbc`                                                                                                                                                                                                                              |
| **Supported versions**             | `2.2.6` and later                                                                                                                                                                                                                                                         |
| **Default Java class**             | `com.google.cloud.spanner.jdbc.JdbcDriver`                                                                                                                                                                                                                                |

### Performance

Executing multiple schema changes against Cloud Spanner is comparatively slow due to its need to validate your data. You can read more about it [here](https://cloud.google.com/spanner/docs/schema-updates#performance).

Flyway alleviates this via batching which executes multiple schema changes in one request to minimize latency and improve performance.

To enable batching follow the guide [here](<Configuration/Flyway Namespace/Flyway Batch Setting>) for your platform.

In the Flyway Command-Line this would look like the following:

<pre class="console"><span>&gt;</span> flyway migrate -batch=true</pre>

### Data limit

Flyway Community Edition has a 10GB limit on database size, and this is unlimited in Flyway Teams and Enterprise.

You can find out more about Flyway Teams Edition [here](https://www.red-gate.com/products/flyway/teams).

## Using Flyway with Google Cloud Spanner

### Pre-requisites

- Using Flyway with Maven?
    - Include the latest Flyway GCP Spanner dependency [here](https://mvnrepository.com/artifact/org.flywaydb/flyway-gcp-spanner) in your pom
- Using Flyway with Gradle?
    - Include the latest Flyway GCP Spanner dependency [here](https://mvnrepository.com/artifact/org.flywaydb/flyway-gcp-spanner) as a buildscript dependency

### Configuring Flyway

You must configure a JDBC URL that points to your database. You can configure a connection using this sample URL as an example:

`jdbc:cloudspanner:/projects/<project_name>/instances/<instance_name>/databases/<database_name>?credentials=<path/to/keyfile.json>`

We need to fetch three things to complete this url:

- `project_name`
- `instance_name`
- `database_name`
- A path to a `keyfile.json` for authentication (not required when connected to an emulated session)

`project_name`, `instance_name`, `database_name` can all be found on the Cloud Spanner web interface. For authentication, we recommend using the 'keyfile'. This requires creating a service account for Cloud Spanner.

To do this, open `IAM` within GCP project settings. There you can create a service account. Upon creating this you'll have the option to download the keyfile.

The authentication file needs to be accessible to Flyway, so save it somewhere accessible on your machine. Then configure `path_to_service_account` to point to this file.

You can learn more about service accounts [here](https://cloud.google.com/iam/docs/service-accounts).

Set this URL in the [`url`](<Configuration/Environments Namespace/Environment url Setting>) property in your Flyway configuration.

### Other configuration

Set the [`user`](<Configuration/Environments Namespace/Environment user Setting>) and [`password`](<Configuration/Environments Namespace/Environment password Setting>) properties to empty in your Flyway configuration (conf or TOML) since we're authenticating using the JDBC URL

