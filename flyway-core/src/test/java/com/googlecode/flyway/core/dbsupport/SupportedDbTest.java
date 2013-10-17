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
import org.junit.Test;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class SupportedDbTest {
    @Test
    public void forName(){
        assertEquals(SupportedDb.DERBY, SupportedDb.forName("DERBY"));
        assertEquals(SupportedDb.H2, SupportedDb.forName("H2"));
        assertEquals(SupportedDb.HSQL, SupportedDb.forName("HSQL"));
        assertEquals(SupportedDb.SQLSERVER, SupportedDb.forName("SQLSERVER"));
        assertEquals(SupportedDb.MYSQL, SupportedDb.forName("MYSQL"));
        assertEquals(SupportedDb.ORACLE, SupportedDb.forName("ORACLE"));
        assertEquals(SupportedDb.POSTGRESQL, SupportedDb.forName("POSTGRESQL"));
        assertEquals(SupportedDb.DB2, SupportedDb.forName("DB2"));
    }

    @Test
    public void forNameIgnoresCase(){
        assertEquals(SupportedDb.forName("DerbY"), SupportedDb.forName("DERBY"));
    }

    @Test
    public void forDatabaseProductName() throws SQLException {
        assertEquals(SupportedDb.DERBY, SupportedDb.forDatabaseProductName("Apache Derby..."));
        assertEquals(SupportedDb.H2, SupportedDb.forDatabaseProductName("H2..."));
        assertEquals(SupportedDb.HSQL, SupportedDb.forDatabaseProductName("...HSQL Database Engine..."));
        assertEquals(SupportedDb.SQLSERVER, SupportedDb.forDatabaseProductName("Microsoft SQL Server..."));
        assertEquals(SupportedDb.MYSQL, SupportedDb.forDatabaseProductName("...MySQL..."));
        assertEquals(SupportedDb.ORACLE, SupportedDb.forDatabaseProductName("Oracle..."));
        assertEquals(SupportedDb.POSTGRESQL, SupportedDb.forDatabaseProductName("PostgreSQL"));
        assertEquals(SupportedDb.DB2, SupportedDb.forDatabaseProductName("DB2"));
    }

    @Test
     public void forNameWithUnsupportedDatabaseProductName(){
        try {
            assertNull(SupportedDb.forDatabaseProductName("Unsupported"));
            fail("Should fail for unsupported database.");
        }catch(FlywayException e){
            assertEquals("Unsupported Database: Unsupported", e.getMessage());
        }
    }

    @Test
    public void forNameWithUnsupportedDatabaseName(){
        try {
            assertNull(SupportedDb.forName("Unsupported"));
            fail("Should fail for unsupported database.");
        }catch(FlywayException e){
            assertEquals("Unsupported Database: Unsupported", e.getMessage());
        }
    }

}


