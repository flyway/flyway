---
layout: documentation
menu: grails
subtitle: 'Grails'
redirect_from: /documentation/plugins/grails/
---
# Community Plugins and Integrations: Grails

<img src="/assets/logos/grails.png">

Grails 3.x is based on Spring Boot comes with out-of-the-box <a href="https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto-execute-flyway-database-migrations-on-startup">integration for Flyway</a>.

All you need to do is add `flyway-core` to your `build.gradle`:
<pre class="prettyprint">compile "org.flywaydb:flyway-core:{{ site.flywayVersion }}"</pre>

Spring Boot will then automatically autowire Flyway with its DataSource and invoke it on startup.

You can then configure a good number of Flyway properties <a href="https://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html">directly from your <code>application.properties</code> or <code>application.yml file</code></a>.

<a class="inline-cta" href="https://boxfuse.com/blog/grails-aws"><i class="fa fa-cloud"></i> Want to deploy your Grails 3 apps effortlessly to AWS? Follow our <strong>5 minute</strong> tutorial using <img src="/assets/logo/boxfuse-logo-nano-blue.png"> Boxfuse <i class="fa fa-arrow-right"></i></a>

<p class="next-steps">
    <a class="btn btn-primary" href="/documentation/database/oracle">Oracle <i class="fa fa-arrow-right"></i></a>
</p>