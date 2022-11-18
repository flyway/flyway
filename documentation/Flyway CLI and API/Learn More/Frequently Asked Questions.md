---
menu: faq
subtitle: FAQ
redirect_from:
- /documentation/faq/
- /documentation/faq.html/
---
<div id="faq">
    <h1>Frequently Asked Questions</h1>
    <ul>
        <li><a href="#defect">I found a defect. Where should I report it?</a></li>
        <li><a href="#feature-request">I have a feature request. Where should I submit it?</a></li>
        <li><a href="#question">I have a question. Where can I ask it?</a></li>
        <li><a href="#downgrade">Does Flyway support undo/downgrade/downward migrations?</a></li>
        <li><a href="#hot-fixes">What is the best strategy for dealing with hot fixes?</a></li>
        <li><a href="#parallel">Can multiple nodes migrate in parallel?</a></li>
        <li><a href="#rollback">Does Flyway perform a roll back if a migration fails?</a></li>
        <li><a href="#multiple-schemas">Does Flyway support multiple schemas?</a></li>
        <li><a href="#osgi?">Does Flyway work with OSGI?</a></li>
        <li><a href="#placeholders">Does Flyway support placeholder replacement?</a></li>
        <li><a href="#spring">Does Flyway depend on Spring?</a></li>
        <li><a href="#outside-changes">Can I make structure changes to the DB outside of Flyway?</a></li>
        <li><a href="#repair">How do you repair the database after a failed migration?</a></li>
        <li><a href="#clean-objects">Why does clean drop individual objects instead of the schema itself?</a></li>
        <li><a href="#db-specific-sql">What is the best strategy for handling database-specific sql?</a></li>
        <li><a href="#case-sensitive">Why is the flyway_schema_history table case-sensitive?</a></li>
        <li><a href="#hibernate-cdi">How can I integrate Flyway with Hibernate in a CDI environment?</a></li>
    </ul>

    <h2 id="defect">I found a defect. Where should I report it?</h2>

    <p>Check the <a href="https://github.com/flyway/flyway/issues?state=open">issue tracker</a> if someone else
        already reported it. If not, <a href="https://github.com/flyway/flyway/issues?state=open">raise a new
            issue</a>. <br/> <br/></p>

    <h2 id="feature-request">I have a feature request. Where should I submit it?</h2>

    <p>Check the <a href="https://github.com/flyway/flyway/issues?state=open">issue tracker</a> if someone else
        already suggested it. If not, <a href="https://github.com/flyway/flyway/issues?state=open">raise a new
            issue</a>. <br/> <br/></p>

    <h2 id="question">I have a question. Where can I ask it?</h2>

    <p>Post your question on <a href="http://stackoverflow.com">StackOverflow</a> under the
        <a class="stackoverflow-tag" href="http://stackoverflow.com/questions/tagged/flyway">flyway</a> tag.
    </p>

    <h2 id="downgrade">What about undo/downgrade/downward migrations?</h2>

    <p>Yes, Flyway does support <a href="Commands/undo">undo migrations</a>.</p>

    <h2 id="hot-fixes">What is the best strategy for dealing with hot fixes?</h2>

    <p>You have a regular release schedule, say once per sprint. Version 7 is live and you are developing version 8.
        Version 8 contains DB Schema Changes. Suddenly hot fix is required for version 7, and it also needs a schema
        change. </p>

    <p><strong>How can you deal with this?</strong></p>

    <p>Even though the code may be branched, the database schema won't. It will have a linear evolution. </p>

    <p>This means that the emergency fix, say version 7.1 needs to be deployed as part of the hot fix AND the new
        version 8. </p>

    <p>By the time version 8 will be deployed, Flyway will recognize that the migration version 7.1 has already be
        applied. It will ignore it and migrate to version 8. </p>

    <p>When recreating the database, everything with be cleanly installed in order: version 7, 7.1 and 8. </p>

    <p><strong>If this isn't an option</strong> you can activate the outOfOrder property to allow Flyway to run the
        migrations out of order and fill the gaps.<br/>
        <br/></p>

    <h2 id="parallel">Can multiple nodes migrate in parallel?</h2>

    <p>Yes! Flyway uses the locking technology of your database to coordinate multiple nodes. This ensures that even if
        multiple instances of your application attempt to migrate the database at the same time, it still works.
        Cluster configurations are fully supported.<br/> <br/></p>

    <h2 id="rollback">Does Flyway perform a roll back if a migration fails?</h2>

    <p>Flyway runs each migration in a separate transaction. In case of failure this transaction is rolled back.
        Unfortunately, today only DB2, PostgreSQL, Derby, EnterpriseDB and to a certain extent SQL Server support DDL
        statements inside a transaction. Other databases such as Oracle will implicitly sneak in a commit before and after
        each DDL statement, drastically reducing the effectiveness of this roll back. One alternative if you want to work
        around this, is to include only a single DDL statement per migration. This solution however has the drawback of
        being quite cumbersome.</p>

    <h2 id="multiple-schemas">Does Flyway support multiple schemas?</h2>

    <p>Yes! These are the recommended strategies for dealing with them:<br/>

    <h3>Multiple identical schemas</h3>

    <p>If you have multiple identical schemas, say one per tenant, invoke Flyway in a loop and change
    <code>flyway.schemas</code> to match the name of the schema of the current tenant.</p>

    <h3>The schemas are distinct, but have the same life-cycle:</h3>

    <p>Use a single Flyway instance. Flyway has support for this built-in. Fill the
        <code>flyway.schemas</code> property with the comma-separated list of schemas you wish to manage. All
        schemas will be tracked using a single schema history table that will be placed in the first schema of the list. Make
        sure the user of the datasource has the necessary grants for all schemas, and prefix the objects (tables, views,
        ...) you reference.</p>

    <h3>The schemas have a distinct life-cycle or must be autonomous and cleanly separated:</h3>

    <p>Use multiple Flyway instances. Each instance manages its own schema and references its own schema history table. Place
        migrations for each schema in a distinct location.</p>

    <p>Schema foo: </p><pre class="prettyprint">locations = /sql/foo
