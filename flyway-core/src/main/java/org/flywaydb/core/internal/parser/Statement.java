package org.flywaydb.core.internal.parser;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Getter;

import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
class Statement {
    private final int pos;
    private final int line;
    private final int col;
    private final StatementType statementType;
    private final String sql;
    private final List<Token> tokens;
}