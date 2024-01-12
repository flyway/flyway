---
pill: outputType
subtitle: output formatting
---
## Description
Change the console output formatting.

 | Value | Purpose |
 | --- | --- |
 | `json` | Output JSON to the console instead of human-readable output. <br/>Errors are included in the JSON payload instead of being sent to `stderr` |

## Default
Human readable console output

## Usage
### Commandline
```pwershell
./flyway info -outputType=json
```

### TOML Configuration File
Not available
### Configuration File
Not available
### Environment Variable
Not available
### API
Not available
### Gradle
Not available
### Maven
Not available

## Use Cases
You will be doing further processing on the flyway output and it is easier to parse structured JSON output that regular console output.
