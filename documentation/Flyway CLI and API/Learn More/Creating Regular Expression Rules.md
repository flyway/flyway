---
pill: writing-regex-rules
subtitle: Code Analysis with Regular Expressions
---
{% include enterprise.html %}

# Writing Regex Rules
You are able to create a regular expression (regex) rule to be applied to your code to identify particular combinations of keywords that are problematic. It is not checking for valid SQL but for text that is deemed bad practice as it could be a problem for database and it's ecosystem. 
## File format
The expected format is [TOML](https://toml.io/en/) but in this context this means a key-value pair layout

| Field            | Purpose  | Type | Possible values | Example 
|---               | ---      | ---  | ---             | ---
| dialects         | Which dialect of SQL does this rule apply to | Array (of Strings) | TEXT, BIGQUERY, DBS, <BR>MYSQL, ORACLE, POSTGRES,<BR>REDSHIFT, SNOWFLAKE,<BR>SQLITE, TSQL | ["TEXT"]
| rules            | The regex rule you want | Array (of Strings)| [Regular Expressions](https://www.regular-expressions.info/) | ["your regex here"]
| passOnRegexMatch | If the regex matches should the rule trigger a violation  | String |  true, false | "false"
| description      | Allows a more in-depth description of the rule | String | Anything | "Descriptive comment that will appear in your report" 

## Dialects
The way your regex rule is structured will vary depending on the dialect of SQL in use with your database (different keywords and syntax) so you may need explicitly declare the dialect that this rule is relevant for.

Flyway will identify the variety of SQL relevant to database based on the JDBC connection string and only apply relevant rules (so a rule declared for the Oracle dialect won't be applied when using a PostgreSQL database).

* The `TEXT` dialect means the rule will be applied to all migrations regardless of the DB type Flyway is configured to use.

## passOnRegexMatch
Your regular expression will return one of two values:
* The pattern matched something in the migration
* The pattern did not match anything in the migration

You can modify this to alter whether this flags a violation or not (inverting the logic of the regular expression)

| Value | Purpose 
|--- | ---
| false | There is something in my migration that the regex matches - I want this rule to flag a violation in this case
| true  | I want a particular style or pattern in my code (for example, something standard in every migration script). If it is *not* there then flag a violation

# Creating Your own rules
Each rule is declared in a separate .toml file and these should be located in the `/rules` folder in the root of your flyway installation.

We'd suggest taking one of the [supplied regex rules](Usage/Code Analysis Rules) in the default `\rules` folder and adapting it to your needs.
## File content example
```
dialects = ["TEXT"]
rules = ["(?i)(^|\\s)TO\\s+DO($|\\s|;)"]
passOnRegexMatch = false
description = "Phrase 'to do' remains in the code"
```
### Good to know
* Does case sensitivity matter to you ? If it doesn't then make the regex rules insensitive too with the prefix `(?i)`
* You will need to use the [Java dialect](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html) of regex 

## File Naming
The file name will be used as the source of rule metadata:
`A__B.toml` (that's two underscores)
* Where `A` is the rule identifier (e.g. MyRule01)
* Where `B` is a short rule description (this will be replaced by the `description` field in the file content if supplied) 

## File location

See the [check.rulesLocation](Configuration/Parameters/Flyway/Check/Rules Location) parameter

# Running the rules
When you run `./flyway check -code` all regex rules will be run

You will see the following line of text output on the command line indicating the rules are being run and if there are violations you will see them in the produced report:

<pre>RegexRulesEngine code analysis summary:</pre>
