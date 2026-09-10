---
subtitle: Configuring SQLFluff Rules
---
SQLFluff is configured using a configuration file.

Flyway ships with a default configuration file in the `conf/` folder of the Flyway installation called `sqlfluff.cfg`. In order to turn this into a project policy we recommend putting a copy of this file under version control for your project and directing Flyway where to find this using the [`rulesConfig`](<configuration/flyway namespace/flyway check namespace/flyway check rules config setting>) parameter

## Which configuration is used

Flyway resolves at most one SQLFluff configuration file per run. The choice depends on whether [`flyway.check.rulesConfig`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Rules Config Setting>) is set and which SQLFluff engine is active:

| Situation | Configuration used | Local SQLFluff config files |
| --------- | ------------------- | --------------------------------------------------- |
| `rulesConfig` is set | That file, alone. It must exist or Flyway errors | Ignored |
| Unset, Redgate Bundle SQLFluff active, `conf/sqlfluff.cfg` present | `conf/sqlfluff.cfg`, alone | Ignored |
| Unset, using your own SQLFluff installation | None supplied by Flyway | SQLFluff's own [configuration searching and nesting behavior](https://docs.sqlfluff.com/en/stable/configuration/setting_configuration.html) applies |
| Unset, Redgate Bundle SQLFluff active but `conf/sqlfluff.cfg` missing | None supplied by Flyway | SQLFluff's own [configuration searching and nesting behavior](https://docs.sqlfluff.com/en/stable/configuration/setting_configuration.html) applies |

SQLFluff itself reads local configuration from up to five files - `pyproject.toml`, `.sqlfluff`, `pep8.ini`, `tox.ini` and `setup.cfg`. See SQLFluff's [configuration documentation](https://docs.sqlfluff.com/en/stable/configuration/setting_configuration.html) for how these are discovered, ordered and merged. When Flyway resolves a configuration file it suppresses all five; when it does not, all five are available to SQLFluff as normal.

A few things are always true, regardless of which row applies:

- Flyway never merges configuration files. Whenever it resolves one, it passes both `--config` and `--ignore-local-config` to SQLFluff, so exactly one file applies. If you want to extend the shipped default, copy `conf/sqlfluff.cfg` and edit the copy rather than layering a local file on top.
- The dialect always comes from Flyway, not from your configuration file. Flyway passes `--dialect` on every run - [`flyway.check.rulesDialect`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Rules Dialect Setting>) if set, otherwise a dialect derived from the JDBC URL, otherwise ANSI. SQLFluff's command-line arguments take precedence over its config-file keys, so a `dialect =` inside your configuration file has no effect.
- [`flyway.check.sqlfluffCustomRulesPath`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check SQLFluff Custom Rules Location Setting>) is separate from all of this and additive - it points at a directory of custom rules rather than a configuration file, and is only available with Redgate Bundle SQLFluff. See [Configuring Custom SQLFluff Rules](<Code Review Rules/Configuring Custom SQLFluff Rules>) for how to use it.

### Which engine is active

Setting environment variable `FLYWAY_ENV_NATIVE_SQLFLUFF=false` forces Flyway to use your own SQLFluff installation, whatever your edition. This takes precedence over the license check when selecting an engine.

{% include enterprise.html %}

Redgate Bundle SQLFluff is used when Flyway is licensed for Flyway Enterprise and the bundled engine is present in your installation. If it is not present, Flyway logs a warning and falls back to your own SQLFluff installation - in which case no default config is loaded and local config files apply.

