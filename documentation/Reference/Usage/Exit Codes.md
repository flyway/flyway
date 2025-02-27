---
subtitle: Exit Codes
---

When Flyway commands fail on the command line, a non-zero exit code will be returned. By default, this will be `1` but some
scenarios will throw unique codes to help easily identify. This is currently a small list of exit codes
but it will expand over time.

## Unspecified Exit Codes
* `1` - Thrown if Flyway fails for a non categorized reason.

## Licensing Exit Codes
* `35` - Thrown if Flyway fails for any licensing related issue.
