/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.database;

import org.flywaydb.core.internal.database.db2.DB2SqlStatementBuilder;
import org.flywaydb.core.internal.database.derby.DerbySqlStatementBuilder;
import org.flywaydb.core.internal.database.h2.H2SqlStatementBuilder;
import org.flywaydb.core.internal.database.oracle.OracleSqlStatementBuilder;
import org.flywaydb.core.internal.database.postgresql.PostgreSQLSqlStatementBuilder;
import org.flywaydb.core.internal.database.sqlserver.SQLServerSqlStatementBuilder;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test for SqlStatementBuilder.
 */
public class SqlStatementBuilderSmallTest {
    @Test
    public void postgreSqlEndsWithOpenMultilineStringLiteral() {
        assertTrue(endsWithOpenMultilineStringLiteral(createPostgreSQLBuilder(), "INSERT INTO address VALUES (1, '1. first"));
        assertFalse(endsWithOpenMultilineStringLiteral(createPostgreSQLBuilder(), "INSERT INTO address VALUES (1, '1. first\n" +
                "2. second');"));
    }

    private PostgreSQLSqlStatementBuilder createPostgreSQLBuilder() {
        return new PostgreSQLSqlStatementBuilder(new Delimiter(";", false));
    }

    @Test
    public void sqlServerEndsWithOpenMultilineStringLiteral() {
        assertFalse(endsWithOpenMultilineStringLiteral(createSQLServerBuilder(), "print 'baz'+"));
        assertFalse(endsWithOpenMultilineStringLiteral(createSQLServerBuilder(), "CUSTOMER set creaon_date = {ts '3099-01-01 00:00:00'} FROM CUSTOMER c inner join inserted i on c.id=i.id"));
        assertFalse(endsWithOpenMultilineStringLiteral(createSQLServerBuilder(), "print 'baz'-- Oops"));
    }

    private SQLServerSqlStatementBuilder createSQLServerBuilder() {
        return new SQLServerSqlStatementBuilder(new Delimiter("GO", true));
    }

    @Test
    public void h2EndsWithOpenMultilineStringLiteral() {
        assertFalse(endsWithOpenMultilineStringLiteral(createH2Builder(), "select * from t;"));
        assertFalse(endsWithOpenMultilineStringLiteral(createH2Builder(), "select * from t where a='xyz';"));
        assertFalse(endsWithOpenMultilineStringLiteral(createH2Builder(), "select * from t where a= 'xyz';"));
        assertFalse(endsWithOpenMultilineStringLiteral(createH2Builder(), "select * from t where a='xyz'"));
        assertFalse(endsWithOpenMultilineStringLiteral(createH2Builder(), "select * from t where a='$$';"));
        assertFalse(endsWithOpenMultilineStringLiteral(createH2Builder(), "select * from t where a='xy''z';"));
        assertFalse(endsWithOpenMultilineStringLiteral(createH2Builder(), "select * from t where a='xyz' and b like 'abc%';"));
        assertFalse(endsWithOpenMultilineStringLiteral(createH2Builder(), "select * from t where a=' xyz ';"));
        assertFalse(endsWithOpenMultilineStringLiteral(createH2Builder(), "select * from t where a=';';"));
        assertFalse(endsWithOpenMultilineStringLiteral(createH2Builder(), "select * from t where a='$$''$$';"));
        assertFalse(endsWithOpenMultilineStringLiteral(createH2Builder(), "select * from t where a='$$' || '$$';"));
        assertFalse(endsWithOpenMultilineStringLiteral(createH2Builder(), "select * from t where a='$$'||'$$';"));
        assertFalse(endsWithOpenMultilineStringLiteral(createH2Builder(), "INSERT INTO test_user (name) VALUES ('Mr. T');"));
        assertFalse(endsWithOpenMultilineStringLiteral(createH2Builder(), "INSERT INTO test_user (name) VALUES ('Mr. Semicolon;');"));
        assertFalse(endsWithOpenMultilineStringLiteral(createH2Builder(), "INSERT INTO test_user (id, name) VALUES (1, 'Mr. Semicolon;');"));
        assertFalse(endsWithOpenMultilineStringLiteral(createH2Builder(), "insert into TAB1 (GUID, UID, VAL) values (1, '0100', 100);"));
        assertTrue(endsWithOpenMultilineStringLiteral(createH2Builder(), "select * from t where a='$$||''$$;"));
        assertTrue(endsWithOpenMultilineStringLiteral(createH2Builder(), "select * from t where a='"));
        assertTrue(endsWithOpenMultilineStringLiteral(createH2Builder(), "select * from t where a='abc"));
        assertTrue(endsWithOpenMultilineStringLiteral(createH2Builder(), "select * from t where a='abc''"));
        assertTrue(endsWithOpenMultilineStringLiteral(createH2Builder(), "select * from t where a='abc'''||'"));
        assertTrue(endsWithOpenMultilineStringLiteral(createH2Builder(), "INSERT INTO test_user (name) VALUES ('Mr. Semicolon+Linebreak;"));
    }

