---
subtitle: 'Tutorial: Placeholders'
---

{% include enterprise.html %}

Your migrations will often reference values that differ between environments. Rather than maintaining separate migrations per environment, you can specify a [placeholder token](https://documentation.red-gate.com/fd/flyway-placeholders-namespace-277579022.html) e.g. `${schema}` which Flyway will resolve to a value at runtime. Moreover, Flyway can work backwards to replace string literals in your *generated* migration scripts with placeholder tokens that you can leverage to create portable migrations.

This tutorial will explore generating migrations with placeholders and applying them in a pseudo multi-tenant deployment system using Flyway.

### Prerequisites
[Flyway CLI](https://documentation.red-gate.com/fd/command-line-277579359.html) (Enterprise) & [Docker](https://docs.docker.com/get-started/get-docker/).

## Setup: PostgreSQL
This tutorial will use PostgreSQL. With Docker installed, let's launch our container:
```
> docker run --name flyway-placeholders-tutorial -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:18
```
Postgres ships with the `psql` CLI inside the container, let's use that to create our reference (call this "source") and tenant databases:
```
> docker exec -it flyway-placeholders-tutorial psql -U postgres -c "CREATE DATABASE source_db;" -c "CREATE DATABASE tenanta_db;" -c "CREATE DATABASE tenantb_db;"
```
We need to populate our source environment so we can migrate our tenants towards it:
```
> docker exec -it flyway-placeholders-tutorial psql -U postgres -d source_db
```
```
source_db=# CREATE SCHEMA source;
CREATE TABLE source.users (
    id SERIAL PRIMARY KEY
);

source_db=# exit
```
This is the table structure we want every tenant to have.

## Setup: Flyway

Inside your Flyway CLI `conf` directory, create a `flyway.toml` file to hold the configuration:
```toml
# The reference "source" schema we'll generate migrations from
[environments.source]
url = "jdbc:postgresql://localhost:5432/source_db"
user = "postgres"
password = "postgres"

[environments.source.flyway.placeholders]
schema = "source"

# The tenant environments to migrate towards source
[environments.tenanta]
url = "jdbc:postgresql://localhost:5432/tenanta_db"
user = "postgres"
password = "postgres"

[environments.tenanta.flyway.placeholders]
schema = "tenanta"

[environments.tenantb]
url = "jdbc:postgresql://localhost:5432/tenantb_db"
user = "postgres"
password = "postgres"

[environments.tenantb.flyway.placeholders]
schema = "tenantb"

# Where migrations are stored 
[flyway]
locations = ["filesystem:migrations"]
```
Each environment here has an override to configure their unique `${schema}` placeholder.

Lastly create a `migrations` directory in the Flyway CLI root to house the migration scripts we will generate.

## Generating Migrations with Placeholders
We can `diff` the source database against an empty baseline to generate a migration:
```
> flyway-12.6.1 % ./flyway diff generate -diff.source=source -diff.target=empty -generate.usePlaceholders=true -environment=source
```
Take a look at the generated migration in the `migrations` directory:
```sql
...
CREATE TABLE ${schema}.users (
    id integer NOT NULL DEFAULT nextval('${schema}.users_id_seq'::regclass)
);
...
```
By specifying the `-generate.usePlaceholders=true` flag for this environment, Flyway finds every instance of the literal `source` and replaces it with the placeholder token `${schema}`.

*Note: `-generate.usePlaceholders` is not SQL aware, it's not recommended to use values that will partially match other texts in your scripts.*

## Migrating with Placeholders

We can now deploy our migration scripts to each tenant using their own set of values for `${schema}`:
```
> flyway-12.6.1 % ./flyway migrate -environment=tenanta
> flyway-12.6.1 % ./flyway migrate -environment=tenantb -dryRunOutput=dryrun.sql
```
The dry run for `tenantb` doesn't execute the migration, but it's useful for verifying the exact SQL that will be executed. Open the `dryrun.sql` file in the root:
```sql
...
CREATE TABLE tenantb.users (
    id integer NOT NULL DEFAULT nextval('tenantb.users_id_seq'::regclass)
);
...
```
Observe that `${schema}` from our generated migration script has been replaced with our placeholder definition for this environment.

## Summary

To recap, we worked backwards using `generate.usePlaceholders` to automatically insert placeholders into generated migration scripts, then deployed the same migration script to multiple tenants.
