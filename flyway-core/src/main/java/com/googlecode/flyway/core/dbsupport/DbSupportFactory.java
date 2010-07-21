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

package com.googlecode.flyway.core.dbsupport;

import com.googlecode.flyway.core.dbsupport.h2.H2DbSupport;
import com.googlecode.flyway.core.dbsupport.hsql.HsqlDbSupport;
import com.googlecode.flyway.core.dbsupport.mysql.MySQLDbSupport;
import com.googlecode.flyway.core.dbsupport.oracle.OracleDbSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Factory for obtaining the correct DbSupport instance for the current connection.
 */
public class DbSupportFactory {
    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(DbSupportFactory.class);

    /**
     * Prevent instantiation.
     */
    private DbSupportFactory() {
        //Do nothing
    }

    /**
     * Initializes the appropriate DbSupport class for the database product used
     * by the data source.
     *
     * @param jdbcTemplate The Jdbc Template to use to query the database.
     * @return The appropriate DbSupport class.
     */
    public static DbSupport createDbSupport(JdbcTemplate jdbcTemplate) {
        String databaseProductName = getDatabaseProductName(jdbcTemplate);

        LOG.debug("Database: " + databaseProductName);

        Collection<DbSupport> dbSupports = new ArrayList<DbSupport>();

        dbSupports.add(new HsqlDbSupport());
        dbSupports.add(new H2DbSupport());
        dbSupports.add(new MySQLDbSupport());
        dbSupports.add(new OracleDbSupport());

        for (DbSupport dbSupport : dbSupports) {
            if (dbSupport.supportsDatabase(databaseProductName)) {
                if (!dbSupport.supportsLocking()) {
                    LOG.info(databaseProductName + " does not support locking. No concurrent migration supported.");
                }

                return dbSupport;
            }
        }

        throw new IllegalArgumentException("Unsupported Database: " + databaseProductName);
    }

    /**
     * Retrieves the name of the database product.
     *
     * @param jdbcTemplate The Jdbc Template to use to query the database.
     * @return The name of the database product. Ex.: Oracle, MySQL, ...
     */
    private static String getDatabaseProductName(JdbcTemplate jdbcTemplate) {
        return (String) jdbcTemplate.execute(new ConnectionCallback() {
            @Override
            public String doInConnection(Connection connection) throws SQLException, DataAccessException {
                DatabaseMetaData databaseMetaData = connection.getMetaData();
                if (databaseMetaData == null) {
                    throw new IllegalStateException("Unable to read database metadata while it is null!");
                }
                return connection.getMetaData().getDatabaseProductName();
            }
        });
    }

}
