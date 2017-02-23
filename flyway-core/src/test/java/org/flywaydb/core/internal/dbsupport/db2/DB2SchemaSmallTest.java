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
package org.flywaydb.core.internal.dbsupport.db2;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test to demonstrate the migration functionality using DB2.
 */
@Category(DbCategory.DB2.class)
@RunWith(MockitoJUnitRunner.class)
public class DB2SchemaSmallTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private JdbcTemplate jdbcTemplate;

    @Mock
    private DB2DbSupport dbSupport;

    @Test
    public void verifyDropVersioningV9() throws SQLException {
        DB2Schema db2Schema = new DB2Schema(jdbcTemplate, dbSupport, "SCHEMA");

        // Just return an empty list for the other drop statements
        when(jdbcTemplate.queryForStringList(anyString())).thenReturn(Collections.<String>emptyList());
        ResultSet mockResultSet = mock(ResultSet.class);

        // Return empty resultset for Types
        when(jdbcTemplate.getMetaData().getUDTs(null, "SCHEMA", null, null)).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        when(dbSupport.getDb2MajorVersion()).thenReturn(9);

        db2Schema.clean();

        verify(jdbcTemplate, never()).execute("ALTER TABLE SCHEMA.VERSIONED_TABLE DROP VERSIONING");
    }

    @Test
    public void verifyDropVersioningV10() throws SQLException {
        DB2Schema db2Schema = new DB2Schema(jdbcTemplate, dbSupport, "SCHEMA");

        // Just return an empty list for the other drop statements
        when(jdbcTemplate.queryForStringList(anyString())).thenReturn(Collections.<String>emptyList());
        ResultSet mockResultSet = mock(ResultSet.class);

        // Return empty resultset for Types
        when(jdbcTemplate.getMetaData().getUDTs(null, "SCHEMA", null, null)).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Return a VERSIONED_TABLE when the SQL to find version tables is called.
        List<String> versionedTables = new ArrayList<String>();
        versionedTables.add("VERSIONED_TABLE");
        when(jdbcTemplate.queryForStringList("select TABNAME from SYSCAT.TABLES where TEMPORALTYPE <> 'N' and TABSCHEMA = ?", "SCHEMA")).thenReturn(versionedTables);

        when(dbSupport.getDb2MajorVersion()).thenReturn(10);
        when(dbSupport.quote("SCHEMA", "VERSIONED_TABLE")).thenReturn("SCHEMA.VERSIONED_TABLE");

        db2Schema.clean();

        // Verify the DROP VERSIONING SQL is called.
        verify(jdbcTemplate).execute("ALTER TABLE SCHEMA.VERSIONED_TABLE DROP VERSIONING");
    }
}
