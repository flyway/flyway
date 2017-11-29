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
package org.flywaydb.core.internal.util.jdbc.pro;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class JdbcProxies {
    public static Connection createConnectionProxy(final ClassLoader classLoader, final Connection connection,
                                                   final DryRunStatementInterceptor dryRunStatementInterceptor) {
        return (Connection) Proxy.newProxyInstance(classLoader, new Class[]{Connection.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        try {
                            Object result = method.invoke(connection, args);
                            if ("createStatement".equals(method.getName())) {
                                return createStatementProxy(classLoader, (Statement) result, dryRunStatementInterceptor);
                            }
                            if ("prepareStatement".equals(method.getName())) {
                                return createPreparedStatementProxy(classLoader, (PreparedStatement) result, (String) args[0],
                                        dryRunStatementInterceptor);
                            }
                            if ("prepareCall".equals(method.getName())) {
                                return createCallableStatementProxy(classLoader, (CallableStatement) result, (String) args[0],
                                        dryRunStatementInterceptor);
                            }
                            return result;
                        } catch (InvocationTargetException e) {
                            throw e.getCause();
                        }
                    }
                });
    }

    private static Statement createStatementProxy(ClassLoader classLoader, final Statement statement,
                                                  final DryRunStatementInterceptor dryRunStatementInterceptor) {
        return (Statement) Proxy.newProxyInstance(classLoader, new Class[]{Statement.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        try {
                            if ("execute".equals(method.getName())
                                    || "executeUpdate".equals(method.getName())
                                    || "executeLargeUpdate".equals(method.getName())) {
                                dryRunStatementInterceptor.interceptStatement((String) args[0]);
                                if ("execute".equals(method.getName())) {
                                    return false;
                                }
                                return 0;
                            }

                            return method.invoke(statement, args);
                        } catch (InvocationTargetException e) {
                            throw e.getCause();
                        }
                    }
                });
    }

    private static PreparedStatement createPreparedStatementProxy(ClassLoader classLoader,
                                                                  final PreparedStatement statement,
                                                                  final String sql,
                                                                  final DryRunStatementInterceptor dryRunStatementInterceptor) {
        return (PreparedStatement) Proxy.newProxyInstance(classLoader, new Class[]{PreparedStatement.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        try {
                            if ("execute".equals(method.getName())
                                    || "executeUpdate".equals(method.getName())
                                    || "executeLargeUpdate".equals(method.getName())) {
                                if (args == null) {
                                    dryRunStatementInterceptor.interceptPreparedStatement(sql);
                                } else {
                                    dryRunStatementInterceptor.interceptStatement((String) args[0]);
                                }
                                if ("execute".equals(method.getName())) {
                                    return false;
                                }
                                return 0;
                            }

                            return method.invoke(statement, args);
                        } catch (InvocationTargetException e) {
                            throw e.getCause();
                        }
                    }
                });
    }

    private static CallableStatement createCallableStatementProxy(ClassLoader classLoader,
                                                                  final CallableStatement statement,
                                                                  final String sql,
                                                                  final DryRunStatementInterceptor dryRunStatementInterceptor) {
        return (CallableStatement) Proxy.newProxyInstance(classLoader, new Class[]{CallableStatement.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        try {
                            if ("execute".equals(method.getName())
                                    || "executeUpdate".equals(method.getName())
                                    || "executeLargeUpdate".equals(method.getName())) {
                                if (args == null) {
                                    dryRunStatementInterceptor.interceptCallableStatement(sql);
                                } else {
                                    dryRunStatementInterceptor.interceptStatement((String) args[0]);
                                }
                                if ("execute".equals(method.getName())) {
                                    return false;
                                }
                                return 0;
                            }

                            return method.invoke(statement, args);
                        } catch (InvocationTargetException e) {
                            throw e.getCause();
                        }
                    }
                });
    }
}
