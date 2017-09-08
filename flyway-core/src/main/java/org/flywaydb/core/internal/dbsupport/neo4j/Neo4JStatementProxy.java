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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Statement;


/**
 * @author Ricardo Silva (ScuteraTech)
 *
 */
public class Neo4JStatementProxy implements InvocationHandler {

	private Statement proxiedStatement;
	
	public Neo4JStatementProxy(Statement statement) {
		this.proxiedStatement = statement;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (method.getName().equals("setEscapeProcessing")) 
			return null;
		if (method.getName().equals("getMoreResults")) 
			return false;
		try {
			return method.invoke(proxiedStatement, args);
		}catch(Exception e) {
			throw e.getCause();
		}
	}

}
