---
subtitle: 'Dropwizard'
redirect_from: Usage/Community Plugins and Integrations/dropwizard/
---
# Community Plugins and Integrations: Dropwizard

<img src="assets/dropwizard.png">

## Application startup

While Dropwizard doesn't come with out-of-the-box integration for Flyway you can easily run Flyway on application startup by adding `flyway-core` to your `pom.xml`:
<pre class="prettyprint">&lt;dependency&gt;
    &lt;groupId&gt;org.flywaydb&lt;/groupId&gt;
    &lt;artifactId&gt;flyway-core&lt;/artifactId&gt;
    &lt;version&gt;{{ site.flywayVersion }}&lt;/version&gt;
&lt;/dependency&gt;</pre>

And calling Flyway from your application class:

<pre class="prettyprint" style="font-size: 90%">public class MyApplication extends Application&lt;MyConfiguration&gt; {
    ...

    @Override
    public void run(MyConfiguration configuration, Environment environment) {
        DataSourceFactory dataSourceFactory = configuration.getDataSourceFactory();
        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSourceFactory.getUrl(), dataSourceFactory.getUser(), dataSourceFactory.getPassword());
        flyway.migrate();
        ...
    }
}</pre>

## CLI add-on

Jochen Schalanda has created a great <strong><a href="https://github.com/joschi/dropwizard-flyway">Dropwizard Add-On</a></strong> that adds Flyway commands to the CLI of your Dropwizard application.

The website also comes with comprehensive documentation on usage and configuration.