---
subtitle: Configuring Regex Rules
---
{% include enterprise.html %}

Defining and configuring rules with a regular expression is a relatively simple way to define coding standards in your organization.

Rules are defined as text files with various fields defined in each file using  [TOML](https://toml.io/en/) format. It is limited in capability as a regex is a text pattern matching process, it has no awareness of the structure and semantics of the code being analyzed. As such it is best for simple patterns and checks.

Flyway ships with a set of default rules in the `rules/` folder of the Flyway installation. In order to turn this into a project policy we recommend putting a copy of these rules under version control for your project and directing Flyway where to find these. You can use the [`rulesLocation`](<configuration/flyway namespace/flyway check namespace/flyway check rules location setting>) parameter to direct Flyway.

If you want to add more rules it is a matter of creating new files following the same structure outlined below.

## Configuration
For purposes of setting policy, all you need to do is change the `severity` file in each rule.

## Regular expression rules format

When using regular expression rules for static code analysis through [check -code](<Commands/Check/Check Code>), the format of the [TOML](https://toml.io/en/) rules files is as follows:

| Field       | Purpose                                        | Type               | Possible Values                                                                                               | Example                                               |
|-------------|------------------------------------------------|--------------------|---------------------------------------------------------------------------------------------------------------|-------------------------------------------------------|
| code        | Rule Code                                      | String             | Anything                                                                                                      | RX001                                                 |
| dialects    | Which dialect of SQL does this rule apply to   | Array (of Strings) | `text`, `bigquery`, `db2`, <BR>`mysql`, `oracle`, `postgres`,<BR>`redshift`, `snowflake`,<BR>`sqlite`, `tsql` | ["text"]                                              |
| rules       | The regex rule you want                        | Array (of Strings) | [Regular Expressions](https://www.regular-expressions.info/)                                                  | ["your regex here"]                                   |
| description | Allows a more in-depth description of the rule | String             | Anything                                                                                                      | "Descriptive comment that will appear in your report" |
| severity    | Controls how violations are handled            | String             | `error`, `warning`, `disabled`                                                                                | "error"                                               |


### Rule file naming

The file name will be used as the source of rule metadata: A__B.toml (that's two underscores)

* Where A is the rule code (If the `code` field is not set in the file content, this value will be used as the default code)
* Where B is a short rule description (If the `description` field is not set in the file content, this value will be used as the default description)

### Dialects

The way your regex rule is structured will vary depending on the dialect of SQL in use with your database (different keywords and syntax) so you may need explicitly declare the dialect that this rule is relevant for.

Flyway will identify the variety of SQL relevant to database based on the JDBC connection string and only apply relevant rules (so a rule declared for the Oracle dialect won't be applied when using a PostgreSQL database).

* The `TEXT` dialect means the rule will be applied to all migrations regardless of the DB type Flyway is configured to use.

### Severity

The `severity` field controls how violations of this rule are handled:

* `error` - Violations will cause the check command to fail when [`check.code.failOnError`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Code Fail On Error Setting>) is enabled
* `warning` - Violations will be reported but will not cause the operation to fail
* `disabled` - The rule will be ignored and no violations will be reported

The `disabled` severity level allows you to temporarily disable specific rules without removing them from your configuration.

### passOnRegexMatch **Deprecated**

The parameter is deprecated in Regex Code Analysis and will be ignored in future releases. Regex matches will always be treated as violations.
 
If your previous configuration relied on `passOnRegexMatch = true` (i.e. expecting the pattern to be present), you will need to invert your Regex to match cases where the pattern is absent instead.

| Value | Purpose                                                                                                                                                 |
| ----- | ------------------------------------------------------------------------------------------------------------------------------------------------------- |
| false | There is something in my migration that the regex matches - I want this rule to flag a violation in this case                                           |
| true  | I want a particular style or pattern in my code (for example, something standard in every migration script). If it is *not* there then flag a violation |

### Regular expression considerations

* Does case sensitivity matter to you ? If it doesn't then make the regex rules insensitive too with the prefix `(?i)`
* You will need to use the [Java dialect](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html) of regex

### File content example

```
code = "RX001"
dialects = ["TEXT"]
rules = ["(?i)(^|\\s)TO\\s+DO($|\\s|;)"]
description = "Phrase 'to do' remains in the code"
severity = "error"
```

## Related content
- [Product learning - Custom Regex Rules](https://www.red-gate.com/hub/product-learning/flyway/creating-custom-regex-rules-for-code-analysis-in-flyway)