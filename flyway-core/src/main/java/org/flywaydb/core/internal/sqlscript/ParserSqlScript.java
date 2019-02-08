/*
 * Copyright 2010-2019 Boxfuse GmbH
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
package org.flywaydb.core.internal.sqlscript;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.resource.LoadableResource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ParserSqlScript implements SqlScript {
    private static final Log LOG = LogFactory.getLog(ParserSqlScript.class);

    /**
     * The sql statements contained in this script.
     */
    protected final List<SqlStatement> sqlStatements = new ArrayList<>();

    private int sqlStatementCount;

    /**
     * Whether this SQL script contains at least one non-transactional statement.
     */
    private boolean nonTransactionalStatementFound;

    /**
     * The resource containing the statements.
     */
    protected final LoadableResource resource;







    /**
     * Creates a new sql script from this source.
     *
     * @param resource The sql script resource.
     * @param mixed    Whether to allow mixing transactional and non-transactional statements within the same migration.
     */
    public ParserSqlScript(Parser parser, LoadableResource resource, boolean mixed) {
        this.resource = resource;





        try (SqlStatementIterator sqlStatementIterator = parser.parse(resource)) {
            boolean transactionalStatementFound = false;
            while (sqlStatementIterator.hasNext()) {
                SqlStatement sqlStatement = sqlStatementIterator.next();



                    this.sqlStatements.add(sqlStatement);




                sqlStatementCount++;

                if (sqlStatement.canExecuteInTransaction()) {
                    transactionalStatementFound = true;
                } else {
                    nonTransactionalStatementFound = true;
                }

                if (!mixed && transactionalStatementFound && nonTransactionalStatementFound) {
                    throw new FlywayException(
                            "Detected both transactional and non-transactional statements within the same migration"
                                    + " (even though mixed is false). Offending statement found at line "
                                    + sqlStatement.getLineNumber() + ": " + sqlStatement.getSql()
                                    + (sqlStatement.canExecuteInTransaction() ? "" : " [non-transactional]"));
                }









                if (LOG.isDebugEnabled()) {
                    LOG.debug("Found statement at line " + sqlStatement.getLineNumber() + ": " + sqlStatement.getSql()
                            + (sqlStatement.canExecuteInTransaction() ? "" : " [non-transactional]"));
                }
            }
        }
    }

    @Override
    public SqlStatementIterator getSqlStatements() {






        final Iterator<SqlStatement> iterator = sqlStatements.iterator();
        return new SqlStatementIterator() {
            @Override
            public void close() {
            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public SqlStatement next() {
                return iterator.next();
            }

            @Override
            public void remove() {
                iterator.remove();
            }
        };
    }

    @Override
    public int getSqlStatementCount() {
        return sqlStatementCount;
    }








    @Override
    public final LoadableResource getResource() {
        return resource;
    }

    @Override
    public boolean executeInTransaction() {
        return !nonTransactionalStatementFound;
    }

    @Override
    public int compareTo(SqlScript o) {
        return resource.getRelativePath().compareTo(o.getResource().getRelativePath());
    }
}