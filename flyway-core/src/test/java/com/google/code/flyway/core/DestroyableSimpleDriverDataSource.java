package com.google.code.flyway.core;

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
        for (Connection connection:connections) {
            JdbcUtils.closeConnection(connection);
        }
    }
}
