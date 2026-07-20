---
subtitle: Docker error codes
---

These error codes may be returned when using the [`docker` provisioner](<Configuration/Environments Namespace/Environment Provisioner Setting/Docker Provisioner>).

### `DOCKER_NOT_INSTALLED`

- **Caused by:** Running the `docker` command failed because Docker is not installed, or the `docker` executable is not available on the `PATH`.
- **Solution:** Install Docker and ensure the `docker` command is available on the `PATH` of the machine running Flyway.

### `DOCKER_NOT_RUNNING`

- **Caused by:** The `docker` command could not connect to the Docker daemon.
- **Solution:** Start Docker (or Docker Desktop) and ensure the daemon is running before retrying.

### `DOCKER_EULA_NOT_ACCEPTED`

- **Caused by:** Provisioning a SQL Server or Oracle container without setting `iAgreeToTheDBVendorsEula` to `true`, which Flyway requires to confirm you accept the database vendor's EULA. See [EULA acceptance](<Configuration/Environments Namespace/Environment Provisioner Setting/Docker Provisioner#eula-acceptance>).
- **Solution:** Set `iAgreeToTheDBVendorsEula = true` in the `docker` resolver configuration to confirm acceptance.
