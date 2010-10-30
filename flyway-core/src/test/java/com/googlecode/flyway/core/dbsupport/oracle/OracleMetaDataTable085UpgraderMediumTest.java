/**
 * Copyright (C) 2009-2010 the original author or authors.
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

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.runtime.MetaDataTable085UpgraderTestCase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

/**
 * Testcase for the upgrade of the metadata table using Oracle.
 */
@ContextConfiguration(locations = {"classpath:upgrade/oracle/oracle-context.xml"})
public class OracleMetaDataTable085UpgraderMediumTest extends MetaDataTable085UpgraderTestCase {
    @Override
    protected String getBaseDir() {
        return "upgrade/sql";
    }

    @Override
    protected DbSupport getDbSupport(JdbcTemplate jdbcTemplate) {
        return new OracleDbSupport(jdbcTemplate);
    }

    @Override
    protected String getMetaDataTable085CreateScriptLocation() {
        return "upgrade/oracle/createMetaDataTable085.sql";
    }
}
