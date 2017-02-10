/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.core.internal.dbsupport.mongoscript;

import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.flywaydb.core.DbCategory;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.dbsupport.FlywayMongoScriptException;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.mongo.MongoDatabaseUtil;
import org.flywaydb.core.internal.info.MigrationInfoDumper;
import org.flywaydb.core.internal.resolver.MongoFlywayConfigurationForTests;
import org.flywaydb.core.internal.resolver.mongoscript.MongoScriptMigrationResolver;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.scanner.Scanner;
import org.flywaydb.core.migration.MongoScriptMigrationTestCase;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Test to demonstrate Mongo migration with JavaScript.
 */
@Category(DbCategory.MongoDB.class)
public class MongoScriptMigrationTest extends MongoScriptMigrationTestCase {

    private static final Logger LOG = LoggerFactory.getLogger(MongoScriptMigrationTest.class);

    private String validateLocation = "migration/validate";
    private String commentLocation = "migration/comment";
    private String quoteLocation = "migration/quote";
    private String futureFailedLocation = "migration/future_failed";
    private String failedLocation = "migration/failed";

    @Test
    public void migrate() throws Exception {
        MongoClient client = flyway.getMongoClient();
        MongoDatabase mongoDb = client.getDatabase(getDatabaseName());
        MongoCollection usersCollection = mongoDb.getCollection("users");
        FindIterable fi = usersCollection.find(Document.parse("{age: 5}"));
        assertEquals(3, flyway.migrate());
        assertEquals(true, MongoDatabaseUtil.exists(client, getDatabaseName()));
        assertEquals(true, MongoDatabaseUtil.hasCollection(client, getDatabaseName(), "users"));
        assertEquals(true, fi.iterator().hasNext());
        for (MigrationInfo migrationInfo : flyway.info().applied()) {
            assertChecksum(migrationInfo);
        }
    }

    @Test
    public void repair() throws Exception {
        flyway.setLocations(futureFailedLocation);
        assertEquals(4, flyway.info().all().length);

        try {
            flyway.migrate();
            fail();
        } catch (FlywayException e) {
            // Expected
        }

        LOG.info("\n" + MigrationInfoDumper.dumpToAsciiTable(flyway.info().all()));
        assertEquals("2.0", flyway.info().current().getVersion().toString());
        assertEquals(MigrationState.FAILED, flyway.info().current().getState());

        flyway.repair();
        assertEquals("1.2", flyway.info().current().getVersion().toString());
        assertEquals(MigrationState.SUCCESS, flyway.info().current().getState());
    }

    @Test
    public void repairChecksum() {
        flyway.setLocations(commentLocation);
        Integer checksumA = flyway.info().pending()[0].getChecksum();

        flyway.setLocations(quoteLocation);
        Integer checksumB = flyway.info().pending()[0].getChecksum();

        assertNotEquals(checksumA, checksumB);

        flyway.migrate();

        if (flyway.info().applied()[0].getType() == MigrationType.SCHEMA) {
            assertEquals(checksumB, flyway.info().applied()[1].getChecksum());
        } else {
            assertEquals(checksumB, flyway.info().applied()[0].getChecksum());
        }

        flyway.setLocations(commentLocation);
        flyway.repair();

        if (flyway.info().applied()[0].getType() == MigrationType.SCHEMA) {
            assertEquals(checksumA, flyway.info().applied()[1].getChecksum());
        } else {
            assertEquals(checksumA, flyway.info().applied()[0].getChecksum());
        }
    }

    @Test
    public void target() throws Exception {
        flyway.setTarget(MigrationVersion.fromVersion("1.2"));
        flyway.migrate();
        assertEquals("1.2", flyway.info().current().getVersion().toString());
        assertEquals("Delete Mallory", flyway.info().current().getDescription());

        flyway.setTarget(MigrationVersion.fromVersion("1.0"));
        flyway.migrate();
        assertEquals("1.2", flyway.info().current().getVersion().toString());
        assertEquals("Delete Mallory", flyway.info().current().getDescription());

        flyway.setTarget(MigrationVersion.LATEST);
        flyway.migrate();
        assertEquals("2.0", flyway.info().current().getVersion().toString());
    }

