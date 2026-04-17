/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
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
package org.flywaydb.core.internal.nc;

import java.lang.module.ModuleDescriptor.Version;
import lombok.experimental.ExtensionMethod;
import org.flywaydb.core.internal.util.VersionUtils;

@ExtensionMethod(VersionUtils.class)
public class DatabaseVersionImpl implements DatabaseVersion {
    private final Version version;

    public DatabaseVersionImpl(final String version) {
        this.version = Version.parse(version);
    }

    @Override
    public boolean isAtLeast(final String expected) {
        return version.isHigherThanOrEquivalentTo(Version.parse(expected));
    }

    @Override
    public String toString() {
        return version.toString();
    }

}
