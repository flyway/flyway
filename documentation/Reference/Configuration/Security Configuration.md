---
subtitle: Security configuration
---


# How SSL/TLS is configured with Flyway
## Java Configuration
Java assumes some configuration is in place to allow it to use SSL/TLS. This is setup with environment variables that form part of the `JAVA_ARGS` parameter.
 
## Context
The tools used to manage cryptographic stores are the same (`keytool` and `openssl`) but Java differentiates the stores based on their purpose.

### Trust store 
Used to store the (public) certificates of trusted entities. Determines whether the remote authentication credentials, and thus the connection, should be trusted.

| JAVA_ARGS value                    | Purpose                                                                        |
| ---------------------------------- | ------------------------------------------------------------------------------ |
| `javax.net.ssl.trustStore`         | the path to a Truststore containing the certificates of the signing authority |
| `javax.net.ssl.trustStorePassword` | the password to access the Truststore defined in `javax.net.ssl.trustStore`   |

If no trust store is explicitly defined then the following locations will be checked:
- `$JAVA_HOME/lib/security/jssecacerts`
- `$JAVA_HOME/lib/security/cacerts`
- `HOME/.keystore` (Linux)
- `%USER_HOME%\.keystore` (Windows)

### Key store
Used to store your keys and certificates to allow the remote party to authenticate you.

| JAVA_ARGS value                  | Purpose                                                                    |
| -------------------------------- | -------------------------------------------------------------------------- |
| `javax.net.ssl.keyStore`         | the path to a key store containing the client's TLS/SSL certificates       |
| `javax.net.ssl.keyStorePassword` | the password to access the Truststore defined in `javax.net.ssl.keyStore` |

## Native Connectors
Where you are using a native connector driver you will need to provide the relevant connection details as part of the connection string and you may need to provide similar information in two ways - once for Flyway's API driver and once for the vendor native tooling. 

The exact mechanism will vary for each database but we'll look at enabling TLS with MongoDB as an example.


### Prerequisites
- We assume your database instance uses a certificate that is signed by an authority that is not present in the JRE's default certificate store but you have been provided with the certificate in a file called `my_certificates.pem`.
- We assume the database is only authenticating the client through username/password so no Keystore needs to be set up. 

### 1. Setup for Flyway API access

This will involve configuring the Java Truststore and setting the `JAVA_ARGS` parameters `javax.net.ssl.trustStore` and `javax.net.ssl.trustStorePassword`.

How the Truststore file is created will depend on who is providing the certificates and in what format they are originally. 

You can find some reference material here:
- [Oracle `Keytool` reference](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/keytool.html)
- [Connecting programmatically to Amazon DocumentDB](https://docs.aws.amazon.com/documentdb/latest/developerguide/connect_programmatically.html) - Look in the Java programming language section for how to convert AWS's CA file to Java Truststore compatible format. It's more complicated that you would hope because `keytool` will only process a single certificate at a time.

The steps would look like this:
- Use `keytool` to convert `my_certificates.pem` into `/tmp/certs/my_certificates.jks`
- Set the environment variable `JAVA_ARGS` to include `-Djavax.net.ssl.trustStore=/tmp/certs/my_certificates.jks` and `-Djavax.net.ssl.trustStorePassword=<certstore-password>`

_Note: JKS is an older format, PKCS12 supersedes it but should work just the same._

### 2. Setup for Mongosh access

Assuming you are using Javascript migrations then Flyway will use the Mongosh native tooling to deploy these. In order to configure Mongosh, all the information it needs will also need to be in the connection string. This is because:
- Mongosh doesn't use the java Keystore and Truststore
- Flyway can only pass configuration to Mongosh via the connection string. 

Where you would typically invoke Mongosh on the command line with parameters like this:

`mongosh "mongodb://localhost:27017" --tls --tlsCAFile="/tmp/certs/my_certificates.pem"`

This is the functionally equivalent command using a connection string:

`mongosh "mongodb://localhost:27017/?tls=true&tlsCAFile=/tmp/certs/my_certificates.pem"`

You can find reference information about the [Mongo connection string parameters here](https://www.mongodb.com/docs/manual/reference/connection-string-options/)

The connection string is passed to Flyway as the URL parameter:

```
[environments.tls-mongodb]
   url = "mongodb://localhost:27017/?tls=true&retryWrites=false&tlsCAFile=/tmp/certs/my_certificates.pem"
   user = "your username"
   password = "your password"
```

## Truststore and Keystore auto-configuration 

{% include enterprise.html %}

This feature is available only in Native Connectors mode with MongoDB. 

As highlighted in the previous section, the MongoDB Java Driver in Native Connectors requires Java Keystore/Truststore, while Mongosh relies on tlsCertKeyFile/tlsCAFile, which must be provided as parameters in the connection string. To simplify configuration, Flyway automatically provisions the Truststore/Keystore based on the tlsCAFile/tlsCertificateKeyFile connection string parameters.

_Note_
- This feature can be turned off by setting environment variable `FLYWAY_SSL_AUTOCONFIGURATION` to false.
- Flyway will skip the auto-configuration if custom Keystore/Truststore `JAVA_ARGS` settings are detected. 

## References
- [Differences between a Keystore and a Truststore](https://www.baeldung.com/java-keystore-truststore-difference)
- [MongoDB - Enable TLS/SSL on a Connection](https://www.mongodb.com/docs/drivers/java/sync/current/fundamentals/connection/tls/#std-label-tls-ssl)