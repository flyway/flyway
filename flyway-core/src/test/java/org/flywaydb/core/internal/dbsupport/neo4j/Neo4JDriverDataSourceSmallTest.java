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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.junit.ClassRule;
import org.junit.Test;
/**
 * @author Ricardo Silva (ScuteraTech)
 *
 */
public class Neo4JDriverDataSourceSmallTest {
	private static final String DOCKER_IMAGE_NAME = "neo4j:latest";
	
	@ClassRule
	public static Neo4JDockerContainer neo4jDockerContainer = new Neo4JDockerContainer(DOCKER_IMAGE_NAME);
	
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
	        new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, neo4jDockerContainer.getJdbcUrl(), neo4jDockerContainer.getUsername(), neo4jDockerContainer.getPassword(), null).getConnection().close();
	    }
}
