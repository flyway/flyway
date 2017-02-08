/*
 * Copyright 2010-2017 Boxfuse GmbH
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
/**
 * 
 */
package org.flywaydb.core.internal.dbsupport.sybase.ase;

import org.flywaydb.core.internal.dbsupport.Delimiter;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;

/**
 * SqlStatementBuilder supporting Sybase Server-specific delimiter changes.
 * 
 * @author Jason Wong
 *
 */
public class SybaseASESqlStatementBuilder extends SqlStatementBuilder {

	@Override
    protected Delimiter getDefaultDelimiter() {
        return new Delimiter("GO", true);
    }
	
	@Override
    protected String computeAlternateCloseQuote(String openQuote) {
        char specialChar = openQuote.charAt(2);
        switch (specialChar) {
            case '(':
                return ")'";
            default:
                return specialChar + "'";
        }
    }
}
