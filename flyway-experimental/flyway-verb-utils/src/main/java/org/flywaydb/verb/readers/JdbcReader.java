/*-
 * ========================LICENSE_START=================================
 * flyway-verb-utils
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
package org.flywaydb.verb.readers;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.experimental.ConnectionType;
import org.flywaydb.core.experimental.ExperimentalDatabase;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.sqlscript.SqlScriptMetadata;
import org.flywaydb.core.internal.sqlscript.SqlStatement;

public class JdbcReader implements Reader<SqlStatement> {

    public Stream<SqlStatement> read(final Configuration configuration,
        final ExperimentalDatabase database,
        final ParsingContext parsingContext,
        final LoadableResource loadableResource, 
        final SqlScriptMetadata metadata) {
        final Parser parser = database.getParser().apply(configuration, parsingContext);
        final Iterable<SqlStatement> iterable = () -> parser.parse(loadableResource, metadata);
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    @Override
    public boolean canRead(final ConnectionType connectionType) {
        return connectionType == ConnectionType.JDBC;
    }
}
