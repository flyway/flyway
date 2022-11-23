---
menu: release
subtitle: Releasing Flyway
---
<div id="release">
    <h1>Releasing Flyway (Committers only)</h1>

    <h2>Prequesites</h2>
    <ol>
        <li>GPG installed, available on the PATH and a key generated<br/> (see <a
                href="https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide#SonatypeOSSMavenRepositoryUsageGuide-5.Prerequisites"
               >the Sonatype OSS Maven Repository Usage Guide</a>)
        </li>
        <li>A checkout of Flyway HEAD</li>
        <li>Maven settings.xml contains</li>
        <ul>
            <li>credentials for Sonatype OSS Repo Hosting</li>
            <li>credentials for Google App Engine</li>
            <li>the GPG passphrase key:</li>
        </ul>
    </ol>
    <pre class="prettyprint">&lt;settings&gt;
    &lt;servers&gt;
        &lt;server&gt;
            &lt;id&gt;sonatype-nexus-snapshots&lt;/id&gt;
            &lt;username&gt;myuser&lt;/username&gt;
            &lt;password&gt;XXX&lt;/password&gt;
        &lt;/server&gt;
        &lt;server&gt;
            &lt;id&gt;sonatype-nexus-staging&lt;/id&gt;
            &lt;username&gt;myuser&lt;/username&gt;
            &lt;password&gt;XXX&lt;/password&gt;
        &lt;/server&gt;
        &lt;server&gt;
            &lt;id&gt;flyway-sample-appengine&lt;/id&gt;
            &lt;username&gt;myemail@address.com&lt;/username&gt;
            &lt;password&gt;XXX&lt;/password&gt;
        &lt;/server&gt;
    &lt;/servers&gt;
    &lt;profiles&gt;
        &lt;profile&gt;
            &lt;id&gt;flyway-release&lt;/id&gt;
            &lt;activation&gt;
                &lt;property&gt;
                    &lt;name&gt;releaseVersion&lt;/name&gt;
                &lt;/property&gt;
            &lt;/activation&gt;
            &lt;properties&gt;
                &lt;gpg.passphrase&gt;XXX&lt;/gpg.passphrase&gt;
            &lt;/properties&gt;
        &lt;/profile&gt;
    &lt;/profiles&gt;
&lt;/settings&gt;</pre>


    <h2>Required Steps</h2>
    <ol>
        <li>Checkout the latest version of <strong>flyway/flyway-release</strong></li>
        <li>Invoke <strong>release.cmd &lt;&lt;version&gt;&gt;</strong></li>
        <li>Once the release completes, the artifacts must be promoted to Maven Central by logging in to <a href="https://oss.sonatype.org/">https://oss.sonatype.org/</a></li>
        <li>We select <strong>Staging Repositories</strong> on the left and tick the checkbox in front of the open <strong>org.flywaydb</strong> repository</li>
        <li>We then close it (without message) and release it (without message)</li>
        <li>The site must be now be updated by putting the new version number in <strong>_config.yml</strong> of flywaydb.org</li>
        <li>We also update the <a href="https://documentation.red-gate.com/fd/release-notes-for-flyway-engine-179732572.html">Release Notes</a> by changing the Unreleased Version in the new version with the date of today</li>
        <li>The newly released version should also be removed from the <a href="Learn More/roadmap">Roadmap</a></li>
        <li>Remove the old javadoc from the site by deleting everything under Usage/api/javadoc</li>
        <li>Copy the new javadoc from flyway-release/flyway/flyway-core/target/apidocs to Usage/api/javadoc</li>
        <li>Copy the new sbt plugin from flyway-release/flyway/flyway-sbt/target/scala-2.10/sbt-0.13 to /repo/org/flywaydb/flyway-sbt_2.10_0.13/&lt;&lt;version&gt;&gt;</li>
        <li>Any remaining documentation changes to reflect changes in the code, must now be made to the site</li>
        <li>Write a release announcement for the blog</li>
        <li>Attach the release highlights to the GitHub tag</li>
        <li>Close the GitHub milestone</li>
        <li>Great, we're done ! We can now announce it on Twitter!</li>
        <li>Update the StackOverflow tag for any new functionality</li>
        <li>Update Wikipedia with the correct version</li>
    </ol>
</div>
