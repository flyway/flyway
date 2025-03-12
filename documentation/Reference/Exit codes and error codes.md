---
subtitle: Exit Codes
---

## Exit Codes

When Flyway commands fail on the command line, a non-zero exit code will be returned. By default, this will be `1` but some
scenarios will throw unique codes to help easily identify. This is currently a small list of exit codes
but it will expand over time.

### Unspecified Exit Codes
* `1` - Thrown if Flyway fails for a non categorized reason.

### Licensing Exit Codes
* `35` - Thrown if Flyway fails for any licensing related issue.

## Error Codes

When Flyway commands fail, they throw an exception with a message to help you identify the problem. They also contain an error code which users of the API or those who have enabled machine readable output can inspect and handle accordingly. Below are details of each error code under the command that causes it along with a suggested solution.

| Error code categories                                                     | Description                                                                               |
|---------------------------------------------------------------------------|-------------------------------------------------------------------------------------------|
| [General error codes](<Exit codes and error codes/General error codes>)   | These error codes may appear from any command, and are indicative of more general errors. |
| [Check error codes](<Exit codes and error codes/Check error codes>)       | These error codes are surfaced when running `check changes` or `check drift`.             |
| [Prepare error codes](<Exit codes and error codes/Prepare error codes>)   | These error codes are surfaced when running `prepare`.                                    |
| [Validate error codes](<Exit codes and error codes/Validate error codes>) | These error codes are surfaced when running `validate` or `validateWithResult`.           |
