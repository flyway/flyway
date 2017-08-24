package org.flywaydb.core.internal.dbsupport.neo4j;

import java.lang.reflect.Method;
import java.sql.Statement;

import org.mockito.cglib.proxy.InvocationHandler;


public class Neo4JStatementProxy implements InvocationHandler {

	Statement proxiedStatement;
	
	public Neo4JStatementProxy(Statement statement) {
		this.proxiedStatement = statement;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (method.getName().equals("setEscapeProcessing")) 
			return null;
		if (method.getName().equals("getMoreResults")) 
			return false;
		return method.invoke(proxiedStatement, args);
	}

}
