---
subtitle: Check Code Concept
---
# Check Code Concept
{% include redgate.html %}

## Overview

The `-code` flag produces a report showing the results of running static code analysis over your SQL migrations.

Rules that Flyway comes supplied with can be found in: [Code Analysis Rules](<Usage/Code Analysis Rules>)

For details on how to use code analysis, see the [check -code](<Configuration/Parameters/Flyway/Check/Check Code>) parameter reference

## Analysis engines
At least one needs to be present or else this command will fail with an error.
Enterprise users have the Regular Expression engine available by default but otherwise you will have to install SQLFluff to use this command.

## Analysis engine: SQLFluff

### Requirements 

[SQLFluff](https://www.sqlfluff.com/) needs to be installed on the machine producing the report. 

We currently only integrate with version 1.2.1 and You can install it by running:

```powershell
pip3 install sqlfluff==1.2.1
```

Alternatively, the redgate/flyway docker image comes with this already pre-installed.

### Configuring SQLFluff

If you provide a URL/environment to `check -code` Flyway will use it to automatically determine which SQL dialect to use when analysing your SQL.

If no URL is provided, then you need to configure the dialect in a `.sqlfluff` configuration file.
This file needs to be located in the same location as the migrations being analysed.

You can find more information on configuration in the [SQLFLuff documentation](https://docs.sqlfluff.com/en/stable/configuration.html).

## Analysis engine: Regular Expressions
{% include enterprise.html %}

### Configuring Regular Expression rules

Customers can easily craft their own custom rules or take advantage of the set of rules Flyway provides:
- [Creating Regular Expression Rules](<Learn More/Creating Regular Expression Rules>)
- [Code Analysis Rules](<Usage/Code Analysis Rules>)







