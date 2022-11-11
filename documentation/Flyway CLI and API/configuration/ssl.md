---
layout: documentation
menu: ssl
subtitle: SSL support
redirect_from: /documentation/ssl/
---
# SSL support

Maintaining a secure connection to your database is highly desirable in a production environment, even if not already 
enforced by the database configuration. Flyway can easily be configured to
use SSL to establish a secure connection as and when required, provided the relevant database and JDBC driver also 
support SSL. Although details will vary between databases, the process to configure them is as follows:

## Obtain a copy of the database's certificate 

For on-premise databases, the certificate that you need to establish a trusted connection will have been installed 
with the database and should be obtained from the relevant administrator. For cloud services, the provider will 
publish the relevant certificate - for example [Azure](https://www.digicert.com/CACerts/BaltimoreCyberTrustRoot.crt.pem)
and [Amazon RDS](https://s3.amazonaws.com/rds-downloads/rds-combined-ca-bundle.pem).

## Import the certificate into a truststore

Your certificate is very likely already in a form in which it can be imported into a local trust store 
(for example, `.pem` or `.der`). If not, then it can be converted using a tool such as `openssl`.

To import the certificate, use the `keytool` utility. This is included as part of the Java runtime environment that 
is shipped with Flyway. If you're making a new store then you will be prompted for a password. Don't lose it - you
will need that password later!

<pre class="console"><span>&gt;</span> keytool -keystore myStorePath -alias "My database certificate" -import -file databaseCertificate.pem</pre>

If you don't specify a particular truststore then the default location is in your home directory: 
`$HOME/.keystore` (Linux) or `%USER_HOME%\.keystore` (Windows)

You can also check which certificates have been imported already:

<pre class="console"><span>&gt;</span> keytool -keystore myStorePath -list</pre>

## Make sure Java can access the truststore

You now need to configure your Java environment to be able to access these stores. This simply requires
setting a couple of [JVM properties](https://flywaydb.org/blog/jvm-properties). If you're using the Flyway 
command-line, then you can set the `JAVA_ARGS` environment variable which the command-line script reads.

<pre class="console"><span>&gt;</span> JAVA_ARGS='-Djavax.net.ssl.trustStore="myStorePath" -Djavax.net.ssl.trustStorePassword="myStorePassword"'</pre>

If you're using the Flyway Java API directly, or another tool which invokes Java, then you should add these arguments
to the place where you start the Java process:

<pre class="console"><span>&gt;</span> java -Djavax.net.ssl.trustStore="myStorePath" -Djavax.net.ssl.trustStorePassword="myStorePassword" myJavaApplication</pre>

## Configure Flyway's database connection to use SSL

Most JDBC drivers will only use SSL if explicitly instructed to do so. The way to do this differs between 
drivers, but it is usually a matter of adding optional parameters to the URL. Details for specific databases
can be found in the relevant documentation pages. For example, with Postgres, to use SSL simply requires
the `ssl` parameter:

```
flyway.url=jdbc:postgresql://postgres.flyway.test:62079/flyway_db?ssl=true
```

And you should now have secure connections to your database! If you don't want to use SSL for particular databases - 
say you need SSL for a production database, but don't need the overhead in a testing environment - then it is fine
to carry out all the above steps and then simply switch SSL on and off as required in the database URL.

## Flyway in Docker

Using SSL with the Flyway Docker image is a little more involved, as you will need to get the certificate into the
container you ultimately run. If you wish to do this, we can recommend an excellent 
[guide by Joao Rosa](https://www.joaorosa.io/2019/01/13/using-flyway-and-gitlab-to-deploy-a-mysql-database-to-aws-rds-securely/)
who follows the process through step by step.


<p class="next-steps">
    <a class="btn btn-primary" href="/documentation/configuration/authentication">Authentication<i class="fa fa-arrow-right"></i></a>
</p>

{% include trialpopup.html %}