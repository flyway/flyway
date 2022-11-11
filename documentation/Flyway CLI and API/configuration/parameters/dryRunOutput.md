---
layout: documentation
menu: configuration
pill: dryRunOutput
subtitle: flyway.dryRunOutput
redirect_from: /documentation/configuration/dryRunOutput/
---

# Dry Run Output
{% include teams.html %}

## Description
The file where to output the SQL statements of a migration dry run. If the file specified is in a non-existent directory, 
Flyway will create all directories and parent directories as needed.

Omit to use the default mode of executing the SQL statements directly against the database.

See [dry runs](/documentation/concepts/dryruns) for more details.

### Amazon S3

Paths starting with <code>s3:</code> point to a bucket in AWS S3, which must exist. They are in the format 
`s3:<bucket>(/optionalfolder/subfolder)/filename.sql`. To use AWS S3, the 
[AWS SDK v2](https://mvnrepository.com/artifact/software.amazon.awssdk/services) and dependencies must be included, 
and [configured](https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html) for your S3 account.

### Google Cloud Storage

Paths starting with <code>gcs:</code> point to a bucket in Google Cloud Storage, which must exist. They are in the 
format `gcs:<bucket>(/optionalfolder/subfolder)/filename.sql`. To use GCS, the GCS library must be included, and the 
GCS environment variable `GOOGLE_APPLICATION_CREDENTIALS` must be set to the credentials file for the service 
account that has access to the bucket.

## Default
<i>Execute directly against the database</i>

## Usage

### Commandline
```powershell
./flyway -dryRunOutput="/my/output/file.sql" clean
```

### Configuration File
```properties
flyway.dryRunOutput=/my/output/file.sql
```

### Environment Variable
```properties
FLYWAY_DRYRUN_OUTPUT=/my/output/file.sql
```

### API
```java
Flyway.configure()
    .dryRunOutput("/my/output/file.sql")
    .load()
```

### Gradle
```groovy
flyway {
    dryRunOutput = '/my/output/file.sql'
}
```

### Maven
```xml
<configuration>
    <dryRunOutput>/my/output/file.sql</dryRunOutput>
</configuration>
```

## Use Cases

### Preview changes without altering the database

Quite often a migration may be making use of [placeholders](/documentation/configuration/parameters/placeholders), such as in the following statement:

```
INSERT INTO table1(name) VALUES('${name}')
```

There may also be callbacks executing as part of your migration process which you might not be aware of when developing migrations. Instead of risking errors when migrating against your actual database with these unknowns, you can use dry runs to generate the SQL that would be executed in order to preview what would happen without altering the database. For example, you may notice that the placeholder `${name}` isn't what you expected. Part of the dry run might show as:

```
-- Source: ./V1__insert1.sql
---------------------------
INSERT INTO table1(name) VALUES('XYZ')
```

You may have expected `${name}` to be `ABC` instead of `XYZ`. There could also be a callback being executed before your migration which you aren't accounting for.
