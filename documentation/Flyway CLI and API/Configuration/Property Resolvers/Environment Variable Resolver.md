---
subtitle: Environment Variable
---
# Environment Variable Resolver
{% include commandlineonly.html %}

For parameters in the [environment namespace](Configuration/Parameters/Environments) it is possible to inject variables into the TOML using the environment variable resolver.

This expects an entry of the form `${env.VARIABLE_NAME}`.

_Note: Flyway parameters outside of the environment namespace have their own configuration - see the 'Environment Variable' section of the parameter of interest_
## Example
Set your environment variable:
```shell
export DATABASE_USERNAME=sa
```
This can then be used in the TOML configuration like this:
```toml
...
[environments.mydevdb]
   user = "${env.DATABASE_USERNAME}"
   ...
```

