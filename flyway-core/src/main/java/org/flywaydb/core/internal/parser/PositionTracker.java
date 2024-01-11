package org.flywaydb.core.internal.parser;

import lombok.Getter;

public class PositionTracker {
    @Getter
    private int pos = 0;
    @Getter
    private int line = 1;
    @Getter
    private int col = 1;
    @Getter
    private int colIgnoringWhitespace = 1;

    private int markPos = 0;
    private int markLine = 1;
    private int markCol = 1;
    private int markColIgnoringWhitespace = 1;

    public void nextPos() {
        pos++;
    }

    public void nextCol() {
        col++;
    }

    public void nextColIgnoringWhitespace() {
        colIgnoringWhitespace++;
    }

    public void linefeed() {
        line++;
        col = 1;
        colIgnoringWhitespace = 1;
    }

    public void carriageReturn() {
        col = 1;
        colIgnoringWhitespace = 1;
    }

    public void mark() {
        markPos = pos;
        markLine = line;
        markCol = col;
        markColIgnoringWhitespace = colIgnoringWhitespace;
    }

    public void reset() {
        pos = markPos;
        line = markLine;
        col = markCol;
        colIgnoringWhitespace = markColIgnoringWhitespace;
    }
}