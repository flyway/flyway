/*
 * Copyright 2017 ScuteraTech Unip.LDA
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
