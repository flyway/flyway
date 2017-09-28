package org.flywaydb.core.internal.dbsupport;

import org.flywaydb.core.internal.dbsupport.oracle.OracleDbSupport;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class SqlScriptExceptionSmallTest {
    private JdbcTemplate jdbcTemplate;

    @Before
    public void before() {
        this.jdbcTemplate = mock(JdbcTemplate.class);
    }

    @Test(expected = FlywaySqlScriptException.class)
    public void failOnExceptionsByDefault() throws SQLException {
        doThrow(new SQLException()).when(this.jdbcTemplate).executeStatement("SELECT 'fail' FROM DUAL");

        String source = "SELECT 1 FROM DUAL;\n" +
                "SELECT 'fail' FROM DUAL;\n" +
                "SELECT 2 FROM DUAL;\n";

        SqlScript sqlScript = new SqlScript(source, new OracleDbSupport(null));
        sqlScript.execute(this.jdbcTemplate);
    }

    @Test
    public void ignoreExceptionsWhenDirected() throws SQLException {
        doThrow(new SQLException()).when(this.jdbcTemplate).executeStatement("SELECT 'fail' FROM DUAL");

        String source = "SELECT 1 FROM DUAL;\n" +
                "WHENEVER SQLERROR CONTINUE;\n" +
                "SELECT 'fail' FROM DUAL;\n" +
                "SELECT 2 FROM DUAL;\n" +
                "WHENEVER SQLERROR EXIT FAILURE;\n" +
                "SELECT 3 FROM DUAL;\n";

        SqlScript sqlScript = new SqlScript(source, new OracleDbSupport(null));
        sqlScript.execute(this.jdbcTemplate);

        verify(this.jdbcTemplate).executeStatement("SELECT 1 FROM DUAL");
        verify(this.jdbcTemplate).executeStatement("SELECT 2 FROM DUAL");
        verify(this.jdbcTemplate).executeStatement("SELECT 3 FROM DUAL");
    }

    @Test
    public void failOnExceptionWhenDirected() throws SQLException {
        doThrow(new SQLException()).when(this.jdbcTemplate).executeStatement("SELECT 'fail1' FROM DUAL");
        doThrow(new SQLException()).when(this.jdbcTemplate).executeStatement("SELECT 'fail2' FROM DUAL");

        boolean caughtException = false;
        String source = "SELECT 1 FROM DUAL;\n" +
                "WHENEVER SQLERROR CONTINUE;\n" +
                "SELECT 2 FROM DUAL;\n" +
                "SELECT 'fail1' FROM DUAL;\n" +
                "WHENEVER SQLERROR EXIT FAILURE;\n" +
                "SELECT 3 FROM DUAL;\n" +
                "SELECT 'fail2' FROM DUAL;\n" +
                "SELECT 4 FROM DUAL;\n";

        SqlScript sqlScript = new SqlScript(source, new OracleDbSupport(null));
        try {
            sqlScript.execute(this.jdbcTemplate);
        } catch (FlywaySqlScriptException ignore) {
            caughtException = true;
        }

        verify(this.jdbcTemplate).executeStatement("SELECT 1 FROM DUAL");
        verify(this.jdbcTemplate).executeStatement("SELECT 2 FROM DUAL");
        verify(this.jdbcTemplate).executeStatement("SELECT 3 FROM DUAL");
        verify(this.jdbcTemplate, never()).executeStatement("SELECT 4 FROM DUAL");
        assertTrue(caughtException);
    }
}
