/**
 * Copyright 2010-2014 Axel Fontaine
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
package org.flywaydb.core.internal.dbsupport.firebird;

import org.flywaydb.core.internal.dbsupport.Delimiter;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;

/**
 * SqlStatementBuilder supporting Firebird-specific delimiter changes.
 */
public class FirebirdSqlStatementBuilder extends SqlStatementBuilder {

    private static final String SQL_TERM = "SET TERM";

    @Override
    public Delimiter extractNewDelimiterFromLine(String line) {
        String l = line.trim().toUpperCase();
        if (!l.startsWith(SQL_TERM) || l.length() < SQL_TERM.length() + 2) {
            return null;
        }
        String newTerm = StringUtils.collapseWhitespace(l).substring(SQL_TERM.length() + 1);
        if (newTerm.contains(" ")) { //terminator statement itself terminated
            newTerm = newTerm.substring(0, newTerm.lastIndexOf(' '));
        }
        return new Delimiter(newTerm, false);
    }

}
