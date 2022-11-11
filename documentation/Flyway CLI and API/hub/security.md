---
layout: documentation
menu: hub_security
subtitle: Security information for Flyway Hub
redirect_from: /documentation/hub-security
---

# How we protect your data

Flyway Hub is a hosted service currently running entirely within AWS infrastructure. We operate Flyway Hub from the London data center. Please <a href="mailto:flywayhub@red-gate.com">contact us via email</a> if you'd like us to operate Flyway Hub in multiple regions.

## Security Measures

All Flyway Hub resources are stored in the Flyway Hub cloud which is a multitenant service. Here’s what we do to protect your data:

* All user data and running instances are isolated from each other through industry-standard approaches:
  * Process and filesystem containerisation of running workloads
  * Network isolation between running workloads
* Connections to the Flyway Hub API use OpenID Connect authentication and JSON Web Tokens to ensure users only see their own projects
  * Therefore, users may only see and run their own projects
  * Access to data or workloads that are not associated with your user is prohibited through the isolation measures described above
* GitHub access is controlled via the Redgate Flyway Hub GitHub application
  * An access token is stored in the Flyway Hub cloud, encrypted at rest
  * Access can be revoked at any time through GitHub Applications
  * Files from a given GitHub repository are stored on temporary storage, for the duration of the workload only
* Database containers are created specifically for each workload and removed on completion
  * Database containers only accept connections on a randomly-allocated TCP port
  * Connections to running database containers are enforced through TLS, and the connection encryption is enforced to ensure secure transmission of data
  * Database containers are always configured with unique, cryptographically generated random passwords

If you have any questions, please <a href="mailto:flywayhub@red-gate.com">contact us via email</a>.

<a href="/hub"
        class="btn btn-primary">Get started <i class="fa fa-arrow-right"></i></a>
