/**
 * Copyright 2010-2014 Axel Fontaine and the many contributors.
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
package org.flywaydb.core.dbsupport;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.dbsupport.db2.DB2DbSupport;
import org.flywaydb.core.dbsupport.db2zos.DB2zosDbSupport;
import org.flywaydb.core.dbsupport.derby.DerbyDbSupport;
import org.flywaydb.core.dbsupport.h2.H2DbSupport;
import org.flywaydb.core.dbsupport.hsql.HsqlDbSupport;
import org.flywaydb.core.dbsupport.mysql.MySQLDbSupport;
import org.flywaydb.core.dbsupport.oracle.OracleDbSupport;
import org.flywaydb.core.dbsupport.postgresql.PostgreSQLDbSupport;
import org.flywaydb.core.dbsupport.sqlserver.SQLServerDbSupport;
import org.flywaydb.core.util.logging.Log;
import org.flywaydb.core.util.logging.LogFactory;

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
     * @param printInfo  Where the DB info should be printed in the logs.
     * @return The appropriate DbSupport class.
     */
    public static DbSupport createDbSupport(Connection connection, boolean printInfo) {
        String databaseProductName = getDatabaseProductName(connection);

        if (printInfo) {
            LOG.info("Database: " + getJdbcUrl(connection) + " (" + databaseProductName + ")");
        }

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
        if (databaseProductName.startsWith("PostgreSQL")) {
            return new PostgreSQLDbSupport(connection);
        }
        if (databaseProductName.startsWith("DB2")) {
			if (getDatabaseProductVersion(connection).startsWith("DSN")){
				return new DB2zosDbSupport(connection);
			} else {
				return new DB2DbSupport(connection);
			}
        }

        throw new FlywayException("Unsupported Database: " + databaseProductName);
    }

    /**
     * Retrieves the Jdbc Url for this connection.
     *
     * @param connection The Jdbc connection.
     * @return The Jdbc Url.
     */
    private static String getJdbcUrl(Connection connection) {
        try {
            return connection.getMetaData().getURL();
        } catch (SQLException e) {
            throw new FlywayException("Unable to retrieve the Jdbc connection Url!", e);
        }
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

	/**
	 * Retrieves the database version.
	 *
	 * @param connection The connection to use to query the database.
	 * @return The version of the database product.
	 * Ex.: DSN11015 DB2 for z/OS Version 11
	 *      SQL10050 DB" for Linux, UNIX and Windows Version 10.5
	 */
	private static String getDatabaseProductVersion(Connection connection) {
		try {
			DatabaseMetaData databaseMetaData = connection.getMetaData();
			if (databaseMetaData == null) {
				throw new FlywayException("Unable to read database metadata while it is null!");
			}

			String databaseProductVersion = databaseMetaData.getDatabaseProductVersion();
			if (databaseProductVersion == null) {
				throw new FlywayException("Unable to determine database. Product version is null.");
			}


			return databaseProductVersion;
		} catch (SQLException e) {
			throw new FlywayException("Error while determining database product version", e);
		}
	}


}
