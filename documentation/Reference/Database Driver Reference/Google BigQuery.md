---
subtitle: Google BigQuery
---

- **Verified Versions:** Latest
- **Maintainer:** {% include redgate-badge.html %}

## Supported Versions and Support Levels

{% include database-boilerplate.html %}

## Driver

| Item                               | Details                                                                                                                                                               |
| ---------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **URL format**                     | `jdbc:bigquery://https://www.googleapis.com/bigquery/v2:443;ProjectId=project_id;OAuthType=0;OAuthServiceAcctEmail=service_account_name;OAuthPvtKeyPath=path_to_key;` |
| **SSL support**                    | No                                                                                                                                                                    |
| **Ships with Flyway Command-line** | No                                                                                                                                                                    |
| **Maven Central coordinates**      | None. The Simba driver is available for download [here](https://cloud.google.com/bigquery/docs/reference/odbc-jdbc-drivers)                                           |
| **Supported versions**             | -                                                                                                                                                                     |
| **Default Java class**             | `com.simba.googlebigquery.jdbc42.Driver`                                                                                                                              |

## Terminology
We have to map Flyway concepts to Google Big Query - this is how Flyway sees the mapping:

| Big Query Concept | Flyway Concept |
|-------------------|----------------|
| dataset           | schema         |

### Performance

Executing multiple schema changes against GCP BigQuery is comparatively slow as it is optimized for data. You can read more about
it [here](https://cloud.google.com/blog/products/data-analytics/troubleshoot-bigquery-performance-with-these-dashboards).

Flyway alleviates this via batching which executes multiple schema changes in one request to minimize latency and improve performance.

To enable batching follow the guide [here](<Configuration/Flyway Namespace/Flyway batch Setting>)for your platform. <br/>
In the Flyway Command-Line this would look like the following:

<pre class="console"><span>&gt;</span> flyway migrate -batch=true</pre>

### Data limit

Flyway Community Edition has a 10GB data limit across all datasets, and this is unlimited in {% include teams.html %}

## Using Flyway with Google BigQuery

### Pre-requisites

- Using Flyway with Maven?
    - Include the Flyway GCP BigQuery dependency [here](https://mvnrepository.com/artifact/org.flywaydb/flyway-gcp-bigquery) in your pom
- Using Flyway with Gradle?
    - Include the Flyway GCP BigQuery dependency [here](https://mvnrepository.com/artifact/org.flywaydb/flyway-gcp-bigquery) as a buildscript dependency

### Installing dependencies

Google BigQuery requires a number of dependencies to be installed manually.

Go to [Google's documentation](https://cloud.google.com/bigquery/docs/reference/odbc-jdbc-drivers#current_jdbc_driver_release_12161020) and download the JDBC driver.

You will get a zip archive with many JARs inside.

If you are using the Flyway command-line, you will need replace the
`flyway/drivers/` folder with the contents of this archive.

If you are using the Flyway Maven plugin, you will need to add the contents of this archive to your classpath.
​

### Configuring Flyway

​
This is a JDBC URL that points to your database. You can configure a connection using this sample URL as an example:

`jdbc:bigquery://https://www.googleapis.com/bigquery/v2:443;ProjectId=<project_id>;OAuthType=0;OAuthServiceAcctEmail=<service_account_name>;OAuthPvtKeyPath=<path_to_service_account>;`

We need to fetch three things to complete this URL:
​

- `project_id`
- `service_account_name`
- `path_to_service_account`

`project_id` is the name of your BigQuery project within GCP.

To get `service_account_name` and `path_to_service_account`, you'll need to create a 'service account' for your Flyway connections.

To do this, open `IAM` within GCP project settings. There you can create a service account. Upon creating this, you will be given the `service_account_name` (it will look
like `something@projectname.iam.gserviceaccount.com`). Upon creating this you'll have the option to download a keyfile.

The keyfile file needs to be accessible to Flyway, so save it somewhere accessible on your machine. Then configure `path_to_service_account` to point to this file.

You can learn more about service accounts [here](https://cloud.google.com/iam/docs/service-accounts).

Set this URL in the [`url`](<Configuration/Environments Namespace/Environment url Setting>) property in your Flyway configuration.

### Other configuration

Set the [`schemas`](<Configuration/Environments Namespace/Environment schemas Setting>) property in your Flyway configuration to the name of a `data set` within your BigQuery project.
Set the [`user`](<Configuration/Environments Namespace/Environment user Setting>) and [`password`](<Configuration/Environments Namespace/Environment password Setting>) properties to empty in your Flyway configuration since we're authenticating using the JDBC URL
i.e.
```
flyway.schemas=<your data set>
flyway.user=
flyway.password=
```

In a Flyway configuration file.

## Limitations

While the Simba JDBC driver supports a number
of [different modes](https://cloud.google.com/bigquery/docs/reference/odbc-jdbc-drivers)
for authentication, Google User Account authentication (that is, `OAuthType=1`) is not recommended for desktop
use and is not supported at all for unattended use, or use in Docker, as it requires a browser to be available to
get an access token interactively.
