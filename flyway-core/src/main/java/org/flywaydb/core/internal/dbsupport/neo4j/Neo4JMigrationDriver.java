package org.flywaydb.core.internal.dbsupport.neo4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.mockito.cglib.proxy.Proxy;
import org.neo4j.jdbc.Driver;

public class Neo4JMigrationDriver extends Driver {

	public Neo4JMigrationDriver() throws SQLException {
		super();
	}

	@Override
	public Connection connect(String url, Properties info) throws SQLException {
		Connection connection = super.connect(url, info);

		Connection proxyConnnection = (Connection) Proxy.newProxyInstance(Neo4JConnectionProxy.class.getClassLoader(),
				new Class[] { Connection.class }, new Neo4JConnectionProxy(connection));
		return proxyConnnection;
	}

}
