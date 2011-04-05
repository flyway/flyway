/**
 * Copyright (C) 2010-2011 the original author or authors.
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
package com.googlecode.flyway.core.dbsupport.oracle;

import com.googlecode.flyway.core.migration.sql.PlaceholderReplacer;
import com.googlecode.flyway.core.migration.sql.SqlStatement;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Test for OracleSqlScript.
 */
public class OracleSqlScriptSmallTest {
    @Test
    public void parseSqlStatements() throws Exception {
        String source = FileCopyUtils.copyToString(new InputStreamReader(new ClassPathResource("migration/dbsupport/oracle/sql/placeholders/V1.sql").getInputStream(), Charset.forName("UTF-8")));

        OracleSqlScript sqlScript = new OracleSqlScript(source, PlaceholderReplacer.NO_PLACEHOLDERS);
        List<SqlStatement> sqlStatements = sqlScript.getSqlStatements();
        assertEquals(3, sqlStatements.size());
        assertEquals(18, sqlStatements.get(0).getLineNumber());
        assertEquals(27, sqlStatements.get(1).getLineNumber());
        assertEquals(32, sqlStatements.get(2).getLineNumber());
        assertEquals("COMMIT", sqlStatements.get(2).getSql());
    }

    @Test
    public void parseFunctionsAndProcedures() throws Exception {
        String source = FileCopyUtils.copyToString(new InputStreamReader(new ClassPathResource("migration/dbsupport/oracle/sql/function/V1__Function.sql").getInputStream(), Charset.forName("UTF-8")));

        OracleSqlScript sqlScript = new OracleSqlScript(source, PlaceholderReplacer.NO_PLACEHOLDERS);
        List<SqlStatement> sqlStatements = sqlScript.getSqlStatements();
        assertEquals(3, sqlStatements.size());
        assertEquals(17, sqlStatements.get(0).getLineNumber());
        assertEquals(26, sqlStatements.get(1).getLineNumber());
        assertEquals(34, sqlStatements.get(2).getLineNumber());
        assertEquals("COMMIT", sqlStatements.get(2).getSql());
    }

    @Test
    public void changeDelimiterRegEx() {
        final OracleSqlScript script = new OracleSqlScript("", PlaceholderReplacer.NO_PLACEHOLDERS);
        Assert.assertNull(script.changeDelimiterIfNecessary(null, "begin_date", null));
        Assert.assertEquals("/", script.changeDelimiterIfNecessary(null, "begin date", null));
        Assert.assertNull(script.changeDelimiterIfNecessary(null, " begin date", null));
        Assert.assertEquals("/", script.changeDelimiterIfNecessary(null, "begin\tdate", null));
        Assert.assertEquals("/", script.changeDelimiterIfNecessary(null, "begin", null));
    }


}
