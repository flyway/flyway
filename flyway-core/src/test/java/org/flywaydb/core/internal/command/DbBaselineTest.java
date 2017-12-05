/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
 */
package org.flywaydb.core.internal.command;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.FlywayCallback;
import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.database.Schema;
import org.flywaydb.core.internal.schemahistory.AppliedMigration;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.*;


public class DbBaselineTest {
    private static final MigrationVersion TEST_BASELINE_VERSION = MigrationVersion.fromVersion("2.0.0");
    private static final String TEST_BASELINE_DESCRIPTION = "test baseline";

    private Connection connection;
    private Database database;
    private Schema schema;
    private FlywayCallback testCallback;
    private List<FlywayCallback> callbacks;
    private SchemaHistory schemaHistory;
    private DbBaseline testBaseline;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void before() {
        this.connection = mock(Connection.class);
        this.database = mock(Database.class);
        this.schema = mock(Schema.class);
        testCallback = mock(FlywayCallback.class);
        callbacks = Collections.singletonList(this.testCallback);
        this.schemaHistory = mock(SchemaHistory.class);
        this.testBaseline = createTestBaselinie(TEST_BASELINE_VERSION);
    }

    @Test
    public void newBaseline() {
        // arrange
        when(this.schemaHistory.hasBaselineMarker()).thenReturn(false);

        // act
        this.testBaseline.baseline();

        // assert
        verify(this.schemaHistory).addBaselineMarker(TEST_BASELINE_VERSION, TEST_BASELINE_DESCRIPTION);
        verify(this.testCallback).beforeBaseline(this.connection);
        verify(this.testCallback).afterBaseline(this.connection);
    }

    @Test
    public void newBaselineWithMigrations() {
        // arrange
        when(this.schemaHistory.hasBaselineMarker()).thenReturn(false);
        when(this.schemaHistory.hasAppliedMigrations()).thenReturn(true);

        // assert
        this.expectedException.expect(FlywayException.class);
        this.expectedException.expectMessage("contains migrations");

        // act
        this.testBaseline.baseline();
    }

    @Test
    public void sameBaselineMarkerPresentWithoutMigrations() {
        // arrange
        AppliedMigration baseline = new AppliedMigration(1, TEST_BASELINE_VERSION, TEST_BASELINE_DESCRIPTION, MigrationType.BASELINE, "V2.0.0__test-migration.sql", 12345, new Date(), "test", 100, true);
        when(this.schemaHistory.hasBaselineMarker()).thenReturn(true);
        when(this.schemaHistory.getBaselineMarker()).thenReturn(baseline);
        when(this.schemaHistory.hasAppliedMigrations()).thenReturn(false);

        // act
        this.testBaseline.baseline();

        // assert
        verify(this.schemaHistory, never()).addBaselineMarker(TEST_BASELINE_VERSION, TEST_BASELINE_DESCRIPTION);
    }

    @Test
    public void sameBaselineMarkerPresentWithMigrations() {
        // arrange
        AppliedMigration baseline = new AppliedMigration(1, TEST_BASELINE_VERSION, TEST_BASELINE_DESCRIPTION, MigrationType.BASELINE, "V2.0.0__test-migration.sql", 12345, new Date(), "test", 100, true);
        when(this.schemaHistory.hasBaselineMarker()).thenReturn(true);
        when(this.schemaHistory.getBaselineMarker()).thenReturn(baseline);
        when(this.schemaHistory.hasAppliedMigrations()).thenReturn(true);

        // act
        this.testBaseline.baseline();

        // assert
        verify(this.schemaHistory, never()).addBaselineMarker(TEST_BASELINE_VERSION, TEST_BASELINE_DESCRIPTION);
    }

    @Test
    public void differentBaselineMarkerVersionPresent() {
        // arrange
        MigrationVersion baselineVersion = MigrationVersion.fromVersion("3.0.0");
        AppliedMigration baseline = new AppliedMigration(1, baselineVersion, TEST_BASELINE_DESCRIPTION, MigrationType.BASELINE, "V2.0.0__test-migration.sql", 12345, new Date(), "test", 100, true);
        when(this.schemaHistory.hasBaselineMarker()).thenReturn(true);
        when(this.schemaHistory.getBaselineMarker()).thenReturn(baseline);

        // assert
        this.expectedException.expect(FlywayException.class);
        this.expectedException.expectMessage(TEST_BASELINE_VERSION.toString());
        this.expectedException.expectMessage(TEST_BASELINE_DESCRIPTION);
        this.expectedException.expectMessage(baselineVersion.toString());

        // act
        this.testBaseline.baseline();
    }

    @Test
    public void differentBaselineMarkerDescriptionPresent() {
        // arrange
        String baselineDescription = "Differen description";
        AppliedMigration baseline = new AppliedMigration(1, TEST_BASELINE_VERSION, baselineDescription, MigrationType.BASELINE, "V2.0.0__test-migration.sql", 12345, new Date(), "test", 100, true);
        when(this.schemaHistory.hasBaselineMarker()).thenReturn(true);
        when(this.schemaHistory.getBaselineMarker()).thenReturn(baseline);

        // assert
        this.expectedException.expect(FlywayException.class);
        this.expectedException.expectMessage(TEST_BASELINE_VERSION.toString());
        this.expectedException.expectMessage(TEST_BASELINE_DESCRIPTION);
        this.expectedException.expectMessage(baselineDescription);

        // act
        this.testBaseline.baseline();
    }


    @Test
    public void schemaMarkerPresentAndBaselineVersionZero() {
        // arrange
        DbBaseline versionZeroBaseline = createTestBaselinie(MigrationVersion.fromVersion("0"));
        when(this.schemaHistory.hasSchemasMarker()).thenReturn(true);

        // assert
        this.expectedException.expect(FlywayException.class);
        this.expectedException.expectMessage("used for schema creation");

        // act
        versionZeroBaseline.baseline();
    }

    private DbBaseline createTestBaselinie(MigrationVersion version) {
        return new DbBaseline(database, schemaHistory, schema, version, TEST_BASELINE_DESCRIPTION, callbacks);
    }

}
