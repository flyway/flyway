---
subtitle: add
---
# Add - Preview

## Why is this useful ?
The `add` command is used to create new migration scripts using following the naming convention of your existing migrations. 

### Auto-generated version
If no version is provided to the add command then Flyway will search all configured filesystem migration locations to determine the next migration version.
For example, if two migration directories are configured with the following contents:
```
$ ls legacy-migrations
B1.0.0__Baseline.sql
...
V1.0.25__Migration.sql

$ ls migrations
V2.0.1__Migration.sql
...
V2.0.54__Migration.sql
```
Then Flyway would calculate the next migration version to be `2.0.55`.

### Undo Scripts
In the case where an undo migration is generated then Flyway will use the description and version of the highest versioned migration.
For example, if the migrations directory has the following contents before running the add command:
```
$ ls migrations
B001.sql
...
V055__feature_A.sql
```

Then the `add` command would give the generated undo migration the version and description corresponding to `feature_A`.
The command below shows this example:
```
$ flyway add -add.type=undo

created: .\migrations\U055__feature_A.sql
```

### Quiet & nameOnly options
When flyway is run in `Quiet` mode, `flyway add` will output the name of the created migration file to the output stream only. If the `nameOnly` option is set, flyway will not create the file on disk, but will still calculate the name. This may be useful for scripting purposes where migration files are generated as part of the script:
```bash
#!/bin/bash
NEXT_MIGRATION_FILE="$(flyway -q add -add.nameOnly=true)"
...
echo "$SQL_HEADER" > "$NEXT_MIGRATION_FILE"
echo "$SQL_CONTENT" >> "$NEXT_MIGRATION_FILE"
```