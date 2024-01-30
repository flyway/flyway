package org.flywaydb.core.internal.parser;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.sqlscript.Delimiter;

import java.security.InvalidParameterException;
import java.util.Stack;

public class ParserContext {
    private int parensDepth = 0;
    private int blockDepth = 0;
    private final Stack<String> blockInitiators = new Stack<>();
    private String lastClosedBlockInitiator = null;
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

    public String getBlockInitiator() {
        return blockInitiators.size() > 0 ? blockInitiators.peek() : "";
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