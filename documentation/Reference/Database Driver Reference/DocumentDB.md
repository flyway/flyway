---
subtitle: AWS DocumentDB - Native Connectors
---

- **Flyway configuration:** Native Connectors only
- **Verified Versions:** `docdb 5.0.0`
- **Maintainer:** {% include redgate-badge.html %}

## Supported Versions and Support Levels
{% include database-boilerplate.html %}

## Driver

| Item                               | Details                                                                                                                                            |
| ---------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------- |
| **URL format**                     | `mongodb://ip address:port number/database_name`                                                                                                   |
| **SSL support**                    | Yes                                                                                                                                                |
| **Ships with Flyway Command-line** | JSON migrations: Yes. <br>Javascript migrations requires [Mongosh](https://www.mongodb.com/docs/mongodb-shell/install/) to be installed separately |
| **Maven Central coordinates**      | n/a                                                                                                                                                |

## Using Flyway with MongoDB Native Connectors
- If you are using javascript migrations then you'll need [`mongosh`](https://www.mongodb.com/docs/mongodb-shell/install/) to be installed. 
- There is a [tutorial available here](/tutorials/tutorial---using-native-connectors-to-connect-to-mongodb).

## Observations
AWS DocumentDB aims to be MongoDB compatible - as such we use the Flyway MongoDB infrastructure and can only verify that Flyway's verb testing works.
- We saw this error: `MongoServerError: Retryable writes are not supported` which was overcome by adding `retrywrites=false` to the connection string, See [Functional differences: Amazon DocumentDB and MongoDB](https://docs.aws.amazon.com/documentdb/latest/developerguide/functional-differences.html#functional-differences.retryable-writes) 
- We had to set up an [SSH tunnel](https://docs.aws.amazon.com/documentdb/latest/developerguide/connect-from-outside-a-vpc.html) to work on DocumentDB from outside of the AWS VPC
- TLS is enabled by default on DocumentDB and requires the [Java Truststore to be set up to](https://documentation.red-gate.com/fd/tutorial-configure-ssl-for-database-connections-275218636.html) work with AWS.

## Limitations
The [Flyway Native Connectors blog post](https://documentation.red-gate.com/display/FD/Flyway+Native+Connectors+-+MongoDB) describes more of the details of the Native Connectors changes.
- Transactions are only available with JSON migrations (these use the API and not Mongosh)
- Dry run is not available


## Terminology
We have to map Flyway concepts and language rooted in the relational database world to MongoDB - this is how Flyway sees the mapping:

| MongoDB Concept | Flyway Concept  |
| --------------- | --------------- |
| database        | database/schema |
| collection      | table           |
| row             | document        |
| transaction     | transaction     |