    private H2SqlStatementBuilder createH2Builder() {
        return new H2SqlStatementBuilder(new Delimiter(";", false));
    }

    @Test
    public void oracleEndsWithOpenMultilineStringLiteral() {
        assertFalse(endsWithOpenMultilineStringLiteral(createOracleBuilder(), "select q'[Hello 'quotes']' from dual;"));
        assertFalse(endsWithOpenMultilineStringLiteral(createOracleBuilder(), "select q'(Hello 'quotes')' from dual;"));
        assertFalse(endsWithOpenMultilineStringLiteral(createOracleBuilder(), "select q'{Hello 'quotes'}' from dual;"));
        assertFalse(endsWithOpenMultilineStringLiteral(createOracleBuilder(), "select q'<Hello 'quotes'>' from dual;"));
        assertFalse(endsWithOpenMultilineStringLiteral(createOracleBuilder(), "select q'$Hello 'quotes'$' from dual;"));

        assertFalse(endsWithOpenMultilineStringLiteral(createOracleBuilder(), "COMMENT ON COLUMN SATZ_MARKE.KURZZEICHEN IS 'Kurzzeichen';"));

        assertTrue(endsWithOpenMultilineStringLiteral(createOracleBuilder(), "select q'[Hello 'quotes']"));
        assertTrue(endsWithOpenMultilineStringLiteral(createOracleBuilder(), "select q'(Hello 'quotes')"));
        assertTrue(endsWithOpenMultilineStringLiteral(createOracleBuilder(), "select q'{Hello 'quotes'}"));
        assertTrue(endsWithOpenMultilineStringLiteral(createOracleBuilder(), "select q'<Hello 'quotes'>"));
        assertTrue(endsWithOpenMultilineStringLiteral(createOracleBuilder(), "select q'$Hello 'quotes'$"));
    }

    private OracleSqlStatementBuilder createOracleBuilder() {
        return new OracleSqlStatementBuilder(new Delimiter(";", false));
    }

    @Test
    public void oracleEndsWithOpenMultilineStringLiteralComplex() {
        assertFalse(endsWithOpenMultilineStringLiteral(createOracleBuilder(), "INSERT INTO USER_SDO_GEOM_METADATA (TABLE_NAME, COLUMN_NAME, DIMINFO, SRID)\n" +
                "VALUES ('GEO_TEST', 'GEO',\n" +
                "MDSYS.SDO_DIM_ARRAY\n" +
                "(MDSYS.SDO_DIM_ELEMENT('LONG', -180.0, 180.0, 0.05),\n" +
                "MDSYS.SDO_DIM_ELEMENT('LAT', -90.0, 90.0, 0.05)\n" +
                "),\n" +
                "8307);"));

    }

    @Test
    public void oracleEndsWithOpenMultilineStringLiteralAngularBrackets() {
        assertFalse(endsWithOpenMultilineStringLiteral(createOracleBuilder(), "ALTER TABLE VALUATION_PROPERTY_FORMATTING ADD (\n" +
                "CONSTRAINT VAL_PROPERTY_FORMAT_CURR_CK\n" +
                "CHECK ((NUMBER_DISPLAY_CONVERSION='FX_RATE' AND CURRENCY IS NOT NULL) OR (NUMBER_DISPLAY_CONVERSION<>'FX_RATE' AND CURRENCY IS NULL))\n" +
                "ENABLE VALIDATE);"));

    }

    @Test
    public void oracleEndsWithOpenMultilineStringLiteralNoSpace() {
        assertFalse(endsWithOpenMultilineStringLiteral(createOracleBuilder(), "CREATE OR REPLACE PROCEDURE BARBAZ\n" +
                "    (\n" +
                "        BAR  IN OUT VARCHAR2,\n" +
                "        BAZ  IN OUT VARCHAR2\n" +
                "    )\n" +
                "AS\n" +
                "\n" +
                "BEGIN\n" +
                "    IF BAR = 'BAR'THEN\n" +
                "        BAZ := 'BAZ';\n" +
                "    END IF;\n" +
                "END;\n" +
                "/"));
        assertFalse(endsWithOpenMultilineStringLiteral(createOracleBuilder(), "SELECT'HELLO'FROM DUAL;"));
        assertFalse(endsWithOpenMultilineStringLiteral(createOracleBuilder(), "SELECT'HELLO SELECT'FROM DUAL;"));
        assertFalse(endsWithOpenMultilineStringLiteral(createOracleBuilder(), "SELECT'FROM 'FROM DUAL;"));
        assertFalse(endsWithOpenMultilineStringLiteral(createOracleBuilder(), "SELECT' 'FROM DUAL;"));
    }

