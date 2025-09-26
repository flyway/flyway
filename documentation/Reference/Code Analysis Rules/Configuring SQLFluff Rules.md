---
subtitle: Configuring SQLFluff Rules
---
SQLFluff is configured using a configuration file.

Flyway ships with a default configuration file in the `conf/` folder of the Flyway installation called `sqlfluff.cfg.example`. In order to turn this into a project policy we recommend putting a copy of this as `sqlfluff.cfg` under version control for your project and directing Flyway where to find this using the [`rulesConfig`](<configuration/flyway namespace/flyway check namespace/flyway check rules config setting>) parameter

## Enabling and Disabling Rules
There are a number of ways to enable and disable rules in the SQLFluff configuration file
[SQLFluff Rules configuration](https://docs.sqlfluff.com/en/stable/configuration/rule_configuration.html#enabling-and-disabling-rules)

The example configuration that Flyway ships with globally disables all SQLFluff rules to avoid flooding you with warnings the moment you run code analysis. If you comment out the `exclude_rules` line, all rules will be enabled.

```toml
[sqlfluff]
#exclude_rules = all
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
Many rules have configurable options that allow you to customize their behavior. For the [standard SQLFluff rules](https://docs.sqlfluff.com/en/stable/reference/rules.html) you will see these defined in each rule in a section labelled "Configuration"

For the [Redgate SQLFluff rules](<Code Analysis Rules/Redgate SQLFluff Rules>) you will also find a "Configuration" section for rules where this is applicable.

In order to configure these rules you would edit the SQLFluff configuration file

As an example, if we take the rule: [CV09 - Block a list of configurable words from being used](https://docs.sqlfluff.com/en/stable/reference/rules.html#rule-convention.blocked_words). This offers the following configuration parameters `blocked_regex`, `blocked_words` and `match_source`

We could configure the rule to trigger a violation on the use of `TODO` in the SQL by configuring the it in `sqlfluff.cfg` like this

```toml
[sqlfluff:rules:convention.blocked_words]
blocked_words = TODO
```

