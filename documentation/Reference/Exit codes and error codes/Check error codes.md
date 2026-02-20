---
subtitle: Check error codes
---

These error codes may be returned when running [`check changes`](<Commands/Check/Check Changes>) or [`check drift`](<Commands/Check/Check Drift>).

### `PLUGIN_CHECK_MISSING_BUILD_TARGET`

- **Caused by:** Missing required arguments for the `check changes` or `check drift` operation
- **Solution:** `check changes` requires a `buildEnvironment` or `deployedSnapshot` to be specified. `check drift` requires a `buildEnvironment` or `nextSnapshot` to be specified.

### `CHECK_BUILD_NO_PROVISIONER`

- **Caused by:** No provisioner is configured for the build environment and clean is disabled, so the build environment
  cannot be cleaned or re-provisioned
- **Solution:** Configure a provisioner for the build environment (e.g., `provisioner: "clean"`) or enable clean on the
  build environment (`environments.<environment name>.flyway.cleanDisabled: false`).
