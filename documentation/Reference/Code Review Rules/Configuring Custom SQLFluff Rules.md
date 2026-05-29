---
subtitle: Configuring Custom SQLFluff Rules
---
{% include teams.html %}

Flyway's `check -code` operation ships with the standard SQLFluff rule set plus a library of additional rules authored by Redgate. You can point Flyway at your **own** SQLFluff rules, written in Python, and have them run alongside the bundled rules.

This is useful when you have policy that is specific to your team or product for example, "no `TRUNCATE TABLE`" or "every table name must start with `app_`" and the standard rules don't cover it.

## Prerequisites

- A version of Flyway that supports [`check.sqlfluffCustomRulesPath`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check SQLFluff Custom Rules Location Setting>) ([see release notes](https://documentation.red-gate.com/flyway/release-notes-and-older-versions/release-notes-for-flyway-engine)).
- Familiarity with [authoring SQLFluff rules](https://docs.sqlfluff.com/en/stable/perma/rules.html). The Flyway integration uses standard SQLFluff plugin API there is no rule API specific to Flyway to learn.

## Configuration

Point Flyway at a directory containing your rules plugin package by setting [`check.sqlfluffCustomRulesPath`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check SQLFluff Custom Rules Location Setting>) in your `flyway.toml`:

```toml
[flyway.check]
sqlfluffCustomRulesPath = "/code-review-rules"
```

You can also set this on the command line:

```shell
flyway check -code -check.sqlfluffCustomRulesPath="/code-review-rules"
```

The path must be a directory that:

- exists, and
- contains an `__init__.py` file (i.e. it is a Python package that follows the [SQLFluff plugin layout](https://docs.sqlfluff.com/en/stable/configuration/plugin_configuration.html)).

If either of these is not true, `check -code` aborts with a clear error rather than running with rules silently missing:

> The specified SQLFluff custom rules directory not found: /code-review-rules.
>
> Please verify your check.sqlfluffCustomRulesPath setting

> The specified SQLFluff custom rules directory is not a plugin package:
>
> /code-review-rules (missing __init__.py).
>
> Please verify your check.sqlfluffCustomRulesPath setting

## Plugin layout

A minimal plugin is two files:

```
my-rules/
├── __init__.py
└── rules.py
```

### `__init__.py`

This file exposes your rules to SQLFluff via the `get_rules` hook. Rule imports must happen **inside** the hook, not at module top level, so that the package finishes loading (and `get_configs_info` runs) before SQLFluff's metaclass validates any `config_keywords`.

```python
from sqlfluff.core.plugin import hookimpl


@hookimpl
def get_rules():
    from .rules import Rule_TP01, Rule_TP02
    return [Rule_TP01, Rule_TP02]


@hookimpl
def get_configs_info():
    return {
        "required_table_prefix": {
            "definition": "Required prefix for all table names.",
            "default": "app_",
        },
    }
```

Only include `get_configs_info` if at least one of your rules takes a parameter (see [Parameterised rules](#parameterised-rules)).

### `rules.py`

Each rule is a class that extends `BaseRule`. Class names must follow SQLFluff's convention: `Rule_<CODE>`, where `<CODE>` is the rule code that appears in the report (e.g. `Rule_TP01` becomes code `TP01`).

```python
from sqlfluff.core.rules import BaseRule, LintResult
from sqlfluff.core.rules.crawlers import SegmentSeekerCrawler


class Rule_TP01(BaseRule):
    """No TRUNCATE TABLE statements allowed."""

    groups = ("all",)
    name = "custom.no_truncate"
    crawl_behaviour = SegmentSeekerCrawler({"truncate_table"})
    is_fix_compatible = False

    def _eval(self, context):
        return LintResult(
            anchor=context.segment,
            description="TRUNCATE TABLE is not permitted by company policy (TP01).",
        )
```

The rule's class docstring becomes the rule title shown when Flyway lists available rules.

### Parameterised rules

Rules that read configuration values declare the parameter names in `config_keywords` and read them via `self.<keyword>` in `_eval`. The defaults and metadata must also be declared in the package's `get_configs_info` hook.

```python
class Rule_TP02(BaseRule):
    """All table names must start with a configured prefix."""

    groups = ("all",)
    name = "custom.table_prefix"
    crawl_behaviour = SegmentSeekerCrawler({"table_reference"})
    is_fix_compatible = False
    config_keywords = ["required_table_prefix"]

    def _eval(self, context):
        table_name = context.segment.raw
        if not table_name.startswith(self.required_table_prefix):
            return LintResult(
                anchor=context.segment,
                description=(
                    f"Table '{table_name}' must start with "
                    f"'{self.required_table_prefix}' (TP02)."
                ),
            )
        return None
```

Override the default by setting the parameter in your `sqlfluff.cfg` (configured via [`check.rulesConfig`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Rules Config Setting>)):

```toml
[sqlfluff:rules:custom.table_prefix]
required_table_prefix = "dim_"
```

## Rule code naming conventions

Custom rule codes use a leading alphabetic prefix (e.g. `TP` in `TP01`). Flyway reserves the prefixes already used by standard SQLFluff (`AL`, `AM`, `CP`, `CV`, `JJ`, `LT`, `RF`, `ST`, `TQ`, …) and the Redgate-bundled rules (`RG`). Any custom rule whose code uses a reserved prefix is rejected at load time with a warning on stderr:

```
custom rule Rule_RG99 rejected: rule code 'RG99' uses the prefix 'RG' which
is reserved for stock SQLFluff or Redgate rules. Pick a code with a different prefix.
```

The rule is dropped; the rest of the run continues. Pick a prefix that's distinctive to your organisation - e.g. company initials or a project tag.

If your plugin loads but every rule is rejected (or `get_rules` simply returns nothing), Flyway emits a single informational warning:

```
SQLFluff custom rules plugin at '/code-review-rules' contributed no rules
(either get_rules() returned nothing or every rule was rejected for a code collision).
```

## Enabling and configuring custom rules

Custom rules are loaded but, like any other SQLFluff rule, they are only **run** if they are included in the active rule set. The simplest pattern is to list them explicitly in your `sqlfluff.cfg`:

```toml
[sqlfluff]
rules = ambiguous,convention,structure,RG01,RG02,TP01,TP02
```

All the usual SQLFluff mechanisms work for custom rules too. See [Configuring SQLFluff Rules](<Code Review Rules/Configuring SQLFluff Rules>) for downgrading rules to warnings, disabling them, and suppressing them in-file with `-- noqa`.

## What the output looks like

Custom rule violations appear in the CLI summary table as their own engine row, distinct from standard SQLFluff and Redgate rules:

```
+--------------------+-----------+------------------+----------+
| Engine             | Rule Code | Violations Count | Severity |
+--------------------+-----------+------------------+----------+
| SQLFluff           | AM04      | 2                | Error    |
| SQLFluff           | LT01      | 2                | Error    |
| SQLFluff (Redgate) | RG13      | 2                | Error    |
| SQLFluff (Custom)  | TP01      | 2                | Error    |
+--------------------+-----------+------------------+----------+
```

In the JSON/SARIF reports, custom rule violations are tagged with `ruleSource: "custom"`:

```json
{
  "line_no": 1,
  "line_pos": 3,
  "description": "TRUNCATE TABLE is not permitted by company policy (TP01).",
  "code": "TP01",
  "engine": "SQLFluff",
  "ruleSource": "custom",
  "warning": false,
  "help": null,
  "fixes": null
}
```

Note that `help` is always `null` for custom rules. Flyway does not generate help URLs for custom rules as there's no documentation page Redgate can link to.

## Flyway Desktop

Flyway Desktop recognises `check.sqlfluffCustomRulesPath` and will pass the configured path through to `check -code` when running code analysis. The path is read from `flyway.toml` like any other check setting.

## Troubleshooting

| Symptom | Likely cause |
|---------|--------------|
| `The specified SQLFluff custom rules directory not found` | The path in `sqlfluffCustomRulesPath` doesn't exist on disk, or is not a directory. |
| `The specified SQLFluff custom rules directory is not a plugin package (missing __init__.py)` | The directory exists but contains no `__init__.py`. The path must point at the plugin package itself, not its parent. |
| Warning: `custom rule Rule_XX99 rejected: rule code 'XX99' uses the prefix 'XX' which is reserved` | Your rule's code prefix collides with a standard SQLFluff or Redgate prefix. Rename the class (e.g. `Rule_TP99`). |
| Warning: `SQLFluff custom rules plugin … contributed no rules` | `get_rules()` returned nothing, or all rules were rejected. Check the warning above for the underlying reason. |
| `Failed to load SQLFluff custom rules plugin … : <error>` | The plugin couldn't be imported. Usually a syntax error in `__init__.py` or `rules.py`, or a missing dependency. The exception message is included. |
| Custom rule loads but never fires | The rule is not in the active rule set. Add its code (or `all` group) to `rules` in your `sqlfluff.cfg`, or check that it isn't being excluded. |
| `self.<parameter>` is `None` or default in `_eval` | The parameter isn't declared in both `config_keywords` on the rule **and** `get_configs_info` in `__init__.py`. Both are required. |

## Limitations

- **No SQLFluff `autofix`.** Custom rules are loaded as lint-only. Setting `is_fix_compatible = True` on a custom rule has no effect within Flyway.