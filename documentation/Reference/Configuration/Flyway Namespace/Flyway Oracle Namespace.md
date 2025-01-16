---
subtitle: flyway.oracle
---

This namespace contains the configurations specific to Oracle databases.

## Settings

| Setting                                                                                                                   | Tier  | Type    | Description                                                                                                                           |
|---------------------------------------------------------------------------------------------------------------------------|-------|---------|---------------------------------------------------------------------------------------------------------------------------------------|
| [`kerberosCacheFile`](<Configuration/Flyway Namespace/Flyway Oracle Namespace/Flyway Oracle Kerberos Cache File Setting>) | Teams | String  | The location of the `krb5cc_<UID>` credential cache file for use in Kerberos authentication.                                          |
| [`sqlplus`](<Configuration/Flyway Namespace/Flyway Oracle Namespace/Flyway Oracle SQLPlus Setting>)                       | Teams | Boolean | Enable Flyway's support for Oracle SQL*Plus commands.                                                                                 |
| [`sqlplusWarn`](<Configuration/Flyway Namespace/Flyway Oracle Namespace/Flyway Oracle SQLPlus Warn Setting>)              | Teams | Boolean | Whether Flyway should issue a warning instead of an error whenever it encounters an Oracle SQL*Plus statement it doesn't yet support. |
| [`walletLocation`](<Configuration/Flyway Namespace/Flyway Oracle Namespace/Flyway Oracle Wallet Location Setting>)        | Teams | String  | The location on disk of your Oracle wallet.                                                                                           |
