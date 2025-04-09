---
subtitle: flyway.check.failOnDrift
---

{% include enterprise.html %}

## Description

Where drift has been detected as part of Flyway [check](<Commands/Check>), this can be used to cause Flyway to exit with an error (the return/exit code is not 0).
This would typically be used to stop a pipeline from proceeding to subsequent steps because the presence of drift suggests your DB has been changed outside of a flyway deployment pipeline and some action will need to be taken to bring the DB back to a managed state.

## Type

Boolean

## Default

`false`

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

## Usage

```powershell
"./flyway check -drift -check.failOnDrift=true"
```

### TOML Configuration File

```toml
[flyway.check]
failOnDrift = true
```

### Configuration File

```properties
flyway.check.failOnDrift=true
```