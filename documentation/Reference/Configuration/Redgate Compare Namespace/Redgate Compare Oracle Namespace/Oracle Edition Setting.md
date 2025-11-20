---
subtitle: redgateCompare.oracle.filterFile
---

## Description

An Oracle Edition. Setting this makes it possible to compare objects within a specific edition in addition to the base edition.

For example, if you have two schemas, `HR` and `CUSTOM_EDITION`, and you run the following SQL:
```oraclesqlplus
ALTER USER HR ENABLE EDITIONS;
CREATE EDITION CUSTOM_EDITION;
ALTER SESSION SET EDITION = ORA$BASE;
CREATE VIEW HR.VIEW_1 AS SELECT 'Base edition' "Defined in" FROM DUAL;
ALTER SESSION SET EDITION = CUSTOM_EDITION;
CREATE VIEW HR.VIEW_2 AS SELECT 'My edition' "Defined in" FROM DUAL;
ALTER SESSION SET EDITION = ORA$BASE;
```
then by default Flyway would only track `VIEW_1`.
If this setting is set to `CUSTOM_EDITION`, then `ALTER SESSION SET EDITION = CUSTOM_EDITION;` will be run before performing comparisons, so that `VIEW_2` will also be picked up.  

## Type

String

## Default

<i>none</i>

## Usage

### Flyway Desktop

This can't currently be configured from Flyway Desktop, although it will be honoured.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.edition=CUSTOM_EDITION
```

### TOML Configuration File

```toml
[redgateCompare.oracle]
edition = "CUSTOM_EDITION"
```
