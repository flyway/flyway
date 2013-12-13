package com.googlecode.flyway.core.dbsupport.db2;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import com.googlecode.flyway.core.DbCategory;
import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.dbsupport.JdbcTemplate;

/**
 * Test to demonstrate the migration functionality using DB2.
 */
@Category(DbCategory.DB2.class)
@RunWith(MockitoJUnitRunner.class)
public class DB2SchemaSmallTest {


    private DB2Schema db2Schema;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private JdbcTemplate jdbcTemplate;

    @Mock
    private DbSupport dbSupport;

    @Test
    public void verifyDropVersioningIsCalled() throws SQLException {
        db2Schema = new DB2Schema(jdbcTemplate, dbSupport, "SCHEMA");

        // Just return an empty list for the other drop statements
        when(jdbcTemplate.queryForStringList(anyString())).thenReturn(Collections.<String>emptyList());
        ResultSet mockResultSet = mock(ResultSet.class);

        // Return empty resultset for Types
        when(jdbcTemplate.getMetaData().getUDTs(null, "SCHEMA", null, null)).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Return a VERSIONED_TABLE when the SQL to find version tables is called.
        List<String> versionedTables = new ArrayList<String>();
        versionedTables.add("VERSIONED_TABLE");
        when(jdbcTemplate.queryForStringList("select rtrim(TABNAME) from SYSCAT.TABLES where TEMPORALTYPE <> 'N' and TABSCHEMA = ?", "SCHEMA")).thenReturn(versionedTables);

        when(dbSupport.quote("SCHEMA", "VERSIONED_TABLE")).thenReturn("SCHEMA.VERSIONED_TABLE");

        db2Schema.clean();

        // Verify the DROP VERSIONING SQL is called.
        verify(jdbcTemplate).execute("ALTER TABLE SCHEMA.VERSIONED_TABLE DROP VERSIONING");
    }
}
