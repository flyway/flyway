---
layout: documentation
menu: configuration
pill: oracleWalletLocation
subtitle: flyway.oracleWalletLocation
---

# Oracle Wallet Location
{% include teams.html %}

## Description
The location on disk of your Oracle wallet.

## Default
null

## Usage

### Commandline
```powershell
./flyway -oracle.walletLocation="/User/db/my_wallet" info
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
Flyway.configure()
    .oracleWalletLocation("/User/db/my_wallet")
    .load()
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
