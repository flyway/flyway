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
 * SolidDB support developed 2014 by Sabine Gallus & Michael Forstner
 * Media-Saturn IT Services GmbH
 * Wankelstr. 5
 * 85046 Ingolstadt, Germany
 * http://www.media-saturn.com
 */

package org.flywaydb.core.internal.dbsupport.solid;

import org.flywaydb.core.internal.dbsupport.Delimiter;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;

public class SolidSqlStatementBuilder extends SqlStatementBuilder {

    @Override
    public Delimiter changeDelimiterIfNecessary(final String line, final Delimiter delimiter) {
        if (line.startsWith("\"")) {
            return new Delimiter("\"", false);
        }
        if (line.endsWith("\";")) {
            return getDefaultDelimiter();
        }
        return delimiter;
    }
}
