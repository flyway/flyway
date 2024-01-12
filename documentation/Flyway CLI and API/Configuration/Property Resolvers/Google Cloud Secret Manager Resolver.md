---
subtitle: Google Cloud Secret Manager
---
# Google Cloud Secret Manager
{% include commandlineonly.html %}

Google Cloud Secrets Manager is configured by setting [Google Cloud Secret Manager Project](Configuration/Parameters/Flyway/Google Cloud Secret Manager Project).
Values can be loaded retrieved from the project using `${googlesecrets.key}`, for example:
```TOML
[flyway.gcsm]
project = "quixotic-ferret-345678"

[environments.default]
url = "jdbc:postgresql:${googlesecrets.dbhost}/${googlesecrets.dbname}"
user = "${googlesecrets.username}"
password = "${googlesecrets.password}"
```