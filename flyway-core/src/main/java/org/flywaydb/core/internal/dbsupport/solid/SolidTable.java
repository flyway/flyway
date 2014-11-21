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
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> #885: Clean up source headers
/**
 * SolidDB support developed 2014 by Sabine Gallus & Michael Forstner
 * Media-Saturn IT Services GmbH
 * Wankelstr. 5
 * 85046 Ingolstadt, Germany
 * http://www.media-saturn.com
 */
<<<<<<< HEAD
=======
//
// Project: spring-boot-sample-flyway
//
// Media-Saturn IT Services GmbH
//
// Wankelstr. 5
// 85046 Ingolstadt
// Telefon: +49 (841) 634-0
// Telefax: +49 (841) 634-992596
// Web:     www.media-saturn.com
//
>>>>>>> Initial commit for SolidDB support (#885)
=======
>>>>>>> #885: Clean up source headers

package org.flywaydb.core.internal.dbsupport.solid;

import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;

import java.sql.SQLException;

public class SolidTable extends Table {

    public SolidTable(final JdbcTemplate jdbcTemplate, final DbSupport dbSupport, final Schema schema, final String name) {
        super(jdbcTemplate, dbSupport, schema, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        return exists(null, schema, name);
    }

    @Override
    protected void doLock() throws SQLException {
<<<<<<< HEAD
<<<<<<< HEAD
        jdbcTemplate.execute("LOCK TABLE " + this + " IN EXCLUSIVE MODE");
=======
        jdbcTemplate.execute("SELECT * FROM " + this + " FOR UPDATE");
>>>>>>> Initial commit for SolidDB support (#885)
=======
        jdbcTemplate.execute("LOCK TABLE " + this + " IN EXCLUSIVE MODE");
>>>>>>> #885: Minor fixes regarding SolidDB support including green ConcurrentMigrationTest
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP TABLE " + this);
    }
}
