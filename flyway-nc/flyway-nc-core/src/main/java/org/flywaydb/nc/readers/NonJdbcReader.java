/*-
 * ========================LICENSE_START=================================
 * flyway-nc-core
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
package org.flywaydb.nc.readers;

import static org.flywaydb.core.internal.util.FileUtils.getParentDir;

import java.nio.charset.Charset;
import java.util.stream.Stream;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.nc.ConnectionType;
import org.flywaydb.core.internal.nc.NativeConnectorsDatabase;
import org.flywaydb.core.internal.nc.Reader;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.sqlscript.SqlScriptMetadata;
import org.flywaydb.nc.FileReadingWithPlaceholderReplacement;
import org.flywaydb.nc.executors.NonJdbcExecutorExecutionUnit;

public class NonJdbcReader implements Reader<NonJdbcExecutorExecutionUnit> {
    public Stream<NonJdbcExecutorExecutionUnit> read(final Configuration configuration,
        final NativeConnectorsDatabase database,
        final ParsingContext parsingContext,
        final LoadableResource loadableResource,
        final SqlScriptMetadata metadata) {
        final Charset encoding = metadata != null && metadata.encoding() != null ?
            Charset.forName(metadata.encoding()) :
            configuration.getEncoding();
        final String content = FileReadingWithPlaceholderReplacement.readFile(configuration,
            parsingContext,
            loadableResource.getAbsolutePath(),
            encoding);
        return Stream.of(new NonJdbcExecutorExecutionUnit(
            content,
            getParentDir(loadableResource.getAbsolutePath()),
            encoding
            ));
    }

    @Override
    public boolean canRead(final ConnectionType connectionType) {
        return connectionType == ConnectionType.EXECUTABLE || connectionType == ConnectionType.API;
    }
}
