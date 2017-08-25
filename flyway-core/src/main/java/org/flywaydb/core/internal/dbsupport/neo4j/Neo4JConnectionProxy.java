package org.flywaydb.core.internal.dbsupport.neo4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Statement;



public class Neo4JConnectionProxy implements InvocationHandler {

	private Connection proxiedConnection;

	public Neo4JConnectionProxy(Connection connection) {
		this.proxiedConnection = connection;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (method.getName().equals("createStatement")) {
			Statement statement = (Statement) method.invoke(proxiedConnection, args);
			
			Statement proxyStatement = (Statement) Proxy.newProxyInstance(Neo4JStatementProxy.class.getClassLoader(),
					new Class[] { Statement.class }, new Neo4JStatementProxy(statement));
						
			return proxyStatement;
		}
		return method.invoke(proxiedConnection, args);
	}

}
