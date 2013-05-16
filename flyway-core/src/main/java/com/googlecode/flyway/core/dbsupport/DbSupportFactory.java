/**
 * Copyright (C) 2010-2013 the original author or authors.
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

import com.googlecode.flyway.core.api.FlywayException;
import com.googlecode.flyway.core.dbsupport.db2.DB2DbSupport;
import com.googlecode.flyway.core.dbsupport.derby.DerbyDbSupport;
import com.googlecode.flyway.core.dbsupport.h2.H2DbSupport;
import com.googlecode.flyway.core.dbsupport.hsql.HsqlDbSupport;
import com.googlecode.flyway.core.dbsupport.mysql.MySQLDbSupport;
import com.googlecode.flyway.core.dbsupport.oracle.OracleDbSupport;
import com.googlecode.flyway.core.dbsupport.postgresql.PostgreSQLDbSupport;
import com.googlecode.flyway.core.dbsupport.sqlserver.SQLServerDbSupport;
import com.googlecode.flyway.core.util.logging.Log;
import com.googlecode.flyway.core.util.logging.LogFactory;

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
     * @param connection The Jdbc connection to use to query the database.
     * @return The appropriate DbSupport class.
     */
    public static DbSupport createDbSupport(Connection connection) {
        String databaseProductName = getDatabaseProductName(connection);

        LOG.debug("Database: " + databaseProductName);

        if (databaseProductName.startsWith("Apache Derby")) {
            return new DerbyDbSupport(connection);
        }
        if (databaseProductName.startsWith("H2")) {
            return new H2DbSupport(connection);
        }
        if (databaseProductName.contains("HSQL Database Engine")) {
            // For regular Hsql and the Google Cloud SQL local default DB.
            return new HsqlDbSupport(connection);
        }
        if (databaseProductName.startsWith("Microsoft SQL Server")) {
            return new SQLServerDbSupport(connection);
        }
        if (databaseProductName.contains("MySQL")) {
            // For regular MySQL and Google Cloud SQL.
            // Google Cloud SQL returns different names depending on the environment and the SDK version.
            //   ex.: Google SQL Service/MySQL
            return new MySQLDbSupport(connection);
        }
        if (databaseProductName.startsWith("Oracle")) {
            return new OracleDbSupport(connection);
        }
        if (databaseProductName.startsWith("TimesTen")) {
        	return new TimesTenDbSupport(connection);
        }
        if (databaseProductName.startsWith("PostgreSQL")) {
            return new PostgreSQLDbSupport(connection);
        }
        if (databaseProductName.startsWith("DB2")) {
            // DB2 also returns the OS it's running on.
            //   ex.: DB2/NT
            return new DB2DbSupport(connection);
        }

        throw new FlywayException("Unsupported Database: " + databaseProductName);
    }

    /**
     * Retrieves the name of the database product.
     *
     * @param connection The connection to use to query the database.
     * @return The name of the database product. Ex.: Oracle, MySQL, ...
     */
    private static String getDatabaseProductName(Connection connection) {
        try {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            if (databaseMetaData == null) {
                throw new FlywayException("Unable to read database metadata while it is null!");
            }

            String databaseProductName = databaseMetaData.getDatabaseProductName();
            if (databaseProductName == null) {
                throw new FlywayException("Unable to determine database. Product name is null.");
            }

            int databaseMajorVersion = databaseMetaData.getDatabaseMajorVersion();
            int databaseMinorVersion = databaseMetaData.getDatabaseMinorVersion();

            return databaseProductName + " " + databaseMajorVersion + "." + databaseMinorVersion;
        } catch (SQLException e) {
            throw new FlywayException("Error while determining database product name", e);
        }
    }

}
