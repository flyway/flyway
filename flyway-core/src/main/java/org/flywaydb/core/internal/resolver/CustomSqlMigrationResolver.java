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
package org.flywaydb.core.internal.resolver;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.resolver.sql.SqlMigrationResolver;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.scanner.Resource;

public class CustomSqlMigrationResolver extends SqlMigrationResolver {

    @Override
    protected Pair<MigrationVersion, String> extractVersionAndDescription(String prefix, String separator, String suffix, Resource resource, Location location, boolean repeatable) {
        Pair<MigrationVersion, String> result = super.extractVersionAndDescription(prefix, separator, suffix, resource, location, repeatable);

        return Pair.of(MigrationVersion.fromVersion("99." + result.getLeft().getVersion()), result.getRight());
    }
}
