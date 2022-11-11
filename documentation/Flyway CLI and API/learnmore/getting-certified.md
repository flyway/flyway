---
layout: documentation
menu: gettingCertified
subtitle: Getting your Database Certified
---

# Getting Flyway Certified support for your database

This page provides guidance on the process and criteria for getting your database added to Flyway’s list of “Certified supported” databases. It’s aimed at the vendors and maintainers of relational database management systems (“RDBMS”). 

_If you’re an end user of a database Flyway doesn’t support, and you would like it to, you should let us know by raising an issue on our GitHub repo, or by following the how-to guide for [making your database compatible with Flyway](/documentation/contribute/contributingDatabaseSupport) yourself._

There are three levels of database support; ordered lowest to highest these are **Compatible**, **Certified** and **Guaranteed**. Each reflects the level of confidence we have in our ability to service Flyway users when using those RDBMSs. 

You can read more about [Flyway Database Support Levels](/documentation/learnmore/database-support) but in short, for a database to achieve Certified (or higher) support status, we require it to meet certain criteria. 

## Background

At first glance, adding support for a new engine seems trivial and a lot of the time, mechanically, it is. In fact, when support for an engine isn't available out of the box, we provide end users with [documentation to make Flyway compatible](/documentation/contribute/contributingDatabaseSupport) with their database of choice.     

On principle, we try to make sure any database we list as Certified is available in every edition of Flyway, including our free Community Edition. We want Flyway to be robust and reliable in any application, but stability is especially important for customers of our paid-for editions. These editions help keep the lights on and the Community Edition free. 

One of the things that sets Flyway apart is its extensive and comprehensive approach to testing. Today we have well over 3000 individual tests. These tests ensure that Flyway is dependable in the most demanding infrastructure environments.

Each new database deserves the same degree of testing. As you can imagine, this adds some overhead to Flyway's core development team.

Our relationships with database vendors is an important part of being a stable product, and it's a relationship that also benefits vendors. Today, the majority of databases that have grown in popularity operate on some form of the open-core model, providing paid-for editions of their technology in much the same way as Flyway.

There are [hundreds of database engines](https://db-engines.com/en/ranking) in the world, offering a lot of choice for developers, their specific needs and context of application. When we add support into Flyway for a new database, we strengthen the ecosystems of those vendors and their databases.

## Achieving Certified support

### Criteria
An RDBMS will be considered for Certification when we (the Flyway core team) are satisfied that:

**There is user demand, and value.**

- You can demonstrate that there is end-user (and/or customer) demand for Flyway to work with your RDBMS. This should be backed up with example qualitative data (eg. conversations with existing users or customers), and/or quantitative data (eg. number of “up-votes” on the GitHub issue, or position in the db-engines rankings table).  

**There is technical compatibility.**

- You can demonstrate your RDBMS is technically compatible with Flyway having forked Flyway and followed the Adding Compatibility process. Note that while it’s technically possible, we don’t currently support NoSQL databases due to the degree of architectural change required to Flyway core in order to support them. However, we may do so in the future. Any contributed code requires the author to have signed our CLA.
- You are happy that Flyway’s prescribed migration process matches best-practice for your database, or are able to provide specific guidance in the context of using Flyway with your RDBMS. For example, some Cloud-native RDBMSs with auto-scaling features like geo-replication require the database change-frequency to be limited.    
- You are able to provide documentation to help us understand any nuances specific to your database engine that we need to make Flyway users aware of. For example, special syntax.
- You are able to support us in developing a comprehensive test suite for your database. For example, we require a Dockerized version of your database – or, for Cloud-native databases, a free instance or emulator (with parity) – for us to develop and run our tests against.    
- You are able to support us with real-world beta-testing. As a rough guide, we try to work with ten end-users / organisations in real-world testing a beta version of compatibility before we are able to sign off on certification. This process helps us identify nuances, ensure robustness and develop documentation. 

**We have a line of communication with you.**

- You (as vendor and/or maintainer) are happy to work with us as we develop, support and market compatibility with your database. We believe partnering works best when product management, engineering and marketing are represented on both sides.
- You are happy to work with us on a trust basis. We are happy to enter into Non-disclosure agreements, but rarely find onerous partner on-boarding and contracting to be worth the effort.    

## Process

If you’re satisfied that you have, or will be able to meet the criteria above, the next step is to get in touch by completing the form below.

<iframe src="https://docs.google.com/forms/d/e/1FAIpQLSc4u2O7jrVagqk6oYgiBCBJhsaD8XWwnKe0QG6XfRz_zBHlFA/viewform?embedded=true" width="640" height="1300" frameborder="0" marginheight="0" marginwidth="0">Loading…</iframe>
