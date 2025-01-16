---
subtitle: 'Tutorial: Personal Access Tokens (PATs)'
---

# Tutorial: Personal Access Tokens (PATs)

This brief tutorial will teach you how to authorize Flyway to use Teams or Enterprise Edition using personal access tokens a.k.a. PATs.

## Introduction

Flyway can be authorized non-interactively by specifying the `email` and `token` parameters for PATs.

## Example: Authorizing Flyway with PATs

Let's assume we have just installed Flyway. Flyway defaults to Community Edition out of the box.

Let's say we have access to Enterprise Edition and we would like to authorize Flyway to use Enterprise features.
First, we must generate our PAT. We can do this by visiting the [Personal Access Tokens page](https://identityprovider.red-gate.com/personaltokens)
on the Redgate identity provider website and clicking on the "New Token" button. Be sure to save your token in a secure
place because once they are generated, they can't be viewed in full again.

We can then specify our Redgate email and PAT using the [email](<Configuration/Flyway Namespace/Flyway Email Setting>) and [token](<Configuration/Flyway Namespace/Flyway Token Setting>) configuration parameters on the commandline,
in the TOML configuration file, or the `FLYWAY_EMAIL` and `FLYWAY_TOKEN` environment variables. Please see the above parameters for full configuration options.
For this example, we'll use the TOML configuration file:

```toml
[flyway]
email = "foo.bar@red-gate.com"
token = "1234ABCD"
```

Now, we can use Flyway normally and Flyway will automatically authorize itself to use Enterprise Edition.
Let's run `flyway version` to verify that:

<pre class="console">
> flyway version

Flyway Enterprise Edition {{ site.flywayVersion }} by Redgate

See release notes here: (https://rd.gt/416ObMi)
</pre>

That's all there is to it! Using PATs, we have successfully authorized Flyway to use Enterprise Edition without
needing to run the `auth` command.

## Summary

In this brief tutorial we saw how to:

- Use PATs to authorize Flyway to use an edition higher than Community Edition