    /**
     * Compares the DB checksum to the classpath checksum of this migration.
     *
     * @param migrationInfo The migration to check.
     */
    private void assertChecksum(MigrationInfo migrationInfo) {
        MongoScriptMigrationResolver mongoScriptMigrationResolver = new MongoScriptMigrationResolver(
                new Scanner(Thread.currentThread().getContextClassLoader()),
                new Location(BASEDIR),
                PlaceholderReplacer.NO_PLACEHOLDERS,
                MongoFlywayConfigurationForTests.create());
        List<ResolvedMigration> migrations = mongoScriptMigrationResolver.resolveMigrations();
        for (ResolvedMigration migration : migrations) {
            if (migration.getVersion().toString().equals(migrationInfo.getVersion().toString())) {
                assertEquals("Wrong checksum for " + migrationInfo.getScript(),
                        migration.getChecksum(), migrationInfo.getChecksum());
            }
        }
    }

    @Test(expected = FlywayException.class)
    public void validateFails() throws Exception {
        flyway.setMongoMigrationSuffix("Add_users.js");
        flyway.migrate();

        assertEquals("1.1", flyway.info().current().getVersion().toString());

        flyway.setIgnoreFutureMigrations(false);
        flyway.setMongoMigrationPrefix("CheckValidate");
        flyway.validate();
    }

    @Test(expected = FlywayException.class)
    public void validateMoreAppliedThanAvailable() throws Exception {
        flyway.migrate();

        assertEquals("2.0", flyway.info().current().getVersion().toString());

        flyway.setLocations(validateLocation);
        flyway.validate();
    }

    @Test
    public void validateClean() throws Exception {
        flyway.setLocations(validateLocation);
        flyway.migrate();

        assertEquals("1.1", flyway.info().current().getVersion().toString());

        flyway.setValidateOnMigrate(true);
        flyway.setCleanOnValidationError(true);
        flyway.setMongoMigrationPrefix("CheckValidate");
        assertEquals(1, flyway.migrate());
    }

    @Test
    public void failedMigration() throws Exception {
        flyway.setLocations(failedLocation);
        try {
            flyway.migrate();
            fail();
        } catch (FlywayMongoScriptException e) {
            System.out.println(e.getMessage());
            // root cause of exception must be defined, and it should be FlywayMongoScriptException
            assertNotNull(e.getCause());
            assertTrue(e.getCause() instanceof MongoException);
            // and make sure the failed statement was properly recorded
            assertEquals(16, e.getLineNumber());
            assertEquals("{invalid: 'command'}", e.getStatement());
        }

        MigrationInfo migration = flyway.info().current();
        MigrationVersion version = migration.getVersion();
        assertEquals("2.0", version.toString());
        assertEquals("Should Fail", migration.getDescription());
        assertEquals(MigrationState.FAILED, migration.getState());

        // With schema migration, we'll have 2 applied
        if (flyway.info().applied()[0].getType() == MigrationType.SCHEMA) {
            assertEquals(2, flyway.info().applied().length);
        } else {
            assertEquals(1, flyway.info().applied().length);
        }
    }

    @Test
    public void futureFailedMigration() throws Exception {
        flyway.setValidateOnMigrate(false);
        flyway.setLocations(futureFailedLocation);

        try {
            flyway.migrate();
            fail();
        } catch (FlywayException e) {
            // Expected
        }

        flyway.setIgnoreFutureMigrations(false);
        flyway.setLocations(BASEDIR);
        try {
            flyway.migrate();
            fail();
        } catch (FlywayException e) {
            // Expected
        }
    }


}
