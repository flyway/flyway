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
package com.googlecode.flyway.core.dbsupport;

import com.googlecode.flyway.core.dbsupport.h2.H2DbSupport;
import com.googlecode.flyway.core.dbsupport.hsql.HsqlDbSupport;
import com.googlecode.flyway.core.dbsupport.sqlserver.SQLServerDbSupport;
import com.googlecode.flyway.core.dbsupport.mysql.MySQLDbSupport;
import com.googlecode.flyway.core.dbsupport.oracle.OracleDbSupport;
import com.googlecode.flyway.core.dbsupport.postgresql.PostgreSQLDbSupport;
import com.googlecode.flyway.core.exception.FlywayException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

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
     * Initializes the appropriate DbSupport class for the database product used by the data source.
     *
     * @param jdbcTemplate The Jdbc Template to use to query the database.
     *
     * @return The appropriate DbSupport class.
     */
    public static DbSupport createDbSupport(JdbcTemplate jdbcTemplate) {
        String databaseProductName = getDatabaseProductName(jdbcTemplate);

        LOG.debug("Database: " + databaseProductName);

        DbSupport dbSupport = null;
        if ("H2".equals(databaseProductName)) {
            dbSupport = new H2DbSupport(jdbcTemplate);
        }
        if ("HSQL Database Engine".equals(databaseProductName)) {
            dbSupport = new HsqlDbSupport(jdbcTemplate);
        }
        if ("Microsoft SQL Server".equals(databaseProductName)) {
            dbSupport = new SQLServerDbSupport(jdbcTemplate);
        }
        if ("MySQL".equals(databaseProductName)) {
            dbSupport = new MySQLDbSupport(jdbcTemplate);
        }
        if ("Oracle".equals(databaseProductName)) {
            dbSupport = new OracleDbSupport(jdbcTemplate);
        }
        if ("PostgreSQL".equals(databaseProductName)) {
            dbSupport = new PostgreSQLDbSupport(jdbcTemplate);
        }

        if (dbSupport == null) {
            throw new FlywayException("Unsupported Database: " + databaseProductName);
        }

        if (!dbSupport.supportsLocking()) {
            LOG.info(databaseProductName + " does not support locking. No concurrent migration supported.");
        }

        return dbSupport;
    }

    /**
     * Retrieves the name of the database product.
     *
     * @param jdbcTemplate The Jdbc Template to use to query the database.
     *
     * @return The name of the database product. Ex.: Oracle, MySQL, ...
     */
    private static String getDatabaseProductName(JdbcTemplate jdbcTemplate) {
        return (String) jdbcTemplate.execute(new ConnectionCallback() {
            @Override
            public String doInConnection(Connection connection) throws SQLException, DataAccessException {
                DatabaseMetaData databaseMetaData = connection.getMetaData();
                if (databaseMetaData == null) {
                    throw new FlywayException("Unable to read database metadata while it is null!");
                }
                return databaseMetaData.getDatabaseProductName();
            }
        });
    }

}
