---
subtitle: Local Secret
---
# Local Secret
The Local Secret resolver is intended for reading secrets on a development machine from that machine's secret store. The store flyway will use depends on the operating system of the machine.

Note that accessing the credential storage may prompt for a password, and therefore this resolver is not recommended for use in CI systems or other non-interactive workflows.

The local secret resolver uses the following syntax: `${localSecret.key}` where `key` is the name used to identify the secret in the operating system specific secrets manager.

Example:
```TOML
[environments.default]
url = "jdbc:sqlserver://localhost;databaseName=devDb"
user = "myUser"
password = "${localSecret.devDbPassword}"
```

### Windows

The Windows Credential Manager is used. The secret must be stored as a password field inside a "Generic Credential" under "Windows Based Credentials".

### Mac

The Mac keychain will be used. The secret will be read as a generic password (`find-generic-password`) with a service name ("Keychain Item Name") of `Flyway`. The key used by the secret resolver will be the account name for the keychain item.

### Linux

Libsecret is used, if available. The resolver will look in the default keychain. The resolver will match against the following secret schema:
```
product: Flyway
version: 1
```
