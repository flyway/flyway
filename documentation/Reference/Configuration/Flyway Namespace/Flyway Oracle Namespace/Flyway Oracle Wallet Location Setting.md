---
pill: oracleWalletLocation
subtitle: flyway.oracleWalletLocation
---

{% include teams.html %}

## Description

The location on disk of your Oracle wallet.

Note that Oracle Wallet is not currently supported for operations involving Redgate Comparison technology:

* commands such as `diff`, `check`, `prepare`, `snapshot`
* schema model and migration generation operations in Flyway Desktop

## Type

String

## Default

<i>none</i>

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
./flyway -oracle.walletLocation="/User/db/my_wallet" info
```

### TOML Configuration File

```toml
[flyway.oracle]
walletLocation = "/User/db/my_wallet"
```

### Configuration File

```properties
flyway.oracle.walletLocation=/User/db/my_wallet
```

### Environment Variable

```properties
FLYWAY_ORACLE_WALLET_LOCATION=/User/db/my_wallet
```

### API

```java
OracleConfigurationExtension oracleConfigurationExtension = configuration.getPluginRegister().getPlugin(OracleConfigurationExtension.class);
oracleConfigurationExtension.setWalletLocation("/User/db/my_wallet");
```

### Gradle

```groovy
flyway {
    oracleWalletLocation = '/User/db/my_wallet'
}
```

### Maven

```xml
<configuration>
    <oracleWalletLocation>/User/db/my_wallet</oracleWalletLocation>
</configuration>
```
