/*
 * Copyright 2010-2019 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.sqlserver;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.Token;
import org.flywaydb.core.internal.sqlscript.Delimiter;

import java.util.List;

public class SQLServerParser extends Parser {
    public SQLServerParser(Configuration configuration) {
        super(configuration, 3);
    }

    @Override
    protected Delimiter getDefaultDelimiter() {
        return Delimiter.GO;
    }

    @Override
    protected boolean isDelimiter(String peek, Delimiter delimiter) {
        return peek.length() >= 2
                && (peek.charAt(0) == 'G' || peek.charAt(0) == 'g')
                && (peek.charAt(1) == 'O' || peek.charAt(1) == 'o')
                && (peek.length() == 2 || Character.isWhitespace(peek.charAt(2)));
    }

    @Override
    protected Boolean detectCanExecuteInTransaction(String simplifiedStatement, List<Token> keywords) {
        String current = keywords.get(keywords.size() - 1).getText();
        if ("BACKUP".equals(current) || "RESTORE".equals(current) || "RECONFIGURE".equals(current)) {
            return false;
        }

        if (keywords.size() < 2) {
            return null;
        }

        String previous = keywords.get(keywords.size() - 2).getText();
        // #2175: The procedure 'sp_addsubscription' cannot be executed within a transaction.
        // #2298: The procedures 'sp_serveroption' and 'sp_droplinkedsrvlogin' cannot be executed within a transaction.
        // These procedures is only present in SQL Server. Not on Azure nor in PDW.
        if ("EXEC".equals(previous) && (
                "SP_ADDSUBSCRIPTION".equals(current) ||
                        "SP_DROPLINKEDSRVLOGIN".equals(current) ||
                        "SP_SERVEROPTION".equals(current))) {
            return false;
        }

        // (CREATE|DROP|ALTER) (DATABASE|FULLTEXT (INDEX|CATALOG))
        if (("CREATE".equals(previous) || "ALTER".equals(previous) || "DROP".equals(previous))
                && ("DATABASE".equals(current) || "FULLTEXT".equals(current))) {
            return false;
        }

        return null;
    }

    @Override
    protected int getTransactionalDetectionCutoff() {
        return Integer.MAX_VALUE;
    }
}