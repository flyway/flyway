---
subtitle: CosmosDB - Native Connectors
---
# CosmosDB
- **Status:** Preview
- **Flyway configuration:** Native Connectors only
- **Verified Versions:** 7.0
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
- Whilst this is in preview, you will need to set the environment variable `FLYWAY_NATIVE_CONNECTORS=true` on Redgate editions of Flyway.
- If you are using javascript migrations then you'll need [`mongosh`](https://www.mongodb.com/docs/mongodb-shell/install/) to be installed.
- There is a [tutorial available here](/tutorials/tutorial---using-native-connectors-to-connect-to-mongodb).

## Observations
CosmosDB aims to be MongoDB compatible - as such we use the Flyway MongoDB infrastructure and can only verify that Flyway's verb testing works.
- We saw this error: `The request did not complete due to a high rate of metadata requests.` which was overcome by enabling `Server Side Retry` on the `Settings/Features` page on the CosmosDB management tool.

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