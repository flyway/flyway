---
layout: documentation
menu: migrationtesting
subtitle: 'Tutorial: Testing Flyway migrations in a CI pipeline'
redirect_from: /documentation/getstarted/advanced/migrationtesting/
---
# Tutorial: Testing Flyway migrations in a CI pipeline

This tutorial will guide you through the process of setting up Flyway migration tests in a CI pipeline using [Spawn](https://spawn.cc?utm_source=flyway&utm_medium=docs&utm_campaign=migrationtesting_tutorial&utm_id=flyway) to provision a database on the fly.

This tutorial should take you about **20 minutes** to complete.

## Prerequisites

Install Spawn by visiting the [getting started documentation](https://docs.spawn.cc/getting-started/installation?utm_source=flyway&utm_medium=docs&utm_campaign=migrationtesting_tutorial&utm_id=flyway) and following the installation steps.

This tutorial will assume you have a database with Flyway migrations already set up and commited to your own repository. Alternatively, follow along with the examples taken from [this demo repo](https://github.com/red-gate/flyway-spawn-demo), which you can fork and clone.

## Introduction

Testing your database migrations before they reach production is critical. There are a few scenarios which result in a breaking change: you could write a migration which doesn't work on a database with data in, you could have 2 developers both merge a versioned migration into production at the same time, and someone could write some SQL which leads to data loss. You can read more about the importance of testing your database migrations [here](https://flywaydb.org/blog/why-you-should-be-testing-flyway-migrations-in-ci).

We will work through a step-by-step guide on setting up migration tests for the [PostgreSQL Pagila demo database](https://github.com/devrimgunduz/pagila) stored in [Amazon RDS](https://aws.amazon.com/rds/), in a [GitHub Actions](https://github.com/features/actions) pipeline. The logic can be applied to any other CI pipeline, and also SQL Server and MySQL databases.

## Create a backup of your database

Firstly, we want to get a backup of our production database. This will ultimately be used to run migrations against later. Testing against real data will catch problems which may not arise from testing against an empty database.

1. In our repository, lets create a folder which will hold our backup file:

    <pre class="console"><span>&gt;</span> mkdir backups</pre>

1. Following the [PostgreSQL docs](https://www.postgresql.org/docs/current/app-pgdump.html), we'll run the following to get a database backup file saved to our folder as `pagila.sql`. Note that `pg_dump` will have to be the same version (or lower) of your Spawn database, which is 12 in our example:

    <pre class="console"><span>&gt;</span> export PGPASSWORD=&lt;Password&gt;</pre>
    <pre class="console"><span>&gt;</span> pg_dump -h &lt;Host&gt; -p &lt;Port&gt; -U &lt;Username&gt; --create &lt;DbName&gt; --file backups/pagila.sql</pre>

1. Using Spawn, we can turn this into a [data image](https://www.spawn.cc/docs/concepts-data-image?utm_source=flyway&utm_medium=docs&utm_campaign=migrationtesting_tutorial&utm_id=flyway) from our backup file, following the instructions [here](https://www.spawn.cc/docs/source-configuration-backup-postgres?utm_source=flyway&utm_medium=docs&utm_campaign=migrationtesting_tutorial&utm_id=flyway). First, we will create a source file `pagila-backup.yaml` **in the directory which contains the `backups` folder** with the following content:

    ```yaml
    name: Pagila
    engine: postgresql
    version: 12.0
    sourceType: backup
    backup:
      folder: ./backups/
      file: pagila.sql
      format: plain
    tags:
      - prod
      - latest
    ```

1. The source file describes where the data and schema comes from. We can now create a data image from this:

    <pre class="console"><span>&gt;</span> spawnctl create data-image --file ./pagila-backup.yaml</pre>

This set up will allow us to create databases from the backup file - we'll come back to this later on in the tutorial.

## Write a GitHub Actions workflow

Lets create and set up our GitHub Actions workflow to test database migrations.

### Set up access token for Spawn

1. To access `spawnctl` in the pipeline, we will first need to create an [access token](https://spawn.cc/docs/spawnctl-accesstoken-create?utm_source=flyway&utm_medium=docs&utm_campaign=migrationtesting_tutorial&utm_id=flyway#tutorial) so that we won't be prompted to authenticate with Spawn in our CI environment:

    <pre class="console"><span>&gt;</span> spawnctl create access-token --purpose "CI system Spawn access token"
    Access token generated: &lt;access-token-string&gt;</pre>

1. Copy the `<access-token-string>` into your clipboard; you won't be able to retrieve it again. Now lets add it as a secret variable called `SPAWNCTL_ACCESS_TOKEN` in GitHub Actions by following this [tutorial](https://docs.github.com/en/actions/reference/encrypted-secrets#creating-encrypted-secrets-for-a-repository).

### Create GitHub workflow

1. In your repository, add a file underneath the folder `.github/workflows` called `migration-test.yaml`

1. Copy and paste the yaml below, then commit and push the new file:

    ```yaml
    name: Database migration test

    on: workflow_dispatch

    jobs:
      run_migration_test:
        name: Run Flyway migration tests
        runs-on: ubuntu-latest
        steps:
          - name: Checkout
            uses: actions/checkout@v2
          - name: Run database migrations against prod backup
            run: ./migration-test-prod.sh
            env:
              SPAWNCTL_ACCESS_TOKEN: {% raw %}${{ secrets.SPAWNCTL_ACCESS_TOKEN }}{% endraw %}
    ```

1. This workflow is referencing a bash script `migration-test-prod.sh` which can be called from any CI pipeline as it is, and just work. Lets go ahead and create that script by copying the code below and saving the file at the root level of our directory, substituting the variable `databaseName` for your own databases name:

    ```bash
    #!/bin/bash

    set -e

    echo "Downloading and installing spawnctl..."
    curl -sL https://run.spawn.cc/install | sh > /dev/null 2>&1
    export PATH=$HOME/.spawnctl/bin:$PATH
    echo "spawnctl successfully installed"

    export SPAWN_PAGILA_IMAGE_NAME=Pagila:prod

    echo
    echo "Creating Pagila backup Spawn data container from image '$SPAWN_PAGILA_IMAGE_NAME'..."
    pagilaContainerName=$(spawnctl create data-container --image $SPAWN_PAGILA_IMAGE_NAME --lifetime 10m --accessToken $SPAWNCTL_ACCESS_TOKEN -q)

    databaseName="pagila"
    pagilaJson=$(spawnctl get data-container $pagilaContainerName -o json)
    pagilaHost=$(echo $pagilaJson | jq -r '.host')
    pagilaPort=$(echo $pagilaJson | jq -r '.port')
    pagilaUser=$(echo $pagilaJson | jq -r '.user')
    pagilaPassword=$(echo $pagilaJson | jq -r '.password')

    echo "Successfully created Spawn data container '$pagilaContainerName'"
    echo

    docker pull postgres:12-alpine > /dev/null 2>&1
    docker pull flyway/flyway > /dev/null 2>&1

    echo
    echo "Starting migration of database with flyway"

    docker run --net=host --rm -v $PWD/sql:/flyway/sql flyway/flyway migrate -url="jdbc:postgresql://$pagilaHost:$pagilaPort/$databaseName" -user=$pagilaUser -password=$pagilaPassword

    echo "Successfully migrated 'Pagila' database"
    echo

    spawnctl delete data-container $pagilaContainerName --accessToken $SPAWNCTL_ACCESS_TOKEN -q

    echo "Successfully cleaned up the Spawn data container '$pagilaContainerName'"
    ```    

1. Before committing and pushing this file to the repository, lets give it executable permissions:

    <pre class="console"><span>&gt;</span> chmod +x migration-test-prod.sh</pre>

This script accomplishes a few things. We are:
  * Installing Spawn to give us access to the `spawnctl` command from the pipeline agent
  * Creating a [Spawn data container](https://www.spawn.cc/docs/concepts-data-container?utm_source=flyway&utm_medium=docs&utm_campaign=migrationtesting_tutorial&utm_id=flyway) from the data image we previously created named `Pagila:prod`
  * Using the official [Flyway docker image](https://hub.docker.com/r/flyway/flyway) to run `flyway migrate` on our data container, using migration scripts stored under the `sql` folder
  * Automating the cleanup of the database

That's it - that is our migration test. We have quickly provisioned a database instance from our back up using Spawn, and set the Flyway connection details to point to that database and run the migration scripts in our repository. Any errors will be apparent at this point before changes get merged into the main branch.

Note: There is an **unofficial** [Flyway Migration action](https://github.com/marketplace/actions/flyway-migration) in the GitHub Marketplace which you can copy from. But using the code in `migration-test-prod.sh` is using generic bash and will work across all CI pipelines.

### Run Flyway migration tests

Once you've pushed all your scripts to GitHub, you can now manually run the migration test workflow by navigating to the Actions tab in GitHub and clicking on 'Database migration test', then 'Run workflow'. Or, a more likely scenario, we want this to automatically run on a pull request so the development team can test that any new migrations will run against main before merging.

Alter your `migration-test.yaml` file to the following, so that we now trigger this workflow on other GitHub events:

```yaml
name: Database migration test

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

  workflow_dispatch:

jobs:
  run_migration_test:
    name: Run Flyway migration tests
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v2
      - name: Run database migrations against prod backup
        run: ./migration-test-prod.sh
        env:
          SPAWNCTL_ACCESS_TOKEN: {% raw %}${{ secrets.SPAWNCTL_ACCESS_TOKEN }}{% endraw %}
```

### Automate creating backups 

One last thing which is useful - with the way it's set up, you will have to create a data image manually every time you have migration changes to get the latest backup of your production database. But we can [automate this step](https://www.spawn.cc/docs/howto-ci-scheduled-image-creation?utm_source=flyway&utm_medium=docs&utm_campaign=migrationtesting_tutorial&utm_id=flyway) using Spawn in our existing GitHub Actions pipeline.

1. Add some more GitHub secrets so that we can allow the agent access to our production database **to back up, not write to**. We will need the database's `<Host>`, `<Username>`, and `<Password>`. The next step will reference these secrets by using the names `PAGILA_HOST`, `PAGILA_ADMIN_USERNAME` and `PAGILA_ADMIN_PASSWORD` but you will create secret names prefixed with your own database name.

1. Lets add another workflow called `db-backup.yaml` underneath our `.github/workflows` folder:

    ```yaml
    name: Take backup of production database daily

    on:
      schedule:
        - cron: "0 9 * * MON-FRI"

      workflow_dispatch:

    jobs:
      take_db_backup:
        name: Take backup of production database
        runs-on: ubuntu-latest
        steps:
          - name: Checkout
            uses: actions/checkout@v2
          - name: Create backup
            run: ./take-db-backup.sh
            env:
              SPAWNCTL_ACCESS_TOKEN: {% raw %}${{ secrets.SPAWNCTL_ACCESS_TOKEN }}{% endraw %} 
              PAGILA_HOST: {% raw %}${{ secrets.PAGILA_HOST }}{% endraw %} 
              PAGILA_USERNAME: {% raw %}${{ secrets.PAGILA_ADMIN_USERNAME }}{% endraw %} 
              PAGILA_PASSWORD: {% raw %}${{ secrets.PAGILA_ADMIN_PASSWORD }}{% endraw %} 
    ```

1. We are going to create another bash script here called `take-db-backup.sh`, which you can create at the root level of your repository with the following content substituting the variable `databaseName` for your own databases name:

    ```bash
    #!/bin/bash

    set -e

    echo "Downloading and installing spawnctl..."
    curl -sL https://run.spawn.cc/install | sh > /dev/null 2>&1
    export PATH=$HOME/.spawnctl/bin:$PATH
    echo "spawnctl successfully installed"

    databaseName="pagila"
    mkdir backups

    docker pull postgres:12-alpine > /dev/null 2>&1

    echo
    echo "Backing up Pagila database..."

    docker run --net=host --rm -v $PWD/backups:/backups/ -e PGPASSWORD=$PAGILA_PASSWORD postgres:12-alpine pg_dump -h $PAGILA_HOST -p 5432 -U $PAGILA_USERNAME --create $databaseName --file /backups/pagila.sql

    echo "Creating Spawn data image..."
    echo

    pagilaImageName=$(spawnctl create data-image --file ./pagila-backup.yaml --lifetime 336h --accessToken $SPAWNCTL_ACCESS_TOKEN -q)

    echo "Successfully created Spawn data image '$pagilaImageName'"
    echo
    echo "Successfully backed up Pagila database"
    ```

1. Once again, this script needs to be made executable:

    <pre class="console"><span>&gt;</span> chmod +x take-db-backup.sh</pre>

1. Finally you'll need to add the source file for your data image `pagila-backup.yaml`, which you created locally earlier, to your repository. Now you can push all your new files to the repository.

Every week day at 9am, this script will run which creates a new data image from a backup of the latest state of Pagila database. We don't need to change our other workflow - that is already programmed to use an image under the name of `Pagila:prod`, and much like docker tags we will have a newer image with the `prod` tag after this script is run.

## Summary

In this tutorial we saw how to
- take a backup and create a Spawn data image from it
- spin up a database from that data image in a CI pipeline
- automate running `flyway migrate` to test latest migrations against a copy of production
- automate scheduling image creation so tests are always run against the latest database
