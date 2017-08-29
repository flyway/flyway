/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.internal.dbsupport.neo4j;

import static org.junit.Assert.*;

import java.sql.SQLException;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.dbsupport.neo4j.Neo4JMetaDataTable;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.internal.util.scanner.classpath.ClassPathResource;
import org.junit.Test;

public class Neo4JDriverDataSourceSmallTest {
	
	@Test  
	public void getConnectionException() throws Exception {
	        String url = "jdbc:neo4j:bolt:<<<Invalid--URL>>";
	        String user = "neo4j";
	        String password = "test";

	        try { 
	            new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, url, user, password, null).getConnection();
	        } catch (FlywayException e) {
	            assertTrue(e.getCause() instanceof SQLException);
	            assertTrue(e.getMessage().contains(url));
	            assertTrue(e.getMessage().contains(user));
	            assertFalse(e.getMessage().contains(password));
	        }
	    }

	    @Test
	    public void nullInitSqls() throws Exception {
	        new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, "jdbc:neo4j:bolt://localhost:7687/", "neo4j", "test", null).getConnection().close();
	    }
	    
	    @Test
	    public void testTableComposition(){
	    	  String resourceName = "org/flywaydb/core/internal/dbsupport/" + "neo4j" + "/createMetaDataTable.sql";
              String source = new ClassPathResource(resourceName, Neo4JMetaDataTable.class.getClassLoader()).loadAsString("UTF-8");
              
              String expected = "MERGE (schemaVersion :schema_version);\n" + 
              		"\n" + 
              		"MATCH (sv :schema_version) \n" + 
              		"CREATE (sv)-[:schema_versionToMigration]->\n" + 
              		"(Migration? :Migration \n" + 
              		"	{installed_rank: 'installed_rank' , \n" + 
              		"	version : 'version',\n" + 
              		"	description:'description',\n" + 
              		"	type:'type',\n" + 
              		"	script:'script',\n" + 
              		"	checksum:'checksum',\n" + 
              		"	installed_by:'installed_by',\n" + 
              		"	installed_on:'installed_on',\n" + 
              		"	execution_time:'execution_time',\n" + 
              		"	success:'success'});";
              
              assertEquals(expected, source);

	    }	
}
