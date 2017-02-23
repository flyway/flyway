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
package org.flywaydb.core.internal.command;

import java.sql.Connection;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.FlywayCallback;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.metadatatable.AppliedMigration;
import org.flywaydb.core.internal.metadatatable.MetaDataTable;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class DbBaselineTest {

    private static final MigrationVersion TEST_BASELINE_VERSION = MigrationVersion.fromVersion("2.0.0");
    private static final String TEST_BASELINE_DESCRIPTION = "test baseline";

    private Connection connection;
    private DbSupport dbSupport;
    private Schema schema;
    private FlywayCallback testCallback;
    private FlywayCallback[] callbacks;
    private MetaDataTable metaDataTable;
    private DbBaseline testBaseline;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void before() {
        this.connection = mock(Connection.class);
        this.dbSupport = mock(DbSupport.class);
        this.schema = mock(Schema.class);
        testCallback = mock(FlywayCallback.class);
        callbacks = new FlywayCallback[] { this.testCallback };
        this.metaDataTable = mock(MetaDataTable.class);

        this.testBaseline = createTestBaselinie(TEST_BASELINE_VERSION);
    }

    @Test
    public void newBaseline() {
        // arrange
        when(this.metaDataTable.hasBaselineMarker()).thenReturn(false);

        // act
        this.testBaseline.baseline();

        // assert
        verify(this.metaDataTable).addBaselineMarker(TEST_BASELINE_VERSION, TEST_BASELINE_DESCRIPTION);
        verify(this.testCallback).beforeBaseline(this.connection);
        verify(this.testCallback).afterBaseline(this.connection);
    }

    @Test
    public void newBaselineWithMigrations() {
        // arrange
        when(this.metaDataTable.hasBaselineMarker()).thenReturn(false);
        when(this.metaDataTable.hasAppliedMigrations()).thenReturn(true);

        // assert
        this.expectedException.expect(FlywayException.class);
        this.expectedException.expectMessage("contains migrations");

        // act
        this.testBaseline.baseline();
    }

    @Test
    public void sameBaselineMarkerPresentWithoutMigrations() {
        // arrange
        AppliedMigration baseline = new AppliedMigration(TEST_BASELINE_VERSION, TEST_BASELINE_DESCRIPTION, MigrationType.BASELINE, "V2.0.0__test-migration.sql", 12345, 100, true);
        when(this.metaDataTable.hasBaselineMarker()).thenReturn(true);
        when(this.metaDataTable.getBaselineMarker()).thenReturn(baseline);
        when(this.metaDataTable.hasAppliedMigrations()).thenReturn(false);

        // act
        this.testBaseline.baseline();

        // assert
        verify(metaDataTable, never()).addBaselineMarker(Mockito.<MigrationVersion>anyObject(), anyString());
    }

    @Test
    public void sameBaselineMarkerPresentWithMigrations() {
        // arrange
        AppliedMigration baseline = new AppliedMigration(TEST_BASELINE_VERSION, TEST_BASELINE_DESCRIPTION, MigrationType.BASELINE, "V2.0.0__test-migration.sql", 12345, 100, true);
        when(this.metaDataTable.hasBaselineMarker()).thenReturn(true);
        when(this.metaDataTable.getBaselineMarker()).thenReturn(baseline);
        when(this.metaDataTable.hasAppliedMigrations()).thenReturn(true);

        // act
        this.testBaseline.baseline();

        // assert
        verify(metaDataTable, never()).addBaselineMarker(Mockito.<MigrationVersion>anyObject(), anyString());
    }

    @Test
    public void differentBaselineMarkerVersionPresent() {
        // arrange
        MigrationVersion baselineVersion = MigrationVersion.fromVersion("3.0.0");
        AppliedMigration baseline = new AppliedMigration(baselineVersion, TEST_BASELINE_DESCRIPTION, MigrationType.BASELINE, "V2.0.0__test-migration.sql", 12345, 100, true);
        when(this.metaDataTable.hasBaselineMarker()).thenReturn(true);
        when(this.metaDataTable.getBaselineMarker()).thenReturn(baseline);

        // assert
        this.expectedException.expect(FlywayException.class);
        this.expectedException.expectMessage(TEST_BASELINE_VERSION.toString());
        this.expectedException.expectMessage(TEST_BASELINE_DESCRIPTION.toString());
        this.expectedException.expectMessage(baselineVersion.toString());

        // act
        this.testBaseline.baseline();
    }

    @Test
    public void differentBaselineMarkerDescriptionPresent() {
        // arrange
        String baselineDescription = "Differen description";
        AppliedMigration baseline = new AppliedMigration(TEST_BASELINE_VERSION, baselineDescription, MigrationType.BASELINE, "V2.0.0__test-migration.sql", 12345, 100, true);
        when(this.metaDataTable.hasBaselineMarker()).thenReturn(true);
        when(this.metaDataTable.getBaselineMarker()).thenReturn(baseline);

        // assert
        this.expectedException.expect(FlywayException.class);
        this.expectedException.expectMessage(TEST_BASELINE_VERSION.toString());
        this.expectedException.expectMessage(TEST_BASELINE_DESCRIPTION.toString());
        this.expectedException.expectMessage(baselineDescription);

        // act
        this.testBaseline.baseline();
    }


    @Test
    public void schemaMarkerPresentAndBaselineVersionZero() {
        // arrange
        DbBaseline versionZeroBaseline = createTestBaselinie(MigrationVersion.fromVersion("0"));
        when(this.metaDataTable.hasSchemasMarker()).thenReturn(true);

        // assert
        this.expectedException.expect(FlywayException.class);
        this.expectedException.expectMessage("used for schema creation");

        // act
        versionZeroBaseline.baseline();
    }

    private DbBaseline createTestBaselinie(MigrationVersion version) {
        return new DbBaseline(connection, dbSupport, metaDataTable, schema, version, TEST_BASELINE_DESCRIPTION, callbacks);
    }

}
