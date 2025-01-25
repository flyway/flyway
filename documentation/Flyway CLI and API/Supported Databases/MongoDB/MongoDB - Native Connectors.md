---
subtitle: MongoDB - Native Connectors
---
# MongoDB - Native Connectors - Preview
- **Verified Versions:** V7,V8
- **Maintainer:** Redgate

## Supported Versions and Support Levels

{% include database-boilerplate.html %}

## Terminology
We have to map Flyway concepts and language rooted in the relational database world to MongoDB - this is how Flyway sees the mapping:

| MongoDB Concept | Flyway Concept  |
| --------------- | --------------- |
| database        | database/schema |
| collection      | table           |
| row             | document        |
| transaction     | transaction     |

## Using Flyway with MongoDB Native Connectors

- If you are using javascript migrations then you'll need [`mongosh`](https://www.mongodb.com/docs/mongodb-shell/install/) to be installed.
- There is a [tutorial available here](/tutorials/tutorial---using-native-connectors-to-connect-to-mongodb).

## Limitations

See [this blog post](https://documentation.red-gate.com/display/FD/Flyway+Native+Connectors+-+MongoDB) for more details.
