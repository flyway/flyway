package org.flywaydb.core.internal.parser;

import lombok.RequiredArgsConstructor;
import lombok.Getter;

@RequiredArgsConstructor
@Getter
public class Token {
    private final TokenType type;
    private final int pos;
    private final int line;
    private final int col;
    private final String text;
    private final String rawText;
    private final int parensDepth;
}