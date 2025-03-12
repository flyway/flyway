---
subtitle: Check error codes
---

These error codes may be returned when running [`check changes`](<Commands/Check/Check Changes>) or [`check drift`](<Commands/Check/Check Drift>).

### `PLUGIN_CHECK_MISSING_BUILD_TARGET`

- **Caused by:** Missing required arguments for the `check changes` or `check drift` operation
- **Solution:** `check changes` requires a `buildEnvironment` or `deployedSnapshot` to be specified. `check drift` requires a `buildEnvironment` or `nextSnapshot` to be specified.
