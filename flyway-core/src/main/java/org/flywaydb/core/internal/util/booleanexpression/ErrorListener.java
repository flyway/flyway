/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.core.internal.util.booleanexpression;

import java.util.ArrayList;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

@RequiredArgsConstructor
public class ErrorListener extends BaseErrorListener {

    private final Collection<String> errorMessages = new ArrayList<>();
    private final String input;

    public boolean hasErrors() {
        return !errorMessages.isEmpty();
    }

    public String getErrorMessage() {
        return "Syntax error in expression: \"" + input + "\". Details: " + String.join(", ", errorMessages);
    }

    @Override
    public void syntaxError(final Recognizer<?, ?> recognizer,
        final Object offendingSymbol,
        final int line,
        final int charPositionInLine,
        final String msg,
        final RecognitionException e) {

        errorMessages.add(String.format("line %d:%d %s", line, charPositionInLine, msg));
    }
}
