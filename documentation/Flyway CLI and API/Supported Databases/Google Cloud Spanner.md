---
subtitle: Google Cloud Spanner
---
# Google Cloud Spanner
- **Verified Versions:** Latest
- **Maintainer:** Redgate
## Supported Versions and Support Levels
For information regarding the supported version and support levels available,
please see [Supported Databases for Flyway](https://documentation.red-gate.com/flyway/learn-more-about-flyway/system-requirements/supported-databases-for-flyway)

For information regarding the Flyway features available, please see [Flyway feature glossary](https://documentation.red-gate.com/flyway/learn-more-about-flyway/feature-glossary)

## Driver

| Item                               | Details                                                                                                                                                             |
|------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **URL format**                     | <code>jdbc:cloudspanner:/projects/<i>project_name</i>/instances/<i>instance_name</i>/databases/<i>database_name</i>?credentials=<i>path/to/keyfile.json</i></code> <br>**Connecting to an emulator:**  <br><code>jdbc:cloudspanner://<i>host</i>:9020/projects/<i>project_name</i>/instances/<i>instance_name</i>/databases/<i>database_name</i></code> |
| **SSL support**                    | Yes                                                                                                                                                                 |
| **Ships with Flyway Command-line** | Yes                                                                                                                                                                 |
| **Maven Central coordinates**      | `com.google.cloud:google-cloud-spanner-jdbc`                                                                                                                        |
| **Supported versions**             | `2.2.6` and later                                                                                                                                                   |
| **Default Java class**             | `com.google.cloud.spanner.jdbc.JdbcDriver`                                                                                                                          |

## Flyway Teams Features for Cloud Spanner

Executing multiple schema changes against Cloud Spanner is comparatively slow due to its need to validate your data. You can read more about it [here](https://cloud.google.com/spanner/docs/schema-updates#performance).

Flyway Teams Edition alleviates this via batching which executes multiple schema changes in one request to minimize latency and improve performance.

To enable batching follow the guide [here](Configuration/parameters/flyway/batch) for your platform.

In the Flyway Command-Line this would look like the following:

<pre class="console"><span>&gt;</span> flyway migrate -batch=true</pre>

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

Set this URL in the [`url`](Configuration/parameters/environments/url) property in your Flyway configuration.

### Other configuration

Set the [`user`](Configuration/parameters/environments/user) and [`password`](Configuration/parameters/environments/password) properties to empty in your Flyway configuration (conf or toml) since we're authenticating using the JDBC URL

