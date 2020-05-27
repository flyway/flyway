/*
 * Copyright 2010-2020 Redgate Software Ltd
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
package org.flywaydb.core.internal.parser;

import java.util.List;

class Statement {
    private final int pos;
    private final int line;
    private final int col;
    private final StatementType statementType;
    private final String sql;
    private final List<Token> tokens;

    Statement(int pos, int line, int col, StatementType statementType, String sql, List<Token> tokens) {
        this.pos = pos;
        this.line = line;
        this.col = col;
        this.statementType = statementType;
        this.sql = sql;
        this.tokens = tokens;
    }

    public int getPos() {
        return pos;
    }

    public int getLine() {
        return line;
    }

    public int getCol() {
        return col;
    }

    public StatementType getStatementType() {
        return statementType;
    }

    public String getSql() {
        return sql;
    }

    public List<Token> getTokens() {
        return tokens;
    }
}