---
pill: reportEnabled
subtitle: flyway.reportEnabled
---

# Report Enabled
{% include commandlineonly.html %}

## Description
Whether to enable generating a report file.


## Default
false

## Usage

### Commandline
```powershell
./flyway -reportEnabled=true migrate
```

### TOML Configuration File
```toml
[flyway]
reportEnabled = true
```

### Configuration File
```properties
flyway.reportEnabled=true
```

## Notes
The results of `check` will consistently trigger the generation of a report, unaffected by this configuration.