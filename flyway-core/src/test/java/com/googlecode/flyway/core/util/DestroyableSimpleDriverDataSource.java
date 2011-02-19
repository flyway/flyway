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
package com.googlecode.flyway.core.util;

import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.support.JdbcUtils;

import javax.annotation.PreDestroy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

/**
 * SimpleDriverDataSource that destroys all remaining connections when the Spring Context shuts down.
 */
public class DestroyableSimpleDriverDataSource extends SimpleDriverDataSource {
    /**
     * Collection that keeps a reference to all connections that have been retrieved from the database.
     */
    private final Collection<Connection> connections = new ArrayList<Connection>();

    @Override
    protected Connection getConnectionFromDriver(Properties props) throws SQLException {
        Connection connection = super.getConnectionFromDriver(props);
        connections.add(connection);
        return connection;
    }

    /**
     * Destroys this datasource and all the connections it acquired.
     */
    @PreDestroy
    public void destroy() {
        for (Connection connection : connections) {
            JdbcUtils.closeConnection(connection);
        }
    }
}
