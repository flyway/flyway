---
layout: documentation
menu: dbSupportLevels
subtitle: Database Support Levels
---

# Database Support Levels

As Flyway grows, the core Flyway team adds support for more database engines. This page explains what "support" means, and provides a single lookup to see the degree of support you can get for your chosen engine. 

## The Levels

For each database Flyway works with, we publish a documentation page, like this one for [PostgreSQL](/documentation/database/postgresql). On these pages you'll see a list of "Supported Versions", along with "Support Level". For PostgreSQL, the Support Levels look like this:

<table class="table">
    <tr>
        <th width="25%">Compatible</th>
        <td>✅</td>
    </tr>
    <tr>
        <th width="25%">Certified</th>
        <td>✅</td>
    </tr>
    <tr>
        <th width="25%">Guaranteed</th>
        <td>✅ {% include teams.html %}</td>
    </tr>
</table>

There are three levels of support; Compatible, Certified and Guaranteed. 

### Compatible

"Compatible" means that Flyway has been reported to work with this database engine. The report of compatability may have come from a user producing a forked versison of Flyway, or has been connected using Flyway's database extension mechanism.

When a database is listed as "Compatible" only, it is either a lower priority when dealing with GitHub Issues raised against it, or it is going through the Certification process. 

**Ready to add compatibility for your database?** [Learn how to add compatibility for your database to Flyway](/documentation/contribute/contributingDatabaseSupport)

<hr/>

### Certified

"Certified" means that support for the database has been signed off by the core Flyway team. The database engine will work "out of the box" as part of the normal Flyway package. 

The certification process involves the development of testing infrastructure, test development and real world testing. The process is most often done in conjunction with the creator(s) of those database engines. If you are a database vendor and are interested in having your database certified to work with Flyway, please contact us. 

A database going through the certification process will be marked as "pending".

When a database is listed as "Certified", priority for bug-fixing is increased. However, fixes are often dependent on community input and time-to-resolution cannot be guaranteed.

**Ready to get your database certified?** [Learn how to get your database certified with Flyway](/documentation/learnmore/getting-certified)

<hr/>

### Guaranteed

Guaranteed support is only available to Flyway Teams edition customers.

"Guaranteed" means that support for the database is provided at the level outlined in the end-user license agreement, including priority bug-fixing and prioritised feature requests.

**Ready for guaranteed database support?** You can upgrade to Flyway Teams at anytime by visiting [flywaydb.org/download](https://www.flywaydb.org/download) and purchasing a Flyway Teams license, or by emailing <a href="mailto:sales@flywaydb.org">sales@flywaydb.org</a>.
