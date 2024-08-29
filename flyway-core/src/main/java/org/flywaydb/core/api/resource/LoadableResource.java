/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2024 Red Gate Software Ltd
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
package org.flywaydb.core.api.resource;

import java.io.Reader;
import java.util.Objects;

/**
 * A loadable resource.
 */
public abstract class LoadableResource implements Resource, Comparable<LoadableResource> {
    /**
     * Reads the contents of this resource.
     *
     * @return The reader with the contents of the resource.
     */
    public abstract Reader read();

    /**
     * @return Whether it is recommended to stream this resource.
     */
    public boolean shouldStream() {
        return false;
    }

    @Override
    public int compareTo(LoadableResource o) {
        return getRelativePath().compareTo(o.getRelativePath());
    }
}
