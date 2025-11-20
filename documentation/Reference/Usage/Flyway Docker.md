---
subtitle: Flyway Docker
---

## Flyway Docker Image
In addition to the base and Alpine Flyway images, optional Azure and Mongo variants are provided.
The Azure images include additional tooling for running Flyway on hosted Azure DevOps agents, whilst the Mongo variant bundles `mongosh` for native connectors.

You can find the [Dockerfiles](https://github.com/flyway/flyway/tree/main/flyway-docker/dockerfiles) used for our official Flyway images in the open-source repository.
### Redgate Edition
The official Flyway docker image for community, teams, and enterprise editions can be found [here](https://hub.docker.com/r/redgate/flyway).

### Open Source Edition
The open source Flyway image can be found [here]((https://hub.docker.com/r/flyway/flyway)).

## Custom Docker Image
If you are building your own docker image, you will need to ensure the following environmental dependencies are met.

Redgate's comparison engines are built with .NET hence require additional dependencies which may or may not be bundled in your base image.
These are necessary if you wish to use Flyway's advanced capabilities.

### Base
```
libicu74
libgcc1
libstdc++6
libssl3
zlib1g
liblttng-ust1
libunwind8

libgssapi-krb5-2

python3-pip
```
### Alpine
```
icu-libs
libgcc
libstdc++
libssl3
zlib
libintl

krb5-libs

py3-pip

bash
```