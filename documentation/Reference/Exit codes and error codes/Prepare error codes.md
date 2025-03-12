---
subtitle: Prepare error codes
---

These error codes may be returned when running [`prepare`](<Commands/Prepare>)

### `PREPARE_ABORT_ON_WARNINGS`

- **Caused by:** Deployment warnings detected of a severity which exceed the configured `abortOnWarnings` threshold.
- **Solution:** Either address the relevant warning and re-run the command, or adjust the `abortOnWarnings` threshold.
