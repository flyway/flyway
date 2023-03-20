---
subtitle: Google Cloud Spanner
---
# Google Cloud Spanner (Beta)

## Supported Versions

- `Latest`

## Support Level

<table class="table">
    <tr>
        <th width="25%">Compatible</th>
        <td>&#10003;</td>
    </tr>
    <tr>
        <th width="25%">Certified</th>
        <td>&#9203; Pending certification</td>
    </tr>
    <tr>
        <th width="25%">Guaranteed</th>
        <td>&#10060;</td>
    </tr>
</table>

## Driver

<table class="table">
<tr>
<th>URL format</th>
<td><code>jdbc:cloudspanner:/projects/<i>project_name</i>/instances/<i>instance_name</i>/databases/<i>database_name</i>?credentials=<i>path/to/keyfile.json</i></code></td>
</tr>
<tr>
<th>SSL support</th>
<td>No</td>
</tr>
<tr>
<th>Ships with Flyway Command-line</th>
<td>Yes</td>
</tr>
<tr>
<th>Maven Central coordinates</th>
<td><code>com.google.cloud:google-cloud-spanner-jdbc</code></td>
</tr>
<tr>
<th>Supported versions</th>
<td><code>2.2.6</code> and later</td>
</tr>
<tr>
<th>Default Java class</th>
<td><code>com.google.cloud.spanner.jdbc.JdbcDriver</code></td>
</tr>
</table>

Support Level determines the degree of support available for this database ([learn more](Learn More/Database Support Levels)).

## Flyway Teams Features for Cloud Spanner

Executing multiple schema changes against Cloud Spanner is comparatively slow due to its need to validate your data. You can read more about it [here](https://cloud.google.com/spanner/docs/schema-updates#performance).

Flyway Teams Edition alleviates this via batching which executes multiple schema changes in one request to minimize latency and improve performance.

To enable batching follow the guide [here](Configuration/parameters/batch) for your platform. <br/>
In the Flyway Command-Line this would look like the following:

<pre class="console"><span>&gt;</span> flyway migrate -batch=true</pre>

You can find out more about Flyway Teams Edition [here](/https://flywaydb.org/download/?ref=cloud-spanner-batch).

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
​
- `project_name`
- `instance_name`
- `database_name`
- A path to a `keyfile.json` for authentication

`project_name`, `instance_name`, `database_name` can all be found on the Cloud Spanner web interface. For authentication, we recommend using the 'keyfile'. This requires creating a service account for Cloud Spanner.

To do this, open `IAM` within GCP project settings. There you can create a service account. Upon creating this you'll have the option to download the keyfile.

The authentication file needs to be accessible to Flyway, so save it somewhere accessible on your machine. Then configure `path_to_service_account` to point to this file.

You can learn more about service accounts [here](https://cloud.google.com/iam/docs/service-accounts).

Set this URL in the [`url`](Configuration/parameters/url) property in your Flyway configuration.
​
### Other configuration

Set the [`user`](Configuration/parameters/user) and [`password`](Configuration/parameters/password) properties to empty in your Flyway configuration since we're authenticating using the JDBC URL i.e.

```
flyway.user=
flyway.password=
```

In a Flyway configuration file.
