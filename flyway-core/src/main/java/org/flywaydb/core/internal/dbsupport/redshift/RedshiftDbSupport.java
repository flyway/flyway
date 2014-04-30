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
package org.flywaydb.core.internal.dbsupport.redshift;

import java.sql.Connection;
import java.sql.SQLException;

import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.postgresql.PostgreSQLDbSupport;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

/**
 * Redshift-specific support.
 */
public class RedshiftDbSupport extends PostgreSQLDbSupport {
    private static final Log LOG = LogFactory.getLog(RedshiftDbSupport.class);

    /**
     * Creates a new instance.
     *
     * @param connection The connection to use.
     */
    public RedshiftDbSupport(Connection connection) {
        super(connection);
    }

    public String getDbName() {
        return "redshift";
    }

    @Override
    protected void doSetCurrentSchema(Schema schema) throws SQLException {
        if (schema == null) {
            jdbcTemplate.execute("SELECT set_config('search_path', '', false)");
            return;
        }

        String searchPath = jdbcTemplate.queryForString("SHOW search_path");
        if (StringUtils.hasText(searchPath) && !searchPath.equals("unset")) {
             // Redshift throws an error on the $ character of $user when setting search_path. It needs to be quoted.
             if (searchPath.contains("$user") && !searchPath.contains(doQuote("$user"))) {
                 searchPath = searchPath.replace("$user", doQuote("$user"));
             }
            jdbcTemplate.execute("SET search_path = " + schema + "," + searchPath);
        } else {
            jdbcTemplate.execute("SET search_path = " + schema);
        }
    }

    @Override
    public Schema getSchema(String name) {
        return new RedshiftSchema(jdbcTemplate, this, name);
    }

    /**
     * @return true if we are connected to Redshift; false otherwise
     */
    public boolean detect() {
        try {
            return jdbcTemplate.queryForInt("select count(*) from information_schema.tables where table_schema = 'pg_catalog' and table_name = 'stl_s3client'") > 0;
        } catch (SQLException e) {
            LOG.error("Unable to check whether this is a Redshift database", e);
            return false;
        }
    }

}
