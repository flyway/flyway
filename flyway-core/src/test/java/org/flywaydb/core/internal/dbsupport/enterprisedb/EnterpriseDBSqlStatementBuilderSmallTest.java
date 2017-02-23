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
package org.flywaydb.core.internal.dbsupport.enterprisedb;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test for EnterpriseDBSqlStatementBuilder.
 */
public class EnterpriseDBSqlStatementBuilderSmallTest {
    private EnterpriseDBSqlStatementBuilder builder = new EnterpriseDBSqlStatementBuilder();

    @Test
    public void setDefineOff() {
        builder.addLine("set define off;");
        assertTrue(builder.canDiscard());
    }

    @Test
    public void changeDelimiterRegEx() {
        assertNull(builder.changeDelimiterIfNecessary("BEGIN_DATE", null));
        assertEquals("/", builder.changeDelimiterIfNecessary("BEGIN DATE", null).getDelimiter());
        assertEquals("/", builder.changeDelimiterIfNecessary("BEGIN", null).getDelimiter());
    }

    @Test
    public void nvarchar() {
        builder.addLine("INSERT INTO nvarchar2_test VALUES ( N'qwerty' );");
        assertTrue(builder.isTerminated());
    }

    @Test
    public void notNvarchar() {
        builder.addLine("INSERT INTO nvarchar2_test VALUES ( ' N' );");
        assertTrue(builder.isTerminated());
    }

    @Test
    public void qQuote() {
        builder.addLine("select q'[Hello 'no quotes]' from dual;");
        assertTrue(builder.isTerminated());
    }

    @Test
    public void quotedStringEndingWithN() {
        builder.addLine("insert into table (COLUMN) values 'VALUE_WITH_N';");
        assertTrue(builder.isTerminated());
    }

    @Test
    public void quotedWithFrom() {
        builder.addLine("insert into table (COLUMN) values 'FROM';");
        assertTrue(builder.isTerminated());
    }

    @Test
    public void quotedWithFromComplex() {
        builder.addLine("DELETE FROM TEST.TABLE1 where CFG_AREA_ID_1 like '%NAME%' AND SOME_ID='NITS'AND CFG_AREA_CD IN ('COND_TXT','FORM');");
        assertTrue(builder.isTerminated());
    }
}
