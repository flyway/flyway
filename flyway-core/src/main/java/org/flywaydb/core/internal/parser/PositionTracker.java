/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
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