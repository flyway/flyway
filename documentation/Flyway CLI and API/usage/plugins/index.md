---
layout: documentation
menu: plugins
subtitle: Community Plugins and Integrations
redirect_from: /documentation/plugins/
---
# Community Plugins and Integrations

Flyway comes out of the box with integrations for [Maven](/documentation/usage/maven), [Gradle](/documentation/usage/gradle)
and [Docker](/documentation/usage/commandline).

The broader Flyway community however maintains a much larger collection of integrations and plugins. The tools and
platforms being integrated with include application frameworks, CI servers, deployment automation platforms, build tools
and much more.

<div class="row">
    {% include plugin.html name="Ant" logo="ant.svg" url="/documentation/usage/plugins/ant" info="Platform-independent automation tool" %}
    {% include plugin.html name="Bootique" logo="bootique.svg" url="https://github.com/bootique/bootique-flyway" info="Minimally opinionated framework for runnable Java apps" %}
    {% include plugin.html name="Cake" logo="cake.png" url="https://github.com/buthomas/Cake.Flyway" info=" A cross platform build automation system" %}
    {% include plugin.html name="Chef" logo="chef.svg" url="https://supermarket.chef.io/cookbooks/flywaydb" info="Infrastructure Automation" %}
    {% include plugin.html name="Chocolatey" logo="chocolatey.svg" url="https://chocolatey.org/packages/flyway.commandline/" info="Package manager for Windows" %}
    {% include plugin.html name="Dropwizard" logo="dropwizard.png" url="/documentation/usage/plugins/dropwizard" info="Java framework for developing high-performance RESTful web services" %}
    {% include plugin.html name="Grails" logo="grails.svg" url="/documentation/usage/plugins/grails" info="Groovy-based web application framework for the JVM built on top of Spring Boot" %}
    {% include plugin.html name="Homebrew" logo="homebrew.png" url="http://formulae.brew.sh/formula/flyway" info="Package manager for macOS" %}
    {% include plugin.html name="IntelliJ IDEA" logo="intellij.svg" url="https://plugins.jetbrains.com/plugin/8597-flyway-migration-creation" info="Capable and ergonomic Java IDE" %}
    {% include plugin.html name="Jenkins" logo="jenkins.png" url="https://plugins.jenkins.io/flyway-runner" info="Automation server" %}
    {% include plugin.html name="JPABuddy" logo="jpabuddy.png" url="https://www.jpa-buddy.com/" info="Generate Flyway migration scripts directly from your JPA domain objects" %}
    {% include plugin.html name="JUnit" logo="junit.png" url="https://github.com/flyway/flyway-test-extensions" info="Programmer-friendly testing framework for Java" %}
    {% include plugin.html name="Jooby" logo="jooby.png" url="https://jooby.io/modules/flyway/" info="Scalable, fast and modular micro web framework for Java" %}
    {% include plugin.html name="Micronaut" logo="micronaut.png" url="https://github.com/micronaut-projects/micronaut-flyway" info="Modern, JVM-based, full-stack framework" %}
    {% include plugin.html name="Ninja" logo="ninja.png" url="http://www.ninjaframework.org/documentation/working_with_relational_dbs/db_migrations.html" info="Full stack web framework for Java" %}
    {% include plugin.html name="NPM" logo="npm.svg" url="https://www.npmjs.com/package/flywaydb-cli" info="Package manager for JavaScript" %}
    {% include plugin.html name="NuGet" logo="nuget.png" url="https://www.nuget.org/packages/Flyway.CommandLine" info="The package manager for .NET" %}
    {% include plugin.html name="Play" logo="play.png" url="/documentation/usage/plugins/play" info="High velocity web framework for Java and Scala" %}
    {% include plugin.html name="PowerShell" logo="powershell.svg" url="https://github.com/cdavid15/flyway-ps-cli" info="Windows command-line shell designed especially for system administrators" %}
    {% include plugin.html name="Quarkus" logo="quarkus.png" url="https://quarkus.io/guides/flyway-guide" info="Kubernetes Native Java stack tailored for GraalVM & OpenJDK HotSpot" %}
    {% include plugin.html name="SBT" logo="sbt.svg" url="/documentation/usage/plugins/sbt" info="Build tool for Scala projects" %}
    {% include plugin.html name="Spring Boot" logo="springboot.png" url="/documentation/usage/plugins/springboot" info="Framework for production-ready Spring applications" %}
    {% include plugin.html name="Topia" logo="topia.png" url="http://topia.nuiton.org/documentation/flyway_integration.html" info="Tools for Portable and Independent Architecture" %}
    {% include plugin.html name="XL Deploy" logo="xl-deploy.png" url="https://xebialabs.com/plugins/flyway/" info="Enterprise-scale application release automation for any environment" %}
</div>

If you or your company have an actively maintained Flyway plugin or integration that we forgot, let us know by filing an issue against
[flyway/flywaydb.org](https://github.com/flyway/flywaydb.org) and we'll gladly include it.

<p class="next-steps">
    <a class="btn btn-primary" href="/documentation/database/oracle">Oracle <i class="fa fa-arrow-right"></i></a>
</p>