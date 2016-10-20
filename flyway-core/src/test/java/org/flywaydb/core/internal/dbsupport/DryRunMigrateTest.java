/**
 * Copyright 2010-2016 Boxfuse GmbH
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
package org.flywaydb.core.internal.dbsupport;

import java.sql.Connection;
import java.sql.SQLException;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.dbsupport.derby.DerbyDbSupport;
import org.flywaydb.core.internal.dbsupport.h2.H2DbSupport;
import org.flywaydb.core.internal.dbsupport.hsql.HsqlDbSupport;
import org.flywaydb.core.internal.dbsupport.sqlite.SQLiteDbSupport;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Run migration and rollback at end in a single transaction
 */
@SuppressWarnings({"JavaDoc"})
public class DryRunMigrateTest 
{
    private final String SCHEMA_NAME= "PUBLIC";
    
    private void dryRun(boolean isSupported, SchemaFactory factory, String driverClass, String url, String user, String password, String... initSqls) throws SQLException {
        DriverDataSource ds = new DriverDataSource(Thread.currentThread().getContextClassLoader(), driverClass, url, user, password, initSqls);
        Connection conn = ds.getConnection();
        Schema schema = factory.getSchema(conn);
        assertTrue(schema.empty());
        conn.close();
        
        Flyway flyway = new Flyway();
        flyway.setDataSource(ds);
        flyway.setLocations("migration/sql");
        int statements = 0;
        FlywayException flywayException = null;
        try{
            statements = flyway.dryRun();
        } catch(FlywayException e){
            flywayException = e;
        }
        
        if(isSupported){
            assertNull("If the database is supported, a flyway exception should not be thrown.", flywayException);
            assertTrue("Dry-run should still report back the statements it would have run.", statements > 0);
        } else {
            assertNotNull(flywayException);
            assertTrue("Unsupported databases should throw a Flyway Exception", flywayException.getMessage().toLowerCase().contains("dry-run"));
        }
        
        conn = ds.getConnection();
        schema = factory.getSchema(conn);
        assertTrue(isSupported 
                ? "Expected dry-run feature to have rolled back the migration(s)" 
                : "Expected dry-run feature to fail validation and not perform any migration(s)", schema.empty());
        conn.close();
    }

    @Test
    public void dryRunTest_derbyIsSupported() throws Exception {
        dryRun(true, new SchemaFactory() {
            @Override
            public Schema getSchema(Connection conn) {
                return new DerbyDbSupport(conn).getSchema(SCHEMA_NAME);
            }
        }, null, "jdbc:derby:memory:flyway_db;create=true", "", "");
    }
    
    @Test
    public void dryRunTest_hsqlIsNotSupported() throws Exception {
        dryRun(false, new SchemaFactory() {
            @Override
            public Schema getSchema(Connection conn) {
                return new HsqlDbSupport(conn).getSchema(SCHEMA_NAME);
            }
        }, null, "jdbc:hsqldb:mem:flyway_db", "SA", "");
    }
    
    @Test
    public void dryRunTest_sqliteIsNotSupported() throws Exception {
        dryRun(false, new SchemaFactory() {
            @Override
            public Schema getSchema(Connection conn) {
                SQLiteDbSupport support = new SQLiteDbSupport(conn);
                return support.getSchema(support.getCurrentSchemaName());
            }
        }, null, "jdbc:sqlite::memory:", "", "");
    }
    
    @Test
    public void dryRunTest_h2NotSupported() throws Exception {
        dryRun(false, new SchemaFactory() {
            @Override
            public Schema getSchema(Connection conn) {
                return new H2DbSupport(conn).getSchema(SCHEMA_NAME);
            }
        }, null,"jdbc:h2:mem:flyway_db;DB_CLOSE_DELAY=-1", "sa", "");
    }
    
    private interface SchemaFactory{
        Schema getSchema(Connection conn);
    }
}