schemas = foo
table = flyway_schema_history</pre>
    <p>Schema bar: </p><pre class="prettyprint">locations = /sql/bar
schemas = bar
table = flyway_schema_history</pre>

    <h2 id="osgi">Does Flyway work with OSGI?</h2>

    <p>Yes! Flyway runs on Equinox and is well suited for OSGi and Eclipse RCP applications.<br/> <br/></p>

    <h2 id="placeholders">Does Flyway support placeholder replacement?</h2>

    <p>Yes! Flyway can replace placeholders in Sql migrations. The default pattern is ${placeholder}. This can be
        configured using the placeholderPrefix and placeholderSuffix properties.<br/> <br/>
      See <a href="../Configuration/Placeholders">Placeholders</a> for more details.</p>

    <h2 id="spring">Does Flyway depend on Spring?</h2>

    <p>No. Flyway has zero required dependences.</p>

    <p>If you have Spring Jdbc on the classpath, Flyway will be able to load Java migrations making use of Spring's
        convenient JdbcTemplate class.<br/> <br/></p>

    <h2 id="outside-changes">Can I make structure changes to the DB outside of Flyway?</h2>

    <p>No. One of the prerequisites for being able to rely on the metadata in the database and having reliable
        migrations is that ALL database changes are made by Flyway. No exceptions. The price for this reliability is
        discipline. Ad hoc changes have no room here as they will literally sabotage your confidence. Even simple things
        like adding an index can trip over a migration if it has already been added manually before.<br/> <br/>
    </p>

    <h2 id="repair">How do you repair the database after a failed migration?</h2>

    <p>If your database supports DDL transactions, Flyway does the work for you.</p>

    <p>If your database doesn't, these are the steps to follow:</p>
    <ol>
        <li>Manually undo the changes of the migration</li>
        <li>Invoke the repair command</li>
        <li>Fix the failed migration</li>
        <li>Try again</li>
    </ol>

    <h2 id="clean-objects">Why does <code>clean</code> drop individual objects instead of the schema itself?</h2>

    <p><code>clean</code> will remove what Flyway created. If Flyway also created the schema itself, <code>clean</code> will drop it. Otherwise it
        will only drop the objects within the schema.</p>

    <h2 id="db-specific-sql">What is the best strategy for handling database-specific sql?</h2>

    <p>Assuming you use Derby in TEST and Oracle in PROD.</p>

    <p>You can use the <code>flyway.locations</code> property. It would look like this:</p>

    <p>TEST (Derby): <code>flyway.locations=sql/common,sql/derby</code></p>

    <p>PROD (Oracle): <code>flyway.locations=sql/common,sql/oracle</code></p>

    <p>You could then have the common statements (V1__Create_table.sql) in common and different copies of the
        DB-specific statements (V2__Alter_table.sql) in the db-specific locations.</p>

    <p>An even better solution, in my opinion, is to have the same DB in prod and test. Yes, you do lose a bit of
        performance, but on the other hand you also eliminate another difference (and potential source of errors)
        between the environments.</p>

    <h2 id="case-sensitive">Why is the flyway_schema_history table case-sensitive?</h2>

    <p>The flyway_schema_history is case-sensitive due to the quotes used in its creation script. This allows for characters not
        supported in identifiers otherwise.</p>

    <p>The name (and case) can be configured through the <code>flyway.table</code> property.</p>

    <p>The table is an internal Flyway implementation detail and not part of the a public API. It can therefore
        change from time to time.</p>

    <h2 id="hibernate-cdi">How can I integrate Flyway with Hibernate in a CDI environment?</h2>

    <p>For Hibernate 4.X see this <a href="http://stackoverflow.com/questions/11071821/cdi-extension-for-flyway">StackOverflow answer</a>.</p>

    <p>For Hibernate 5.X see <a href="https://github.com/flyway/flyway/issues/1981">this issue</a>.</p>
</div>
