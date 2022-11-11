---
layout: documentation
menu: hub_automation
subtitle: Automating migration script checks with Flyway Hub
redirect_from: /documentation/hub-automation
---

# Automating migration script checks

If your migration script checks aren't run before every database deployment, their value diminishes and issues can slip through the cracks. We've made automating migration script checks really easy, especially if you are able to use GitHub actions.

Having these checks run on every commit and/or Pull Request is the best way to catch issues early and save time firefighting.

## GitHub Actions

Flyway Hub provides a wizard to create a GitHub Actions workflow in just a few simple steps. When using Flyway Hub and viewing a project, you can start the wizard by clicking "Configure automated checks". The wizard helps you to:

- Create a <a href="https://docs.github.com/en/actions/security-guides/encrypted-secrets">GitHub Secret</a> containing a Flyway Hub access token
- Commit a <a href="https://docs.github.com/en/actions/automating-builds-and-tests/about-continuous-integration">GitHub Action workflow</a> to your repository, which runs the migration script checks
- Validate your workflow with a manual run

By default with this workflow, your migration script checks will run as a <a href="https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/collaborating-on-repositories-with-code-quality-features/about-status-checks">status check</a> on every Pull Request and every time a change to your migrations folder is committed to your default branch. You can modify the workflow file to use any <a href="https://docs.github.com/en/actions/learn-github-actions/events-that-trigger-workflows">GitHub Actions trigger</a>.

## Other CI Tools

If you do not wish to use GitHub Actions to automate your migration script checks, it is possible to use another CI tool by scripting the <a href="/documentation/hub/commandline">`flywayhub` command line</a>.

To give you an idea of how the the `flywayhub` command line can be used in your CI process, take a look at the <a href="https://github.com/red-gate/flyway-hub-migration-test/blob/main/entrypoint.sh">implementation</a> of the Flyway Hub Github Action. This script requires the following to run:

- **Environment variable**: `FLYWAYHUB_ACCESS_TOKEN` - An access token for Flyway Hub creating by clicking "Configure automated checks".

Would you like to see Flyway Hub integrated into your CI provider? <a href="mailto:flywayhub@red-gate.com">Let us know</a>

<a href="/documentation/hub/security"
        class="btn btn-primary">Security <i class="fa fa-arrow-right"></i></a>
