---
pill: license_permits
subtitle: flyway.licensePermits
---

# License Permits

## Description
Environment variables used to specify a license permit, either by location on disk or the raw permit itself.

## Usage

### Environment Variables
```properties
REDGATE_LICENSING_PERMIT_PATH=path/to/licensePermit.txt
REDGATE_LICENSING_PERMIT=[license permit text here]
```
Depending on where you are running FLyway from, you may run into issues with the length of the permit as some configurations (for example, cmd in Windows) may be unable to store and/or access the full permit in the `REDGATE_LICENSING_PERMIT` environment variable. For this reason we support reading the permit from a file referenced by `REDGATE_LICENSING_PERMIT_PATH`.
