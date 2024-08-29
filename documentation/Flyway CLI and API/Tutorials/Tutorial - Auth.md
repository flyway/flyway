---
subtitle: 'Tutorial: Auth'
---
# Tutorial: Auth

This brief tutorial will teach you how to authorize Flyway to use Teams or Enterprise Edition using the `auth` command.

## Introduction

Flyway allows users to authorize for Flyway Teams or Enterprise Edition using a command rather than manually specifying a license key in a configuration file.

## Example: Authorizing Flyway

Let's assume we have just installed Flyway. Flyway defaults to Community Edition when no license key or license permit is specified.

If we run `flyway auth -IAgreeToTheEula` while connected to the internet, Flyway will launch our default web browser prompting us to log in with our Redgate account username
and password. Assuming we log in successfully and our Redgate account is authorized to use Flyway Enterprise Edition, logging in will save a license permit to the `Flyway CLI`
directory of the [Redgate app data folder](Commands/Auth) giving us access to Flyway Enterprise Edition.

Our expected output to the command line should look something like this:

<pre class="console">
You are now licensed for:
Flyway ENTERPRISE Edition {{ site.flywayVersion }} by Redgate
</pre>

We can verify that our newly saved permit has taken effect by running `flyway info`:

<pre class="console">&gt; flyway info

Flyway ENTERPRISE Edition {{ site.flywayVersion }} by Redgate

See release notes here: https://rd.gt/416ObMi
Database: jdbc:h2:mem:db (H2 2.2)
Schema history table "PUBLIC"."flyway_schema_history" does not exist yet
Schema version: << Empty Schema >>

+------------+---------+-------------+------+--------------+---------+----------+
| Category   | Version | Description | Type | Installed On | State   | Undoable |
+------------+---------+-------------+------+--------------+---------+----------+
| Versioned  | 1       | first       | SQL  |              | Pending | No       |
| Repeatable |         | repeatable  | SQL  |              | Pending |          |
+------------+---------+-------------+------+--------------+---------+----------+
</pre>

Using this method, we have successfully authorized Flyway to use Enterprise Edition without needing to deal with license keys or a configuration file.

## Summary

In this brief tutorial we saw how to:

- Use the `auth` command to authorize Flyway to use an edition higher than Community Edition
