---
subtitle: 'Tutorial: offline permits'
---

Flyway Enterprise authenticates by calling Redgate's licensing servers at startup. 
In many organizations, build infrastructure lives behind a firewall that blocks outbound internet access. 
An offline permit solves this: it is a signed file that Flyway reads locally, with no network call required.

In this tutorial we will configure a GitHub Actions workflow to run Flyway Enterprise migrations against a 
PostgreSQL database on a network-isolated runner. We will store the permit securely as an encrypted repository secret and write it to disk at runtime.

By the end you will have:

- A working GitHub Actions pipeline that authenticates Flyway Enterprise without internet access
- A repeatable pattern for rotating expired permits through your secrets manager

---

## What you will need

Before starting, make sure you have:

- A Flyway Enterprise license allocated to you and access to your [Redgate account portal](https://account.red-gate.com)
- A GitHub repository containing a Flyway project with migrations in a `migrations/` directory and a `flyway.toml` file at the root
- A PostgreSQL database reachable from your GitHub Actions runner
- A terminal on your local machine (macOS, Linux, or WSL2 on Windows)

If you have not yet set up a Flyway project, see [Getting started with Flyway](https://documentation.red-gate.com/fd/getting-started-183306605.html) first.

---

## Step 1: Download your offline permit

Navigate to `https://permits.red-gate.com/offline?productCode=63` and sign in with your Redgate account credentials.

Download the permit file and save it to your local machine as `flyway-permit.txt`.

You will notice the file is a large block of text, often several kilobytes. This size is why Flyway reads the permit from a file path rather than accepting it as an inline value; the [Flyway Offline Permit Path setting](https://documentation.red-gate.com/fd/flyway-offline-permit-path-setting-314869837.html) reference page lists all supported configuration methods (TOML, environment variable, Maven plugin, and Java API).

> **Permit expiry:** Offline permits have a finite validity period. When yours expires, Flyway will revert to Community features. Repeat Steps 1–2 to download a fresh permit and update the repository secret; no workflow file changes are needed.

---

## Step 2: Store the permit as a GitHub Actions secret

In your GitHub repository, navigate to **Settings → Secrets and variables → Actions**, then click **New repository secret**.

Open `flyway-permit.txt` in a text editor, select all the content, and copy it to your clipboard.

Enter these values:

| Field | Value                    |
|-------|--------------------------|
| Name  | `FLYWAY_OFFLINE_PERMIT`  |
| Value | the permit file contents |

Click **Add secret**.

GitHub now stores the value encrypted at rest. It will be masked in any workflow log output and will never appear in plain text.

---

## Step 3: Configure the permit path and write the migration workflow

First, add the permit path to your `flyway.toml`. Open the file and add this line under the `[flyway]` section:

```toml
[flyway]
offlinePermitPath = "/flyway/permit.txt"
```

This is the path where we will mount the permit inside the Docker container.

Now create the file `.github/workflows/flyway-migrate.yml` in your repository:

```yaml
name: Flyway Migrate

on:
  push:
    branches: [main]

jobs:
  migrate:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Write offline permit
        run: echo "${{ secrets.FLYWAY_OFFLINE_PERMIT }}" > "$RUNNER_TEMP/flyway-permit.txt"

      - name: Run Flyway migrations
        run: |
          docker run --rm \
            -v "${{ github.workspace }}/migrations:/flyway/sql" \
            -v "${{ github.workspace }}/flyway.toml:/flyway/conf/flyway.toml" \
            -v "$RUNNER_TEMP/flyway-permit.txt:/flyway/permit.txt" \
            -e FLYWAY_URL="jdbc:postgresql://${{ secrets.DB_HOST }}:5432/mydb" \
            -e FLYWAY_USER="${{ secrets.DB_USER }}" \
            -e FLYWAY_PASSWORD="${{ secrets.DB_PASSWORD }}" \
            redgate/flyway migrate
```

Here is what each step does:

**Write offline permit** The permit is written from the repository secret to `$RUNNER_TEMP`, a directory that is private to the current job and is cleaned up when the runner finishes. The file is never committed to your repository.

**Run Flyway migrations** The Flyway Docker image is launched with three volume mounts:

- `migrations/` -> `/flyway/sql` your migration scripts
- `flyway.toml` -> `/flyway/conf/flyway.toml` your project configuration, including the permit path
- `$RUNNER_TEMP/flyway-permit.txt` -> `/flyway/permit.txt` the permit, placed at the path specified in `flyway.toml`

Database credentials are passed as environment variables from repository secrets—never hard coded.

You will also need to add `DB_HOST`, `DB_USER`, and `DB_PASSWORD` as repository secrets (following the same process as Step 2) if you have not done so already.

---

## Step 4: Commit, push, and verify

Commit the workflow file and push to `main`:

```bash
git add .github/workflows/flyway-migrate.yml
git commit -m "ci: add Flyway migration workflow with offline permit"
git push origin main
```

Navigate to the **Actions** tab in your GitHub repository. You will see the **Flyway Migrate** workflow appear and begin running.

Click into the run. In the **Write offline permit** step you will see no output and this is expected, because GitHub masks all secret values in logs.

In the **Run Flyway migrations** step, look for lines similar to:

```
Flyway Enterprise Edition ... by Redgate

Database: jdbc:postgresql://db.internal:5432/mydb (PostgreSQL ...)
Successfully validated 5 migrations (execution time 00:00.042s)
Current version of schema "public": 4
Migrating schema "public" to version 5 - add_orders_index
Successfully applied 1 migration to schema "public", now at version v5 (execution time 00:00.198s)
```

The first line confirms that the Enterprise permit was loaded. If you see `Flyway Community Edition` instead, the permit was not applied. The most common cause is a name mismatch between the secret and the workflow reference. Check that `FLYWAY_OFFLINE_PERMIT` appears identically in both **Settings → Secrets** and the `secrets.FLYWAY_OFFLINE_PERMIT` expression in the workflow.

---

## What we have built

We now have a pipeline that:

1. Retrieves the offline permit from encrypted repository secrets at runtime
2. Writes it to a short-lived, job-scoped file on the runner
3. Mounts the permit at the path declared in `flyway.toml`
4. Runs Flyway Enterprise migrations with no outbound internet access required

---

## Rotating an expired permit

When your permit expires:

1. Download a new permit file from `https://permits.red-gate.com/offline?productCode=63` (Step 1)
2. In GitHub, go to **Settings → Secrets and variables → Actions**, find `FLYWAY_OFFLINE_PERMIT`, and click **Update**
3. Paste the new permit contents and save

The pipeline will use the updated permit on its next run. No changes to the workflow file are required.

---

## Related pages

- [License Permits](https://documentation.red-gate.com/fd/license-permits-224919672.html): an overview of permit types and CI/CD integration examples
- [Flyway Offline Permit Path setting](https://documentation.red-gate.com/fd/flyway-offline-permit-path-setting-314869837.html): the configuration reference for `offlinePermitPath` across all supported methods: command-line, TOML, environment variable, Maven plugin, and Java API