## Enabling and Disabling Rules
There are a number of ways to enable and disable rules in the SQLFluff configuration file
[SQLFluff Rules configuration](https://docs.sqlfluff.com/en/stable/configuration/rule_configuration.html#enabling-and-disabling-rules)

The default configuration shipped with Flyway enables only a subset of SQLFluff rules, primarily those that help make your SQL statements clearer, safer, and more consistently styled. 
It also enables a set of custom rules provided by Redgate by default.

Note that these custom rules are available only in the Redgate-packaged version of [`SQLFluff`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check SQLFluff Enabled Setting>).

Redgate also recommends explicitly specifying the rules to enable (using `rules`) rather than relying on `exclude_rules` to disable unwanted ones. 
This approach is safer and helps prevent potential issues if new Redgate custom rules are added in future releases.

```toml
[sqlfluff]
rules = ambiguous,convention,structure,RG01,RG02,RG03...
```

## Configuring Rules

### Severity
By default, rules are enabled and violations are regarded as errors.

Whether Flyway will fail or not if there is an error is governed by the [`check.code.failOnError`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Code Fail On Error Setting>) setting

| Rule severity | Mechanism |
| --------------|-----------|
| Error         | No action required |
| Warning       | Add the rule or group to the [`[sqlfluff].warnings`](https://docs.sqlfluff.com/en/stable/configuration/rule_configuration.html#downgrading-rules-to-warnings) section |
| Disabled      | Add the rule or group to the [`[sqlfluff].exclude_rules`](https://docs.sqlfluff.com/en/stable/configuration/rule_configuration.html#downgrading-rules-to-warnings) section |

### Parameters
Many rules have configurable options that allow you to customize their behavior. For the [standard SQLFluff rules](https://docs.sqlfluff.com/en/stable/reference/rules.html) you will see these defined in each rule in a section labeled "Configuration"

For the [Redgate SQLFluff rules Library](<Code Review Rules/Redgate SQLFluff Rules Library>) you will also find a "Configuration" section for rules where this is applicable.

In order to configure these rules you would edit the SQLFluff configuration file

As an example, if we take the rule: [CV09 - Block a list of configurable words from being used](https://docs.sqlfluff.com/en/stable/reference/rules.html#rule-convention.blocked_words). This offers the following configuration parameters `blocked_regex`, `blocked_words` and `match_source`

We could configure the rule to trigger a violation on the use of `TODO` in the SQL by configuring the it in `sqlfluff.cfg` like this

```toml
[sqlfluff:rules:convention.blocked_words]
blocked_words = TODO
```
# Local violation of policy
Having configured your code review policy there may be occasions when you want to override the policy temporarily. There are a number of mechanisms available with varying degrees of granularity.

## Pipeline level
This involves adding a manual approval step to your pipeline so if Flyway's `check -code` operation returns violations then it will require a review and approval step to proceed.

- Run `flyway check -code` ->  If successful, deploy changes
   - else stop for manual review -> if approved, deploy changes
      - else fail pipeline


## File level 
This involves adding [In-File Configuration Directives](https://docs.sqlfluff.com/en/stable/configuration/setting_configuration.html#in-file-configuration-directives) to the top of your file.
For example, if I want to suspend rule `AM01` for this file I would add a comment to my SQL like this:

```sql
-- noqa: disable=AM01
```

Which will prevent this rule being checked on the remainder of the file

## Section level
This is a more localized change of policy just [ignoring a range](https://docs.sqlfluff.com/en/stable/configuration/ignoring_configuration.html#ignoring-line-ranges) of lines in the SQL
```sql
-- noqa: disable = AL02
SELECT col_a a FROM foo
-- noqa: enable = AL02
```

## line level
This is the most granular change and just [ignores violation on an individual line](https://docs.sqlfluff.com/en/stable/configuration/ignoring_configuration.html#ignoring-individual-lines)
```sql
-- Ignore rule CP02 & rule CP03
SeLeCt  1 from tBl ;    -- noqa: CP02,CP03
```

{% include anchor.html link="RedgatePolicyRules-NOQA"%}
## Reporting `--noqa` Tag Usage
{% include enterprise.html %}

Using `--noqa` tags to suppress SQLFluff code review rules introduces risks, as those statements will bypass linting checks. 

To help mitigate this, Flyway generates a warning-level violation by default whenever a `--noqa` tag is detected. The severity of this violation can be configured using the [`flyway.check.code.noqaSeverity`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Code Noqa Severity Setting>) setting.

If this warning has not been set to `DISABLED`, In the command-line output you will see something similar to the following:

```
+--------------------+-----------+------------------+----------+
| Engine             | Rule Code | Violations Count | Severity |
+--------------------+-----------+------------------+----------+
| SQLFluff (Redgate) | RG01      | 1                | Error    |
| SQLFluff (Redgate) | RG02      | 1                | Error    |
| Policy Overrides   | noqa      | 4                | Warning  |
+--------------------+-----------+------------------+----------+
```
