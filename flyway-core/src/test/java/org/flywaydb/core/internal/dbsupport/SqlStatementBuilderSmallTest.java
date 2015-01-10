/**
 * Copyright 2010-2014 Axel Fontaine
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
package org.flywaydb.core.internal.dbsupport;

import org.flywaydb.core.internal.dbsupport.h2.H2SqlStatementBuilder;
import org.flywaydb.core.internal.dbsupport.oracle.OracleSqlStatementBuilder;
import org.flywaydb.core.internal.dbsupport.postgresql.PostgreSQLSqlStatementBuilder;
import org.flywaydb.core.internal.dbsupport.sqlserver.SQLServerSqlStatementBuilder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test for SqlStatementBuilder.
 */
public class SqlStatementBuilderSmallTest {
    @Test
    public void postgreSqlEndsWithOpenMultilineStringLiteral() {
        assertTrue(new PostgreSQLSqlStatementBuilder().endsWithOpenMultilineStringLiteral("INSERT INTO address VALUES (1, '1. first"));
        assertFalse(new PostgreSQLSqlStatementBuilder().endsWithOpenMultilineStringLiteral("INSERT INTO address VALUES (1, '1. first\n" +
                "2. second');"));
    }

    @Test
    public void sqlServerEndsWithOpenMultilineStringLiteral() {
        assertFalse(new SQLServerSqlStatementBuilder().endsWithOpenMultilineStringLiteral("print 'baz'+"));
        assertFalse(new SQLServerSqlStatementBuilder().endsWithOpenMultilineStringLiteral("CUSTOMER set creaon_date = {ts '3099-01-01 00:00:00'} FROM CUSTOMER c inner join inserted i on c.id=i.id"));

        //Currently broken:
        //assertFalse(new SQLServerSqlStatementBuilder().endsWithOpenMultilineStringLiteral("print 'baz'-- Oops"));
    }

    @Test
    public void h2EndsWithOpenMultilineStringLiteral() {
        assertFalse(new H2SqlStatementBuilder().endsWithOpenMultilineStringLiteral("select * from t;"));
        assertFalse(new H2SqlStatementBuilder().endsWithOpenMultilineStringLiteral("select * from t where a='xyz';"));
        assertFalse(new H2SqlStatementBuilder().endsWithOpenMultilineStringLiteral("select * from t where a= 'xyz';"));
        assertFalse(new H2SqlStatementBuilder().endsWithOpenMultilineStringLiteral("select * from t where a='xyz'"));
        assertFalse(new H2SqlStatementBuilder().endsWithOpenMultilineStringLiteral("select * from t where a='$$';"));
        assertFalse(new H2SqlStatementBuilder().endsWithOpenMultilineStringLiteral("select * from t where a='xy''z';"));
        assertFalse(new H2SqlStatementBuilder().endsWithOpenMultilineStringLiteral("select * from t where a='xyz' and b like 'abc%';"));
        assertFalse(new H2SqlStatementBuilder().endsWithOpenMultilineStringLiteral("select * from t where a=' xyz ';"));
        assertFalse(new H2SqlStatementBuilder().endsWithOpenMultilineStringLiteral("select * from t where a=';';"));
        assertFalse(new H2SqlStatementBuilder().endsWithOpenMultilineStringLiteral("select * from t where a='$$''$$';"));
        assertFalse(new H2SqlStatementBuilder().endsWithOpenMultilineStringLiteral("select * from t where a='$$' || '$$';"));
        assertFalse(new H2SqlStatementBuilder().endsWithOpenMultilineStringLiteral("select * from t where a='$$'||'$$';"));
        assertFalse(new H2SqlStatementBuilder().endsWithOpenMultilineStringLiteral("INSERT INTO test_user (name) VALUES ('Mr. T');"));
        assertFalse(new H2SqlStatementBuilder().endsWithOpenMultilineStringLiteral("INSERT INTO test_user (name) VALUES ('Mr. Semicolon;');"));
        assertFalse(new H2SqlStatementBuilder().endsWithOpenMultilineStringLiteral("INSERT INTO test_user (id, name) VALUES (1, 'Mr. Semicolon;');"));
        assertFalse(new H2SqlStatementBuilder().endsWithOpenMultilineStringLiteral("insert into TAB1 (GUID, UID, VAL) values (1, '0100', 100);"));
        assertTrue(new H2SqlStatementBuilder().endsWithOpenMultilineStringLiteral("select * from t where a='$$||''$$;"));
        assertTrue(new H2SqlStatementBuilder().endsWithOpenMultilineStringLiteral("select * from t where a='"));
        assertTrue(new H2SqlStatementBuilder().endsWithOpenMultilineStringLiteral("select * from t where a='abc"));
        assertTrue(new H2SqlStatementBuilder().endsWithOpenMultilineStringLiteral("select * from t where a='abc''"));
        assertTrue(new H2SqlStatementBuilder().endsWithOpenMultilineStringLiteral("select * from t where a='abc'''||'"));
        assertTrue(new H2SqlStatementBuilder().endsWithOpenMultilineStringLiteral("INSERT INTO test_user (name) VALUES ('Mr. Semicolon+Linebreak;"));
    }

