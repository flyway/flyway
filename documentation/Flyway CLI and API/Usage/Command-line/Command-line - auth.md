---
pill: cli_auth
subtitle: 'Command-line: auth'
---
# Command-line: auth

Authorizes Flyway to use either Teams or Enterprise Edition.

## Usage

<pre class="console"><span>&gt;</span> flyway auth -IAgreeToTheEula</pre>

By using this option you consent to the [Redgate EULA](https://www.red-gate.com/eula)

## Sample output
<pre class="console">&gt; flyway auth -IAgreeToTheEula

Flyway COMMUNITY Edition {{ site.flywayVersion }} by Redgate

See release notes here: https://rd.gt/416ObMi
You are now licensed for:
Flyway ENTERPRISE Edition {{ site.flywayVersion }} by Redgate
</pre>