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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.neo4j.jdbc.bolt.BoltDriver;

/**
 * @author Felipe Nascimento (ScuteraTech)
 *
 */
public class Neo4JMigrationHttpDriver extends BoltDriver {

	public Neo4JMigrationHttpDriver() throws SQLException {
		super();
	}

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        Connection connection = super.connect(url, info);
        return Neo4JConnectionEnhancer.enhancedConnection(connection, url, info);
    }

}
