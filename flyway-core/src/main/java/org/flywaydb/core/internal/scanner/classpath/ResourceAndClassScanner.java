/*
 * Copyright 2010-2020 Redgate Software Ltd
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
package org.flywaydb.core.internal.scanner.classpath;

import org.flywaydb.core.internal.resource.LoadableResource;

import java.util.Collection;

/**
 * Scanner for both resources and classes.
 */
public interface ResourceAndClassScanner<I> {
    /**
     * Scans the classpath for resources under the configured location.
     *
     * @return The resources that were found.
     */
    Collection<LoadableResource> scanForResources();

    /**
     * Scans the classpath for concrete classes under the specified package implementing the specified interface.
     * Non-instantiable abstract classes are filtered out.
     *
     * @return The non-abstract classes that were found.
     */
    Collection<Class<? extends I>> scanForClasses();
}