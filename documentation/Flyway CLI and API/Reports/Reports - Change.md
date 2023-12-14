---
subtitle: Change Report
---
{% include enterprise.html %}

The change report indicates the difference between applied migration scripts on your target database and pending migrations scripts (ie. the set of instructions you want to use to change your target database).

You might want this to:

* Pre-deployment - check the effect of your pending changes (DBA review)
* Post-deployment -capture a history of changes for retrospective auditing or reporting

![Change report](assets/change_report_screenshot.png)

## Learn more
* [Check concept](Concepts/Check Concept) - how does it work ?
* [Check command](Commands/Check) - What is the command structure ?
* [Tutorial - Using Flyway Check](Tutorials/Tutorial - Using Flyway Check with SQL Server.md) - how do I use it ?
* Get a [Flyway Enterprise Trial here](https://www.red-gate.com/products/flyway/enterprise/trial/) or start a trial with the [auth](Commands/Auth) command.
