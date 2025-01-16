---
subtitle: Git Resolver
---

{% include teams.html %}

This is a [property resolver](https://documentation.red-gate.com/flyway/flyway-concepts/environments/resolvers) which can be used to derive values based upon git.
The following values are currently supported:

- `branch` - the git branch which is currently checked out
- `commit` - the commit hash of the current commit
- `user` - the git username (`user.name` in git config)
- `email` - the git user email (`user.email` in git config)

To configure this simply reference one of the supported properties within an environment parameter.

One common use of this resolver is to automatically connect to a different development database when switching branch.

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured.

### TOML Configuration File

```toml
[environments.development]
url = "jdbc:sqlserver://localhost:1433;database=MyDatabase_${git.branch:ad};encrypt=true;integratedSecurity=true"
```

