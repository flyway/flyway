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

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.sqlscript.Delimiter;

import java.security.InvalidParameterException;
import java.util.Stack;

public class ParserContext {
    private int parensDepth = 0;
    private Stack<String> blockInitiators = new Stack<>();
    private String lastClosedBlockInitiator = null;
    private int blockDepth = 0;
    private Delimiter delimiter;
    private StatementType statementType;

    public ParserContext(Delimiter delimiter) {
        this.delimiter = delimiter;
    }

    public void increaseParensDepth() {
        parensDepth++;
    }

    public void decreaseParensDepth() {
        parensDepth--;
    }

    public int getParensDepth() {
        return parensDepth;
    }

    // When a block is closed, retain the token that opened it, so that we can determine what the
    // context of a given END is.
    public String getLastClosedBlockInitiator() {
        return lastClosedBlockInitiator;
    }

    public void increaseBlockDepth(String blockInitiator) {
        blockInitiators.push(blockInitiator);
        blockDepth++;
    }

    public void decreaseBlockDepth() {
        if (blockDepth == 0) {
            throw new FlywayException("Flyway parsing bug: unable to decrease block depth below 0");
        }
        blockDepth--;
        lastClosedBlockInitiator = blockInitiators.pop();
    }

    public int getBlockDepth() {
        return blockDepth;
    }

    public Delimiter getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(Delimiter delimiter) {
        this.delimiter = delimiter;
    }

    public StatementType getStatementType() {
        return statementType;
    }

    public void setStatementType(StatementType statementType) {
        if (statementType == null) {
            throw new InvalidParameterException("statementType must be non-null");
        }

        this.statementType = statementType;
    }

    public boolean isLetter(char c) {
        if (Character.isLetter(c)) {
            return true;
        }
        // Some statement types admit other characters as letters
        if (getStatementType() != StatementType.UNKNOWN) {
            return statementType.treatAsIfLetter(c);
        }
        return false;
    }
}