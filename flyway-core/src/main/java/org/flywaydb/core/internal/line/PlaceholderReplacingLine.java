/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.internal.line;

import org.flywaydb.core.internal.placeholder.PlaceholderReplacer;

public class PlaceholderReplacingLine implements Line {
    private final String line;
    private final int lineNumber;

    public PlaceholderReplacingLine(Line line, PlaceholderReplacer placeholderReplacer) {
        this.line = placeholderReplacer.replacePlaceholders(line.getLine());
        this.lineNumber = line.getLineNumber();
    }

    @Override
    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public String getLine() {
        return line;
    }

    @Override
    public String toString() {
        return getLine();
    }
}