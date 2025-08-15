---
subtitle: write output to file
---

## Description

Write the console output to file.


## Type

String

## Default

None

## Usage

### Command-line

```powershell
./flyway info -outputFile='/my/output.txt'
```

### Environment Variable

Not available

### API

Not available

### Gradle

Not available

### Maven

Not available

## Use Cases

Writing the console output to file cane be useful if the output exceeds the console buffer size.
This can easily occur if debug (-X) is enabled.
