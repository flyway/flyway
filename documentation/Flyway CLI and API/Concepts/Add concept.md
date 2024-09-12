---
subtitle: add
---
# Add

## Why is this useful ?
The `add` command can be used to generate migration scripts using an auto-calculated version.
This allows the user to create migration script files in a consistent manner without having to manually calculate the next version.

### Auto-generated version
If no version is provided to the add command then Flyway will search all configured filesystem migration locations to determine the next migration version.
For example, if two migration directories are configured with the following contents:
```
$ ls legacy-migrations
B001.sql
...
V125.sql

$ ls migrations
V126.sql
...
V254.sql
```
Then Flyway would calculate the next migration version to be `255`.

### Undo reuses description
In the case where an undo migration is generated then Flyway will use the description of the highest versioned migration.
For example, if the migrations directory has the following contents before running the add command:
```
$ ls migrations
B001.sql
...
V055__feature_A.sql
```

Then the `add` command would give the generated undo migration the description `feature_A`.
The command below shows this example:
```
$ flyway add -add.type=undo

created: .\migrations\U055__feature_A.sql
```

### Quiet & nameOnly options
Rather than generating an empty migration script file, the `nameOnly` option can be used to return the name of the migration script without creating the file.
This could be useful for scripting purposes where migration files are generated in a custom way:
```bash
#!/bin/bash
NEXT_MIGRATION_FILE="$(flyway -q add -add.nameOnly=true)"
...
echo "$SQL_HEADER" > "$NEXT_MIGRATION_FILE"
echo "$SQL_CONTENT" >> "$NEXT_MIGRATION_FILE"
```