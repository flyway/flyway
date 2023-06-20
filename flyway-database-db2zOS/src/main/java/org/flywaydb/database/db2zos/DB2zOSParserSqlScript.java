/*
 * Copyright (C) Red Gate Software Ltd 2010-2023
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
package org.flywaydb.database.db2zos;

import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.sqlscript.*;

public class DB2zOSParserSqlScript extends ParserSqlScript {
    /**
     * Creates a new sql script from this source.
     *
     * @param parser The parser to use.
     * @param resource The sql script resource.
     * @param metadataResource The sql script metadata resource.
     * @param mixed Whether to allow mixing transactional and non-transactional statements within the same migration.
     */
    public DB2zOSParserSqlScript(Parser parser, LoadableResource resource, LoadableResource metadataResource, boolean mixed) {
        super(parser, resource, metadataResource, mixed);
    }

    @Override
    public SqlStatementIterator getSqlStatements() {
        return new ExtraCommitSqlStatementIterator(super.getSqlStatements());
    }

    /**
     * Iterator that adds an extra COMMIT statement at the end.
     */
    static class ExtraCommitSqlStatementIterator implements SqlStatementIterator {
        private final SqlStatementIterator original;
        private boolean commitAdded;

        private ExtraCommitSqlStatementIterator(SqlStatementIterator original) {
            this.original = original;
        }

        @Override
        public boolean hasNext() {
            return original.hasNext() || !commitAdded;
        }

        @Override
        public SqlStatement next() {
            if (!original.hasNext() && !commitAdded) {
                commitAdded = true;
                return new ParsedSqlStatement(0, 0, 0, "COMMIT", Delimiter.SEMICOLON, true



                        );
            }
            return original.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }

        @Override
        public void close() {
            original.close();
        }
    }
}