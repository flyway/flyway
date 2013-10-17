/**
 * Copyright 2010-2013 Axel Fontaine and the many contributors.
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

import java.util.regex.Pattern;

public enum SupportedDb {
    DERBY (DerbyDbSupport.class, Pattern.compile("^Apache Derby.*")),
    H2  (H2DbSupport.class, Pattern.compile("^H2.*")),
    HSQL  (HsqlDbSupport.class, Pattern.compile(".*HSQL Database Engine.*")),
    SQLSERVER  (SQLServerDbSupport.class, Pattern.compile("^Microsoft SQL Server.*")),
    MYSQL (MySQLDbSupport.class, Pattern.compile(".*MySQL.*")),
    ORACLE  (OracleDbSupport.class, Pattern.compile("^Oracle.*")),
    POSTGRESQL  (PostgreSQLDbSupport.class, Pattern.compile("^PostgreSQL.*")),
    DB2  (DB2DbSupport.class, Pattern.compile("^DB2.*"));


    public Class<? extends DbSupport> getDbSupportClass() {
        return dbSupportClass;
    }

    public Pattern getProductNameRegexp() {
        return productNameRegexp;
    }

    private Class<? extends DbSupport> dbSupportClass;
    private Pattern productNameRegexp;

    private SupportedDb(Class<? extends DbSupport> dbSupportClass, Pattern productNameRegexp) {
        this.dbSupportClass = dbSupportClass;
        this.productNameRegexp = productNameRegexp;
    }

    public static SupportedDb forName(String databaseName) {
        for(SupportedDb d:values()) {
            if(d.name().equalsIgnoreCase(databaseName)) return d;
        }
        throw new FlywayException("Unsupported Database: " + databaseName);
    }

    public static SupportedDb forDatabaseProductName(String databaseProductName) {
        for(SupportedDb d:values()) {
            if(d.productNameRegexp.matcher(databaseProductName).matches()) return d;
        }
        throw new FlywayException("Unsupported Database: " + databaseProductName);
    }
}