    @Test
    public void oracleEndsWithOpenMultilineStringLiteral() {
        assertFalse(new OracleSqlStatementBuilder().endsWithOpenMultilineStringLiteral("select q'[Hello 'quotes']' from dual;"));
        assertFalse(new OracleSqlStatementBuilder().endsWithOpenMultilineStringLiteral("select q'(Hello 'quotes')' from dual;"));
        assertFalse(new OracleSqlStatementBuilder().endsWithOpenMultilineStringLiteral("select q'{Hello 'quotes'}' from dual;"));
        assertFalse(new OracleSqlStatementBuilder().endsWithOpenMultilineStringLiteral("select q'<Hello 'quotes'>' from dual;"));
        assertFalse(new OracleSqlStatementBuilder().endsWithOpenMultilineStringLiteral("select q'$Hello 'quotes'$' from dual;"));

        assertTrue(new OracleSqlStatementBuilder().endsWithOpenMultilineStringLiteral("select q'[Hello 'quotes']"));
        assertTrue(new OracleSqlStatementBuilder().endsWithOpenMultilineStringLiteral("select q'(Hello 'quotes')"));
        assertTrue(new OracleSqlStatementBuilder().endsWithOpenMultilineStringLiteral("select q'{Hello 'quotes'}"));
        assertTrue(new OracleSqlStatementBuilder().endsWithOpenMultilineStringLiteral("select q'<Hello 'quotes'>"));
        assertTrue(new OracleSqlStatementBuilder().endsWithOpenMultilineStringLiteral("select q'$Hello 'quotes'$"));
    }

    @Test
    public void oracleEndsWithOpenMultilineStringLiteralComplex() {
        assertFalse(new OracleSqlStatementBuilder().endsWithOpenMultilineStringLiteral("INSERT INTO USER_SDO_GEOM_METADATA (TABLE_NAME, COLUMN_NAME, DIMINFO, SRID)\n" +
                "VALUES ('GEO_TEST', 'GEO',\n" +
                "MDSYS.SDO_DIM_ARRAY\n" +
                "(MDSYS.SDO_DIM_ELEMENT('LONG', -180.0, 180.0, 0.05),\n" +
                "MDSYS.SDO_DIM_ELEMENT('LAT', -90.0, 90.0, 0.05)\n" +
                "),\n" +
                "8307);"));

    }

    @Test
    public void oracleEndsWithOpenMultilineStringLiteralAngularBrackets() {
        assertFalse(new OracleSqlStatementBuilder().endsWithOpenMultilineStringLiteral("ALTER TABLE VALUATION_PROPERTY_FORMATTING ADD (\n" +
                "CONSTRAINT VAL_PROPERTY_FORMAT_CURR_CK\n" +
                "CHECK ((NUMBER_DISPLAY_CONVERSION='FX_RATE' AND CURRENCY IS NOT NULL) OR (NUMBER_DISPLAY_CONVERSION<>'FX_RATE' AND CURRENCY IS NULL))\n" +
                "ENABLE VALIDATE);"));

    }

    @Test
    public void oracleEndsWithOpenMultilineStringLiteralNoSpace() {
        assertFalse(new OracleSqlStatementBuilder().endsWithOpenMultilineStringLiteral("CREATE OR REPLACE PROCEDURE BARBAZ\n" +
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
        assertFalse(new OracleSqlStatementBuilder().endsWithOpenMultilineStringLiteral("SELECT'HELLO'FROM DUAL;"));
        assertFalse(new OracleSqlStatementBuilder().endsWithOpenMultilineStringLiteral("SELECT'HELLO SELECT'FROM DUAL;"));
        assertFalse(new OracleSqlStatementBuilder().endsWithOpenMultilineStringLiteral("SELECT'FROM 'FROM DUAL;"));
        assertFalse(new OracleSqlStatementBuilder().endsWithOpenMultilineStringLiteral("SELECT' 'FROM DUAL;"));
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
}
