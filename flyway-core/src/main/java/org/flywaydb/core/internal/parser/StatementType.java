package org.flywaydb.core.internal.parser;

public class StatementType {
    public static final StatementType GENERIC = new StatementType();
    public static final StatementType UNKNOWN = new StatementType();

    /**
     * Whether the character should be treated as if it is a letter; this allows statement types to handle
     * characters that appear in specific contexts
     *
     * @param c
     * @return
     */
    public boolean treatAsIfLetter(char c) {
        return false;
    }
}