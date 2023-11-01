---
pill: reportFilename
subtitle: flyway.reportFilename
---

# Report Filename

## Description
Filename for the report file. The report is a html file containing the details of the migration including SQL script execution times and success/failure statuses as well as the results of `check`.
This filename will also be used for a supplementary json file as well, excluding any html or htm extension.


## Default
report.html

## Usage

### Commandline
```powershell
./flyway -reportFilename=flyway_report.html info
```

### TOML Configuration File
```toml
[flyway]
reportFilename = "flyway_report.html"
```

### Configuration File
```properties
flyway.reportFilename=flyway_report.html
```

### Environment Variable
```properties
FLYWAY_REPORT_FILENAME=flyway_report.html
```

## Notes
Empty `reportFilename` will be replaced by the default value.