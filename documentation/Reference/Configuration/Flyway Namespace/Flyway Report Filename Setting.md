---
pill: reportFilename
subtitle: flyway.reportFilename
---

## Description

Filename for the report file.
The report is a HTML file containing the details of the migration including SQL script execution times and success/failure statuses as well as the results of
`check`.
This filename will also be used for a supplementary JSON file as well, excluding any `.html` or `.htm` extension.
If this is a relative path, it will be resolved relative to your [working directory](<Command-line Parameters/Working Directory Parameter>).

## Type

String

## Default

`report.html`

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

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