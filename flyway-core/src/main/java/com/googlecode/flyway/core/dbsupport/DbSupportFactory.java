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
package com.googlecode.flyway.core.dbsupport;

import com.googlecode.flyway.core.api.FlywayException;
import com.googlecode.flyway.core.util.logging.Log;
import com.googlecode.flyway.core.util.logging.LogFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory for obtaining the correct DbSupport instance for the current connection.
 */
public class DbSupportFactory {
    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(DbSupportFactory.class);

    private static Map<Class<? extends DbSupport>, Class<? extends DbSupport>> customDbSupport =
            new HashMap<Class<? extends DbSupport>, Class<? extends DbSupport>>();

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
        String url = getJdbcUrl(connection);

        LOG.debug("Jdbc Url: " + url);
        LOG.debug("Database: " + databaseProductName);

        Class<? extends DbSupport> dbSupportClass = SupportedDb.forDatabaseProductName(databaseProductName).getDbSupportClass();
        if(customDbSupport.containsKey(dbSupportClass)) dbSupportClass = customDbSupport.get(dbSupportClass);

        try {
            return dbSupportClass.getConstructor(Connection.class).newInstance(connection);
        } catch (Exception e) {
            throw new FlywayException("Unable to create instance of " + dbSupportClass.getName(), e);
        }
    }

    public static void clearCustomDbSupport() {
        customDbSupport.clear();
    }

    public static Class<? extends DbSupport> getCustomDbSupport(Class<? extends DbSupport> dbSupportClass) {
        return customDbSupport.get(dbSupportClass);
    }

    public static void registerCustomDbSupport(String databaseName, String dbSupportClassName) {
        DbSupportFactory.customDbSupport.put(SupportedDb.forName(databaseName).getDbSupportClass(), findClass(dbSupportClassName));
    }

    private static Class<? extends DbSupport> findClass(String className) {
        try {
            return Class.forName(className).asSubclass(DbSupport.class);
        } catch(ClassNotFoundException e) {
            throw new FlywayException("Could not find class: " + className, e);
        }
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

}
