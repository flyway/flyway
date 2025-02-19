---
subtitle: flyway.environments.*.resolvers
---

Resolvers allow Flyway to retrieve configuration parameters from other locations at runtime, such as [secrets managers](https://documentation.red-gate.com/flyway/flyway-concepts/secrets-management) and environment variables.
This is not to be confused with [migration resolvers](<Configuration/Flyway Namespace/Flyway Migration Resolvers Setting>).

Flyway comes with support for the following resolvers:
<div id="children">
{% include childPages.html %}
</div>

## Using Resolvers In TOML Configuration Files

The syntax for this is `${resolver-name.resolver-key}`.

For example, to retrieve the value of the `password` key from [Dapr](<Configuration/Environments Namespace/Environment Resolvers Namespace/Dapr Resolver>), you would use `${dapr.password}`.

### Inlining

These may be inlined, e.g.

```toml
[environments.default]
url = "jdbc:sqlserver://${vault.db-endpoint};databaseName=${vault.db-name}"
```

### Escaping

The syntax can be escaped inline using `$${a.b}`.
I.e. `please $${do.not} resolve` will be read as
`please ${do.not} resolve` and Flyway will not attempt to retrieve a value

Alternatively, the whole value can be escaped by wrapping it in `!{ ... }`.
So `!{please ${do.not} resolve}` will be read as `please ${do.not} resolve`.

### Configuring

Some Resolvers require configuration. This is done within the
`resolvers` namespace within your environment. For
example, to configure a [Hashicorp Vault](Configuration/Resolvers/Vault Resolver) instance in a
`development` environment, you would configure it like this:

```toml
[environments.development.resolvers.vault]
url = "http://localhost:8200/v1"
token = "abc.1234567890"
engineName = "secret"
engineVersion = "v2"
```

If configuration of a resolver requires a parameter from another resolver, you must configure the dependent resolver first.
For instance, if the
`token` configuration for Vault comes from a [Dapr](Configuration/Resolvers/Dapr Resolver) secret, you must configure Dapr first:

```toml
[environments.development.resolvers.dapr]
url = "daprUrl"

[environments.development.resolvers.vault]
url = "http://localhost:8200/v1"
token = "${dapr.vault-token}"
engineName = "secret"
engineVersion = "v2"
```

### Filtering

For security reasons, you may wish to filter the resolver value to avoid arbitrary values being inserted into your configuration.
This can be done with filters. The syntax to add a filter is `${resolver-name.resolver-key:filter}`.

The filter can contain one or more of the following, each of which whitelists a certain type of character:

-

`A` - Allows letters (characters in the following Unicode categories: "Uppercase letter (Lu)", "Lowercase letter (Ll)", "Title case letter (Lt)", "Modifier letter (Lm)" or "Other letter (Lo)")

- `a` - Allows ASCII letters
- `D` - Allows Digits (characters in the "Decimal number (Nd)" Unicode category)
- `d` - Allows ASCII digits

For example, if `${my-resolver.my-value}` has a value of `@bc-123`, then `${my-resolver.my-value:AD}` will return
`bc123` because `@` and `-` are not letters or digits.

### Nesting

Resolvers cannot be nested and attempting to do so will result in an error. For example, the following is disallowed:

```toml
[environments.default]
url = "jdbc:sqlserver://localhost;databaseName=${env.DB_NAME_${env.DB_NAME_ENV_SUFFIX}}"
```