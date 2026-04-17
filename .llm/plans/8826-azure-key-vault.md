# Spike: Azure Key Vault Secrets Resolver

**Issue:** [#8826](https://github.com/red-gate/flyway-main/issues/8826)
**Date:** 2026-03-25
**Author:** Piers Williams

## Summary

Adding Azure Key Vault (AKV) as a secrets resolver in Flyway is a **Small** effort. The existing resolver plugin architecture is well-established with 7 resolvers already shipping. AKV follows the same pattern as Google Cloud Secret Manager (GCSM) — a new Maven module with ~5 Java files, ~150–200 lines of main code, and ~80–100 lines of tests. The Azure SDK handles authentication via `DefaultAzureCredential`, so no custom auth code is needed. Flyway Desktop has partial resolver UI infrastructure that can be extended with minimal effort.

---

## T-Shirt Sizing

| Component | Size | Notes |
|-----------|------|-------|
| Maven module setup (`flyway-akv-integration`) | S | Copy GCSM pom.xml, swap Google deps for Azure SDK |
| `AzureKeyVaultResolver` (PropertyResolver) | S | Standard resolve pattern using `SecretClient` |
| `AkvConfigurationProvider` (bulk loading) | S | Iterate secrets array, fetch each from AKV |
| `AkvConfigurationExtension` + `AkvModel` | S | TOML config mapping — mechanical |
| `AzureKeyVaultResolverConfigurationExtension` | S | Record class with `vaultUrl` field |
| SPI registration | S | One file, three class names |
| Unit tests | S | Config extension env var mapping, resolver mock |
| Integration tests (manual/disabled) | S | Requires Azure subscription; disabled by default |
| Documentation (resolver page + tutorial) | S | Follow existing GCSM/Vault doc templates |
| Flyway Desktop UI | S | Add type definition + single-field form component (see Desktop section) |
| **Total** | **S** | **Mechanical — follows established patterns closely** |

**Comparison to existing resolvers:**
- GCSM: 5 main files, 185 LOC main, 78 LOC tests
- Vault: 6 main files, 322 LOC main, 222 LOC tests (more complex due to KV1/KV2 engine handling)
- **AKV estimate: 5 main files, ~150–200 LOC main, ~80–100 LOC tests** (closest to GCSM)

---

## Implementation Scope

### New Maven Module: `flyway-akv-integration`

Located at `flyway-resolver/flyway-akv-integration/`. Following the GCSM pattern exactly.

### Files to Create

| File | Purpose | Reference |
|------|---------|-----------|
| `AzureKeyVaultResolver.java` | PropertyResolver implementation — resolves individual secrets via `${azureKeyVault.secret-name}` | `GoogleSecretsResolver.java` |
| `AkvConfigurationProvider.java` | Bulk secret loading via `flyway.akv.secrets` config array | `GcsmConfigurationProvider.java` |
| `AkvConfigurationExtension.java` | TOML config mapping (`SecretsManagerConfigurationExtension`) | `GcsmConfigurationExtension.java` |
| `AzureKeyVaultResolverConfigurationExtension.java` | Per-resolver config record with `vaultUrl` | `GoogleSecretsResolverConfigurationExtension.java` |
| `AkvModel.java` | Data model: `vaultUrl`, `secrets[]` | `GcsmModel.java` |
| `META-INF/services/org.flywaydb.core.extensibility.Plugin` | SPI registration for all 3 plugin classes | Existing SPI files |
| `pom.xml` | Maven module with Azure SDK dependencies | `flyway-gcsm-integration/pom.xml` |

### Architecture

```
PropertyResolver (flyway-core)
    └── AzureKeyVaultResolver
            ├── Uses SecretClient from Azure SDK
            ├── Auth via DefaultAzureCredential (automatic)
            └── Enterprise-gated via LicenseGuard

ConfigurationProvider<AkvConfigurationExtension>
    └── AkvConfigurationProvider
            ├── Bulk loads secrets listed in flyway.akv.secrets
            └── Returns Map<String, String> of Flyway config

SecretsManagerConfigurationExtension
    └── AkvConfigurationExtension
            ├── Maps TOML config to AkvModel
            └── Handles env var → config parameter mapping
```

---

## Configuration Design

### TOML Configuration

```toml
[environments.production]
url = "jdbc:sqlserver://myserver.database.windows.net:1433;databaseName=mydb"
user = "${azureKeyVault.db-username}"
password = "${azureKeyVault.db-password}"

[environments.production.resolvers.azureKeyVault]
vaultUrl = "https://my-vault.vault.azure.net"
```

### Configuration Properties

| Property | Required | Type | Description |
|----------|----------|------|-------------|
| `vaultUrl` | Yes | String | The Azure Key Vault URL (e.g., `https://my-vault.vault.azure.net`) |

**Authentication** is handled by the Azure SDK's `DefaultAzureCredential`, which tries these methods in order:
1. Environment variables (`AZURE_CLIENT_ID`, `AZURE_CLIENT_SECRET`, `AZURE_TENANT_ID`)
2. Workload Identity (Kubernetes)
3. Managed Identity (Azure VMs, App Service, Functions)
4. Azure CLI (`az login`)
5. Azure PowerShell
6. Azure Developer CLI

No authentication properties needed in TOML — the Azure SDK discovers credentials automatically from the environment.

### Environment Variables

| Environment Variable | Config Parameter |
|---------------------|-----------------|
| `FLYWAY_AKV_VAULT_URL` | `flyway.akv.vaultUrl` |
| `FLYWAY_AKV_SECRETS` | `flyway.akv.secrets` |

### Secret Reference Syntax

Individual secrets: `${azureKeyVault.secret-name}`

Bulk loading (legacy namespace):
```toml
[flyway.akv]
vaultUrl = "https://my-vault.vault.azure.net"
secrets = ["my-flyway-db-config"]
```

### Command-Line Usage

```bash
./flyway info \
  -environment='production' \
  -environments.production.url='jdbc:sqlserver://${azureKeyVault.db-host}:1433' \
  -environments.production.user='${azureKeyVault.db-username}' \
  -environments.production.password='${azureKeyVault.db-password}' \
  -environments.production.resolvers.azureKeyVault.vaultUrl='https://my-vault.vault.azure.net'
```

---

## Draft Documentation Page

### Azure Key Vault Resolver

> `environments.*.resolvers.azureKeyVault`
>
> **Enterprise**
>
> Per-environment Azure Key Vault secret management configuration.
> Values can be inlined in the environment configuration using `${azureKeyVault.key}`.
>
> #### Prerequisites
>
> Azure Key Vault requires authentication. The resolver uses the Azure SDK's [DefaultAzureCredential](https://learn.microsoft.com/en-us/java/api/com.azure.identity.defaultazurecredential) which automatically discovers credentials from the environment. Common setups:
>
> - **Azure-hosted environments** (VMs, App Service, AKS): Enable Managed Identity and grant it access to the Key Vault
> - **CI/CD pipelines**: Set `AZURE_CLIENT_ID`, `AZURE_CLIENT_SECRET`, and `AZURE_TENANT_ID` environment variables
> - **Local development**: Run `az login` (Azure CLI must be installed)
>
> #### Settings
>
> | Setting | Required | Type | Description |
> |---------|----------|------|-------------|
> | `vaultUrl` | Yes | String | The Azure Key Vault URL (e.g., `https://my-vault.vault.azure.net`) |
>
> #### Usage
>
> **Flyway Desktop**
>
> Configure the Azure Key Vault URL in Flyway Desktop's connection settings. Authentication is handled automatically via Azure's DefaultAzureCredential.
>
> **Command-line**
>
> ```bash
> ./flyway info \
>   -environment='production' \
>   -environments.production.url='jdbc:sqlserver://${azureKeyVault.db-host}:1433' \
>   -environments.production.password='${azureKeyVault.db-password}' \
>   -environments.production.resolvers.azureKeyVault.vaultUrl='https://my-vault.vault.azure.net'
> ```
>
> **TOML Configuration File**
>
> ```toml
> [environments.production.resolvers.azureKeyVault]
> vaultUrl = "https://my-vault.vault.azure.net"
>
> [environments.production]
> url = "jdbc:sqlserver://${azureKeyVault.db-host}:1433"
> user = "${azureKeyVault.db-username}"
> password = "${azureKeyVault.db-password}"
> ```

---

## Dependencies

### Direct Dependencies

```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-security-keyvault-secrets</artifactId>
</dependency>
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
</dependency>
```

### Transitive Dependencies

The Azure SDK brings in:
- `com.azure:azure-core` — HTTP client abstraction
- `com.azure:azure-core-http-netty` — Default HTTP client (Netty-based)
- `io.netty:netty-*` — Network I/O
- `com.fasterxml.jackson.core:jackson-*` — JSON processing (may conflict with Flyway's existing `tools.jackson.core:jackson-databind`)

### Dependency Risks

| Risk | Severity | Mitigation |
|------|----------|------------|
| **Jackson version conflict** | Medium | Azure SDK uses `com.fasterxml.jackson`, Flyway uses `tools.jackson.core`. Need to verify compatibility or use exclusions/shading. |
| **Netty dependency size** | Low | Similar to GCSM's Google Cloud SDK footprint. Netty is already a common transitive dependency. |
| **Azure SDK BOM management** | Low | Use `com.azure:azure-sdk-bom` in parent POM's `<dependencyManagement>` for version alignment. Check if the parent POM already manages Azure versions. |

### Estimated JAR Size Impact

Azure SDK JARs add ~5–10 MB to the distribution (comparable to the Google Cloud SDK JARs for GCSM).

---

## Flyway Desktop Impact

**Impact: Small — partial UI infrastructure already exists**

### Backend (Flyway-side)

- Flyway Desktop discovers resolvers via the same Java SPI mechanism as Flyway CLI
- The AKV resolver module just needs to be on the Desktop's classpath
- No changes to `flyway-desktop-secret-storage` or other Desktop-specific modules

### Frontend (Desktop UI)

The Desktop app (Electron + React/TypeScript/MUI) has **partial resolver UI infrastructure**:

- **Type definitions exist** for Vault, GCSM, and Dapr in `resolverEditorModels.ts`, but only Azure AD Interactive has a working UI form
- **All config is form-based** — no raw TOML editor, so any user-facing resolver config needs explicit React components
- **Everything is hardcoded** — no plugin/dynamic discovery for resolver UI; each resolver's fields are manually defined in TypeScript

**What adding AKV to the Desktop UI requires:**

| Change | File | Scope |
|--------|------|-------|
| Add `azureKeyVault` type definition | `shadow-configuration/types/resolvers/resolverEditorModels.ts` | One field: `vaultUrl` |
| Build form component | New file in `shadow-configuration/components/field-editors/` | Single text input for vault URL |
| Wire into environment converter | `shadow-configuration/utils/environment-converters/unresolvedEnvironmentConverter.ts` | Add AKV case |
| Add as option in shadow config modal | `shadow-configuration/ShadowConfigurationModal.tsx` | Add to resolver list |

**Key advantage:** Auth is handled by `DefaultAzureCredential` (environment-based), so the UI only needs one field (`vaultUrl`). This is simpler than the existing Azure AD Interactive resolver which has two fields (Tenant ID, Client ID).

### Draft Docs Update

With Desktop UI support, the documentation page can be updated from:
> "This can't be set in a config file via Flyway Desktop, although it will be honoured."

To:
> "Configure the Azure Key Vault URL in Flyway Desktop's connection settings. Authentication is handled automatically via Azure's DefaultAzureCredential."

---

## Authentication Deep Dive

Azure Key Vault authentication is simpler than Vault's (which requires an explicit token) because the Azure SDK's `DefaultAzureCredential` automatically chains through multiple credential sources.

### Implementation

```java
SecretClient client = new SecretClientBuilder()
    .vaultUrl(vaultUrl)
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();

KeyVaultSecret secret = client.getSecret(secretName);
return secret.getValue();
```

### Credential Chain (in order of precedence)

1. `EnvironmentCredential` — `AZURE_CLIENT_ID` + `AZURE_CLIENT_SECRET` + `AZURE_TENANT_ID`
2. `WorkloadIdentityCredential` — Kubernetes workload identity
3. `ManagedIdentityCredential` — Azure VMs, App Service, Functions, AKS
4. `AzureDeveloperCliCredential` — `azd auth login`
5. `SharedTokenCacheCredential` — Shared token cache
6. `AzureCliCredential` — `az login`
7. `AzurePowerShellCredential` — `Connect-AzAccount`
8. `InteractiveBrowserCredential` — Browser-based login (if enabled)

### Common Deployment Scenarios

| Scenario | Auth Method | Setup Required |
|----------|-------------|----------------|
| Azure VM / App Service | Managed Identity | Enable system-assigned identity, add Key Vault access policy |
| Azure DevOps pipeline | Service Principal (env vars) | Set `AZURE_CLIENT_ID`, `AZURE_CLIENT_SECRET`, `AZURE_TENANT_ID` |
| GitHub Actions | Service Principal (env vars) | Same as above, via GitHub secrets |
| Local development | Azure CLI | Run `az login` |
| Kubernetes (AKS) | Workload Identity | Configure pod identity |

---

## Testing Approach

### Unit Tests

- `AkvConfigurationExtensionSmallTest` — verify environment variable → config parameter mapping
- Mock-based resolver test — verify `resolve()` calls Azure SDK correctly

### Integration Tests (manually run)

- Requires an Azure subscription with a Key Vault provisioned
- Tests disabled by default (matching GCSM/Vault pattern)
- Cover: single secret retrieval, bulk secret loading, error handling for missing secrets

**Note:** Unlike HashiCorp Vault (which can run in Docker), Azure Key Vault cannot be easily containerized for automated integration testing. This matches the GCSM pattern — integration tests require a real cloud account.

---

## Risks & Open Questions

| # | Item | Risk | Notes |
|---|------|------|-------|
| 1 | **Jackson dependency conflict** | Medium | Azure SDK uses `com.fasterxml.jackson`, Flyway uses `tools.jackson.core`. Needs investigation — may require exclusions or shading. |
| 2 | **Azure SDK BOM not in parent POM** | Low | May need to add Azure BOM to `<dependencyManagement>` in the root POM. |
| 3 | **No local testing without Azure** | Low | Same situation as GCSM. Integration tests require real Azure account. |
| 4 | **Resolver name choice** | Low | `azureKeyVault` is long but descriptive. Alternatives: `akv`, `azurevault`. Recommendation: `azureKeyVault` for clarity, with `akv` as an alias via `getAliases()`. |
| 5 | **Azure Managed HSM support** | Future | Azure Managed HSM uses a different API. Out of scope for initial implementation. |
| 6 | **Secret versioning** | Low | Azure KV supports secret versions. Initial implementation should fetch the latest version (matching how GCSM fetches `latest`). Version support can be added later. |

---

## Recommendation

This is a well-scoped, low-risk feature that follows established patterns. The main implementation risk is the Jackson dependency conflict (item 1 above), which should be investigated before starting. Everything else is mechanical — copy the GCSM structure, swap in the Azure SDK, and follow the existing patterns.

**Suggested approach:** Investigate the Jackson dependency conflict first. If no conflicts, the rest is straightforward pattern-following across both Flyway and Desktop codebases.
