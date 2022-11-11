---
layout: documentation
flywayVersion: 5.2.4
---
# Accessing Older Versions of Flyway

Historically, Flyway Community edition artifacts were uploaded to Maven Central and Gradle Plugins, whereas Pro & Enterprise editions were only available through our own server, repo.flywaydb.org.

Since Flyway 6.4.1, Pro & Enterprise edition artifacts are uploaded to Maven Central and Gradle Plugins, alongside Community edition.

Since Flyway 7.0, Flyway Pro & Enterprise editions are renamed to Flyway Teams.

For all versions of Community, and versions of paid editions beyond 6.4.1:
- [Flyway Maven Central listing](https://mvnrepository.com/artifact/org.flywaydb)
- [Flyway Gradle Plugins listing](https://plugins.gradle.org/search?term=flyway)

For versions of Pro & Enterprise prior to 6.4.1, you must use the legacy repository at repo.flywaydb.org.

For example, to access Flyway {{ page.flywayVersion }}:

<div class="tabbable">
    <ul class="nav nav-tabs">
        <li class="active marketing-item"><a href="#tab-community" data-toggle="tab">Community Edition</a>
        </li>
        <li class="marketing-item"><a href="#tab-pro" data-toggle="tab">Pro Edition</a>
        </li>
        <li class="marketing-item"><a href="#tab-enterprise" data-toggle="tab">Enterprise Edition</a>
        </li>
    </ul>
    <div class="tab-content">
        <div class="tab-pane active" id="tab-community">
<table class="table">
    <tr>
        <th>Maven</th>
        <td>
            <pre class="prettyprint">&lt;dependency&gt;
    &lt;groupId&gt;org.flywaydb&lt;/groupId&gt;
    &lt;artifactId&gt;flyway-core&lt;/artifactId&gt;
    &lt;version&gt;{{ page.flywayVersion }}&lt;/version&gt;
&lt;/dependency&gt;</pre>
        </td>
    </tr>
    <tr>
        <th>Gradle</th>
        <td>
            <pre class="prettyprint">compile "org.flywaydb:flyway-core:{{ page.flywayVersion }}"</pre>
        </td>
    </tr>
    <tr>
        <th>Binary</th>
        <td>
            <a class="btn btn-primary btn-download" href="/download/thankyou?dl=https://repo1.maven.org/maven2/org/flywaydb/flyway-core/{{page.flywayVersion}}/flyway-core-{{page.flywayVersion}}.jar"><i class="fa fa-download"></i> flyway-core-{{page.flywayVersion}}.jar</a>
            <a class="note" href="https://repo1.maven.org/maven2/org/flywaydb/flyway-core/{{page.flywayVersion}}/flyway-core-{{page.flywayVersion}}.jar.md5">md5</a>
            <a class="note" href="https://repo1.maven.org/maven2/org/flywaydb/flyway-core/{{page.flywayVersion}}/flyway-core-{{page.flywayVersion}}.jar.sha1">sha1</a>
        </td>
    </tr>
    <tr>
        <th>Sources</th>
        <td>
            <a class="btn btn-primary btn-download" href="/download/thankyou?dl=https://repo1.maven.org/maven2/org/flywaydb/flyway-core/{{page.flywayVersion}}/flyway-core-{{page.flywayVersion}}-sources.jar"><i class="fa fa-download"></i> flyway-core-{{page.flywayVersion}}-sources.jar</a>
            <a class="note" href="https://repo1.maven.org/maven2/org/flywaydb/flyway-core/{{page.flywayVersion}}/flyway-core-{{page.flywayVersion}}-sources.jar.md5">md5</a>
            <a class="note" href="https://repo1.maven.org/maven2/org/flywaydb/flyway-core/{{page.flywayVersion}}/flyway-core-{{page.flywayVersion}}-sources.jar.sha1">sha1</a>
        </td>
    </tr>
</table>
        </div>
        <div class="tab-pane" id="tab-pro">
<table class="table">
    <tr>
        <th>Maven</th>
        <td>
            <code>&lt;project-dir&gt;/pom.xml</code>
            <pre class="prettyprint" style="font-size: 80%">&lt;repositories&gt;
    &lt;repository&gt;
        &lt;id&gt;flyway-repo&lt;/id&gt;
        &lt;url&gt;https://repo.flywaydb.org/repo&lt;/url&gt;
    &lt;/repository&gt;
    ...
&lt;/repositories&gt;

&lt;dependencies&gt;
    &lt;dependency&gt;
        &lt;groupId&gt;org.flywaydb<strong>.pro</strong>&lt;/groupId&gt;
        &lt;artifactId&gt;flyway-core&lt;/artifactId&gt;
        &lt;version&gt;{{ page.flywayVersion }}&lt;/version&gt;
    &lt;/dependency&gt;
    ...
&lt;/dependencies&gt;</pre>
            <code>&lt;user-home&gt;/.m2/settings.xml</code>
            <pre class="prettyprint" style="font-size: 80%">&lt;settings&gt;
    &lt;servers&gt;
        &lt;server&gt;
            &lt;id&gt;flyway-repo&lt;/id&gt;
            &lt;username&gt;<a href="" data-toggle="modal" data-target="#flyway-trial-license-modal"><i>your-flyway-license-key</i></a>&lt;/username&gt;
            &lt;password&gt;flyway&lt;/password&gt;
        &lt;/server&gt;
    &lt;/servers&gt;
    ...
&lt;/settings&gt;</pre>
        </td>
    </tr>
    <tr>
        <th>Gradle</th>
        <td>
            <pre class="prettyprint" style="font-size: 80%">repositories {
    maven {
        url "https://repo.flywaydb.org/repo"
        credentials {
            username '<a href="" data-toggle="modal" data-target="#flyway-trial-license-modal"><i>your-flyway-license-key</i></a>'
            password 'flyway'
        }
    }
}

dependencies {
    compile "org.flywaydb<strong>.pro</strong>:flyway-core:{{ page.flywayVersion }}"
}</pre>
        </td>
    </tr>
    <tr>
        <th>Binary</th>
        <td>
            <a class="btn btn-primary btn-download" href="/download/thankyou?dl=https://repo.flywaydb.org/repo/org/flywaydb/pro/flyway-core/{{page.flywayVersion}}/flyway-core-{{page.flywayVersion}}.jar"><i class="fa fa-download"></i> flyway-core-{{page.flywayVersion}}.jar</a>
            <a class="note" href="https://repo.flywaydb.org/repo/org/flywaydb/pro/flyway-core/{{page.flywayVersion}}/flyway-core-{{page.flywayVersion}}.jar.md5">md5</a>
            <a class="note" href="https://repo.flywaydb.org/repo/org/flywaydb/pro/flyway-core/{{page.flywayVersion}}/flyway-core-{{page.flywayVersion}}.jar.sha1">sha1</a>
        </td>
    </tr>
    <tr>
        <th>Sources</th>
        <td>
            <a class="btn btn-primary btn-download" href="/download/thankyou?dl=https://repo.flywaydb.org/repo/org/flywaydb/pro/flyway-core/{{page.flywayVersion}}/flyway-core-{{page.flywayVersion}}-sources.jar"><i class="fa fa-download"></i> flyway-core-{{page.flywayVersion}}-sources.jar</a>
            <a class="note" href="https://repo.flywaydb.org/repo/org/flywaydb/pro/flyway-core/{{page.flywayVersion}}/flyway-core-{{page.flywayVersion}}-sources.jar.md5">md5</a>
            <a class="note" href="https://repo.flywaydb.org/repo/org/flywaydb/pro/flyway-core/{{page.flywayVersion}}/flyway-core-{{page.flywayVersion}}-sources.jar.sha1">sha1</a>
        </td>
    </tr>
</table>
        </div>
        <div class="tab-pane" id="tab-enterprise">
<table class="table">
    <tr>
        <th>Maven</th>
        <td>
            <code>&lt;project-dir&gt;/pom.xml</code>
            <pre class="prettyprint" style="font-size: 80%">&lt;repositories&gt;
    &lt;repository&gt;
        &lt;id&gt;flyway-repo&lt;/id&gt;
        &lt;url&gt;https://repo.flywaydb.org/repo&lt;/url&gt;
    &lt;/repository&gt;
    ...
&lt;/repositories&gt;

&lt;dependencies&gt;
    &lt;dependency&gt;
        &lt;groupId&gt;org.flywaydb<strong>.enterprise</strong>&lt;/groupId&gt;
        &lt;artifactId&gt;flyway-core&lt;/artifactId&gt;
        &lt;version&gt;{{ page.flywayVersion }}&lt;/version&gt;
    &lt;/dependency&gt;
    ...
&lt;/dependencies&gt;</pre>
            <code>&lt;user-home&gt;/.m2/settings.xml</code>
            <pre class="prettyprint" style="font-size: 80%">&lt;settings&gt;
    &lt;servers&gt;
        &lt;server&gt;
            &lt;id&gt;flyway-repo&lt;/id&gt;
            &lt;username&gt;<a href="" data-toggle="modal" data-target="#flyway-trial-license-modal"><i>your-flyway-license-key</i></a>&lt;/username&gt;
            &lt;password&gt;flyway&lt;/password&gt;
        &lt;/server&gt;
    &lt;/servers&gt;
    ...
&lt;/settings&gt;</pre>
        </td>
    </tr>
    <tr>
        <th>Gradle</th>
        <td>
            <pre class="prettyprint" style="font-size: 80%">repositories {
    maven {
        url "https://repo.flywaydb.org/repo"
        credentials {
            username '<a href="" data-toggle="modal" data-target="#flyway-trial-license-modal"><i>your-flyway-license-key</i></a>'
            password 'flyway'
        }
    }
}

dependencies {
    compile "org.flywaydb<strong>.enterprise</strong>:flyway-core:{{ page.flywayVersion }}"
}</pre>
        </td>
    </tr>
    <tr>
        <th>Binary</th>
        <td>
            <a class="btn btn-primary btn-download" href="/download/thankyou?dl=https://repo.flywaydb.org/repo/org/flywaydb/enterprise/flyway-core/{{page.flywayVersion}}/flyway-core-{{page.flywayVersion}}.jar"><i class="fa fa-download"></i> flyway-core-{{page.flywayVersion}}.jar</a>
            <a class="note" href="https://repo.flywaydb.org/repo/org/flywaydb/enterprise/flyway-core/{{page.flywayVersion}}/flyway-core-{{page.flywayVersion}}.jar.md5">md5</a>
            <a class="note" href="https://repo.flywaydb.org/repo/org/flywaydb/enterprise/flyway-core/{{page.flywayVersion}}/flyway-core-{{page.flywayVersion}}.jar.sha1">sha1</a>
        </td>
    </tr>
    <tr>
        <th>Sources</th>
        <td>
            <a class="btn btn-primary btn-download" href="/download/thankyou?dl=https://repo.flywaydb.org/repo/org/flywaydb/enterprise/flyway-core/{{page.flywayVersion}}/flyway-core-{{page.flywayVersion}}-sources.jar"><i class="fa fa-download"></i> flyway-core-{{page.flywayVersion}}-sources.jar</a>
            <a class="note" href="https://repo.flywaydb.org/repo/org/flywaydb/enterprise/flyway-core/{{page.flywayVersion}}/flyway-core-{{page.flywayVersion}}-sources.jar.md5">md5</a>
            <a class="note" href="https://repo.flywaydb.org/repo/org/flywaydb/enterprise/flyway-core/{{page.flywayVersion}}/flyway-core-{{page.flywayVersion}}-sources.jar.sha1">sha1</a>
        </td>
    </tr>
</table>
        </div>
    </div>
</div>
