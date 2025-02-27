---
subtitle: 'Grails'
redirect_from: Usage/Community Plugins and Integrations/grails/
---

<img src="assets/grails.png">

Grails 3.x is based on Spring Boot comes with out-of-the-box <a href="https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto-execute-flyway-database-migrations-on-startup">integration for Flyway</a>.

All you need to do is add `flyway-core` to your `build.gradle`:
<pre class="prettyprint">implementation "org.flywaydb:flyway-core:{{ site.flywayVersion }}"</pre>

Spring Boot will then automatically autowire Flyway with its DataSource and invoke it on startup.

You can then configure a good number of Flyway properties <a href="https://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html">directly from your <code>application.properties</code> or <code>application.yml file</code></a>.
