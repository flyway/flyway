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

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Properties;

import org.neo4j.driver.v1.Session;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * @author Felipe Nascimento (ScuteraTech)
 *
 */
public class Neo4JConnectionEnhancer  {

	static Connection enhancedConnection(Connection connection, String url, Properties info) {
        Enhancer enhancer = new Enhancer();
        enhancer.setCallback(new MethodInterceptor() {
            @Override
            public Object intercept(Object arg0, Method arg1, Object[] arg2, MethodProxy arg3) throws Throwable {
                if (arg1.getName().equals("createStatement")) {
                    Statement statement = (Statement) arg1.invoke(connection, arg2);

                    Statement proxyStatement = (Statement) Proxy.newProxyInstance(
                            Neo4JStatementProxy.class.getClassLoader(), new Class[] { Statement.class },
                            new Neo4JStatementProxy(statement));

                    return proxyStatement;
                }
                return arg1.invoke(connection, arg2);
            }
        });

        enhancer.setSuperclass(connection.getClass());
        Class<?>[] argumentTypes = { Session.class, Properties.class, String.class };
        Object[] arguments = { null, info, url };
        Connection proxyConnection = (Connection) enhancer.create(argumentTypes, arguments);
        return proxyConnection;
	}

}
