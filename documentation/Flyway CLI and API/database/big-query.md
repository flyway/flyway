---
layout: documentation
menu: big-query
subtitle: Google BigQuery
---
# Google BigQuery (Beta)

## Supported Versions

- `Latest`

## Support Level

<table class="table">
    <tr>
        <th width="25%">Compatible</th>
        <td>✅</td>
    </tr>
    <tr>
        <th width="25%">Certified</th>
        <td>⏳ Pending certification</td>
    </tr>
    <tr>
        <th width="25%">Guaranteed</th>
        <td>❌</td>
    </tr>
</table>

Support Level determines the degree of support available for this database ([learn more](/documentation/learnmore/database-support)).

## Driver

<table class="table">
<tr>
<th>URL format</th>
<td><code>jdbc:bigquery://https://www.googleapis.com/bigquery/v2:443;ProjectId=<i>project_id</i>;OAuthType=0;OAuthServiceAcctEmail=<i>service_account_name</i>;OAuthPvtKeyPath=<i>path_to_key</i>;</code></td>
</tr>
<tr>
<th>SSL support</th>
<td>No</td>
</tr>
<tr>
<th>Ships with Flyway Command-line</th>
<td>No</td>
</tr>
<tr>
<th>Maven Central coordinates</th>
<td>None. The Simba driver is available for download <a href="https://cloud.google.com/bigquery/docs/reference/odbc-jdbc-drivers" target="_blank">here</a></td>
</tr>
<tr>
<th>Supported versions</th>
<td>-</td>
</tr>
<tr>
<th>Default Java class</th>
<td><code>com.simba.googlebigquery.jdbc42.Driver</code></td>
</tr>
</table>

## Flyway Teams Features for BigQuery

### Performance

Executing multiple schema changes against GCP BigQuery is comparatively slow as it is optimized for data. You can read more about it [here](https://cloud.google.com/blog/products/data-analytics/troubleshoot-bigquery-performance-with-these-dashboards).

Flyway Teams Edition alleviates this via batching which executes multiple schema changes in one request to minimize latency and improve performance.

To enable batching follow the guide [here](/documentation/configuration/parameters/batch) for your platform. <br/>
In the Flyway Command-Line this would look like the following:

<pre class="console"><span>&gt;</span> flyway migrate -batch=true</pre>

### Data limit

Flyway Community Edition has a 10GB data limit across all datasets, and this is unlimited in Flyway Teams.

You can find out more about Flyway Teams Edition [here](/try-flyway-teams-edition/?ref=bigquery-batch).

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

If you are using the Flyway command-line, you will need to extract the contents of this archive into the `flyway/drivers/` folder.

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

To do this, open `IAM` within GCP project settings. There you can create a service account. Upon creating this, you will be given the `service_account_name` (it will look like `something@projectname.iam.gserviceaccount.com`). Upon creating this you'll have the option to download a keyfile.

The keyfile file needs to be accessible to Flyway, so save it somewhere accessible on your machine. Then configure `path_to_service_account` to point to this file.

You can learn more about service accounts [here](https://cloud.google.com/iam/docs/service-accounts).

Set this URL in the [`url`](/documentation/configuration/parameters/url) property in your Flyway configuration.

### Other configuration

Set the [`schemas`](/documentation/configuration/parameters/schemas) property in your Flyway configuration to the name of a `data set` within your BigQuery project. Set the [`user`](/documentation/configuration/parameters/user) and [`password`](/documentation/configuration/parameters/password) properties to empty in your Flyway configuration since we're authenticating using the JDBC URL i.e.

```
flyway.schemas=<your data set>
flyway.user=
flyway.password=
```
​
In a Flyway configuration file.

## Limitations

While the Simba JDBC driver supports a number of [different modes](https://simba.wpengine.com/products/BigQuery/doc/JDBC_InstallGuide/content/jdbc/bq/authenticating/useraccount.htm)
for authentication, Google User Account authentication (that is, `OAuthType=1`) is not recommended for desktop
use and is not supported at all for unattended use, or use in Docker, as it requires a browser to be available to
get an access token interactively.

## Share Your Feedback

<iframe src="https://docs.google.com/forms/d/e/1FAIpQLSep6p4N-okfCVYi7KmJhDbkfQpT6xovVcA0Lxq50BaLzFjaSg/viewform?embedded=true" width="640" height="1869" frameborder="0" marginheight="0" marginwidth="0">Loading…</iframe>
