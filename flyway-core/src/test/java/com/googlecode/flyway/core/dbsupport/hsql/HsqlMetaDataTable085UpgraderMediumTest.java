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

package com.googlecode.flyway.core.dbsupport.hsql;

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.runtime.MetaDataTable085UpgraderTestCase;
import org.springframework.test.context.ContextConfiguration;

/**
 * Testcase for the upgrade of the metadata table using Hsql.
 */
@ContextConfiguration(locations = {"classpath:upgrade/hsql/hsql-context.xml"})
public class HsqlMetaDataTable085UpgraderMediumTest extends MetaDataTable085UpgraderTestCase {
    @Override
    protected String getBaseDir() {
        return "upgrade/sql";
    }

    @Override
    protected DbSupport getDbSupport() {
        return new HsqlDbSupport();
    }

    @Override
    protected String getMetaDataTable085CreateScriptLocation() {
        return "upgrade/hsql/createMetaDataTable085.sql";
    }
}