    @Test
    public void db2EndsWithOpenMultilineStringLiteral() {
        assertFalse(endsWithOpenMultilineStringLiteral(createDB2Builder(), "SELECT' 'FROM DUAL;"));
        assertFalse(endsWithOpenMultilineStringLiteral(createDB2Builder(), "SELECT '123' FROM DUAL;"));
        assertFalse(endsWithOpenMultilineStringLiteral(createDB2Builder(), "SELECT X'0123' FROM DUAL;"));
        assertFalse(endsWithOpenMultilineStringLiteral(createDB2Builder(), "SELECT X'0123',X'0456' FROM DUAL;"));
    }

    private DB2SqlStatementBuilder createDB2Builder() {
        return new DB2SqlStatementBuilder(new Delimiter(";", false));
    }

    @Test
    public void derbyEndsWithOpenMultilineStringLiteral() {
        assertFalse(endsWithOpenMultilineStringLiteral(createDerbyBuilder(), "SELECT' 'FROM DUAL;"));
        assertFalse(endsWithOpenMultilineStringLiteral(createDerbyBuilder(), "SELECT '123' FROM DUAL;"));
        assertFalse(endsWithOpenMultilineStringLiteral(createDerbyBuilder(), "SELECT X'0123' FROM DUAL;"));
        assertFalse(endsWithOpenMultilineStringLiteral(createDerbyBuilder(), "SELECT X'0123',X'0456' FROM DUAL;"));
    }

    private DerbySqlStatementBuilder createDerbyBuilder() {
        return new DerbySqlStatementBuilder(new Delimiter(";", false));
    }

    @Test
    public void stripDelimiter() {
        StringBuilder sql = new StringBuilder("SELECT * FROM t WHERE a = 'Straßenpaß';");
        SqlStatementBuilder.stripDelimiter(sql, new Delimiter(";", false));
        assertEquals("SELECT * FROM t WHERE a = 'Straßenpaß'", sql.toString());
    }

    @Test
    public void stripDelimiterGo() {
        StringBuilder sql = new StringBuilder("SELECT * FROM t WHERE a = 'Straßenpaß'\nGO");
        SqlStatementBuilder.stripDelimiter(sql, new Delimiter("GO", true));
        assertEquals("SELECT * FROM t WHERE a = 'Straßenpaß'\n", sql.toString());

        sql = new StringBuilder("SELECT * FROM t WHERE a = 'Straßenpaß'\ngo");
        SqlStatementBuilder.stripDelimiter(sql, new Delimiter("GO", true));
        assertEquals("SELECT * FROM t WHERE a = 'Straßenpaß'\n", sql.toString());

        sql = new StringBuilder("SELECT * FROM t WHERE a = 'Straßenpaß'\nGo");
        SqlStatementBuilder.stripDelimiter(sql, new Delimiter("GO", true));
        assertEquals("SELECT * FROM t WHERE a = 'Straßenpaß'\n", sql.toString());

        sql = new StringBuilder("SELECT * FROM t WHERE a = 'Straßenpaß'\ngO");
        SqlStatementBuilder.stripDelimiter(sql, new Delimiter("GO", true));
        assertEquals("SELECT * FROM t WHERE a = 'Straßenpaß'\n", sql.toString());
    }

    @Test
    public void stripDelimiterCustom() {
        StringBuilder sql = new StringBuilder("SELECT * FROM t WHERE a = 'Straßenpaß'$ßß$");
        SqlStatementBuilder.stripDelimiter(sql, new Delimiter("$ßß$", false));
        assertEquals("SELECT * FROM t WHERE a = 'Straßenpaß'", sql.toString());
    }


    @Test
    public void stripDelimiterWithAnotherSpecialCharacters() {
        StringBuilder sql = new StringBuilder("SELECT * FROM t WHERE a = 'BİRİNİ';");
        SqlStatementBuilder.stripDelimiter(sql, new Delimiter(";", false));
        assertEquals("SELECT * FROM t WHERE a = 'BİRİNİ'", sql.toString());
    }

    private boolean endsWithOpenMultilineStringLiteral(SqlStatementBuilder builder, String line) {
        builder.applyStateChanges(builder.simplifyLine(line));
        return builder.endWithOpenMultilineStringLiteral();
    }
}
