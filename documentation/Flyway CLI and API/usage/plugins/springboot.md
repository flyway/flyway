---
layout: documentation
menu: springboot
subtitle: 'Spring Boot'
redirect_from: /documentation/plugins/springboot/
---
# Community Plugins and Integrations: Spring Boot

<img src="/assets/logos/springboot.png" style="margin-bottom: 20px">

Spring Boot comes with out-of-the-box <a href="https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto-execute-flyway-database-migrations-on-startup">integration for Flyway</a>.

All you need to do is add `flyway-core` to either your `pom.xml`:
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
    <version>{{ site.flywayVersion }}</version>
</dependency>
```

Or `build.gradle`:

```groovy
compile "org.flywaydb:flyway-core:{{ site.flywayVersion }}"
```

Spring Boot will then automatically autowire Flyway with its DataSource and invoke it on startup.

You can then configure a good number of Flyway properties [directly from your `application.properties` or `application.yml` file](https://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html).
Spring Boot also lets you configure Flyway using [environment variables](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html).
Just be aware that the names of these environment variables differ from [Flyway's native environment variables](https://flywaydb.org/documentation/configuration/envvars).

Note that if you are using Spring Boot's dependency management feature, you do not need to specify a version number for Flyway. Read more on the [Spring Boot documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#using-boot-dependency-management).

<a class="inline-cta" href="https://boxfuse.com/blog/spring-boot-ec2"><i class="fa fa-cloud"></i> Want to deploy your Spring Boot apps effortlessly to AWS? Follow our <strong>5 minute</strong> tutorial using <img src="/assets/logo/boxfuse-logo-nano-blue.png"> Boxfuse <i class="fa fa-arrow-right"></i></a>

<p class="next-steps">
    <a class="btn btn-primary" href="/documentation/database/oracle">Oracle <i class="fa fa-arrow-right"></i></a>
</p>