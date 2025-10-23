/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.core.internal.sqlscript;

import lombok.CustomLog;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.parser.Parser;

import java.util.*;

@CustomLog
public class ParserSqlScript implements SqlScript {
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

    private final SqlScriptMetadata metadata;
    protected final Parser parser;
    private final boolean mixed;
    private boolean parsed;

    private final boolean stream;
    private final Set<SqlScript> referencedSqlScripts = new TreeSet<>();

    /**
     * Creates a new sql script from this source.
     *
     * @param resource         The sql script resource.
     * @param metadataResource The sql script metadata resource.
     * @param mixed            Whether to allow mixing transactional and non-transactional statements within the same migration.
     */
    public ParserSqlScript(Parser parser, LoadableResource resource, LoadableResource metadataResource, boolean mixed) {
        this.resource = resource;
        this.metadata = SqlScriptMetadata.fromResource(metadataResource, parser, parser.configuration);
        this.parser = parser;
        this.stream = resource.shouldStream();
        this.mixed = mixed;
    }

    protected void parse() {
        try (SqlStatementIterator sqlStatementIterator = parser.parse(resource, metadata)) {
            boolean transactionalStatementFound = false;
            SqlStatement sqlStatement;
            while ((sqlStatement = sqlStatementIterator.next()) != null) {
                if (!stream) {
                    this.sqlStatements.add(sqlStatement);
                }

                sqlStatementCount++;

                if (sqlStatement.canExecuteInTransaction()) {
                    transactionalStatementFound = true;
                } else {
                    nonTransactionalStatementFound = true;
                }

                if (!mixed && transactionalStatementFound && nonTransactionalStatementFound && metadata.executeInTransaction() == null && parser.configuration.isExecuteInTransaction()) {
                    throw new FlywayException(
                            "Detected both transactional and non-transactional statements within the same migration"
                                    + " (even though mixed is false). Offending statement found at line "
                                    + sqlStatement.getLineNumber() + ": " + sqlStatement.getSql()
                                    + (sqlStatement.canExecuteInTransaction() ? "" : " [non-transactional]"));
                }









                LOG.debug("Found statement at line " + sqlStatement.getLineNumber() + ": " + sqlStatement.getSql()
                                  + (sqlStatement.canExecuteInTransaction() ? "" : " [non-transactional]"));
            }
        }
        parsed = true;
    }

    @Override
    public void validate() {
        if (!parsed) {
            parse();
        }
    }

    @Override
    public SqlStatementIterator getSqlStatements() {
        validate();

        if (stream) {
            return parser.parse(resource, metadata);
        }

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
                return iterator.hasNext() ? iterator.next() : null;
            }

            @Override
            public void remove() {
                iterator.remove();
            }
        };
    }

    @Override
    public int getSqlStatementCount() {
        validate();

        return sqlStatementCount;
    }

    @Override
    public Collection<SqlScript> getReferencedSqlScripts() {
        // Don't trigger validation if there can't be any referenced SQL scripts anyway
        if (parser.supportsReferencedSqlScripts()) {
            validate();
        }

        return referencedSqlScripts;
    }

    @Override
    public final LoadableResource getResource() {
        return resource;
    }

    @Override
    public boolean executeInTransaction() {
        Boolean executeInTransactionOverride = metadata.executeInTransaction();
        if (executeInTransactionOverride != null) {
            LOG.debug("Using executeInTransaction=" + executeInTransactionOverride + " from script configuration");
            return executeInTransactionOverride;
        }

        validate();

        return !nonTransactionalStatementFound && parser.configuration.isExecuteInTransaction();
    }

    @Override
    public boolean shouldExecute() {
        return metadata.shouldExecute();
    }

    @Override
    public String shouldExecuteExpression() {
        return metadata.shouldExecuteExpression();
    }

    @Override
    public boolean placeholderReplacement() {
        Boolean placeholderReplacementOverride = metadata.placeholderReplacement();
        if (placeholderReplacementOverride != null) {
            LOG.debug("Using placeholderReplacement=" + placeholderReplacementOverride + " from script configuration");
            return placeholderReplacementOverride;
        }

        return parser.configuration.isPlaceholderReplacement();
    }

    @Override
    public int compareTo(SqlScript o) {
        return resource.getRelativePath().compareTo(o.getResource().getRelativePath());
    }
}
