/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.internal.clazz;

import java.util.Collection;

/**
 * A facility to obtain classes.
 */
public interface ClassProvider {
    /**
     * Retrieve all classes which implement this interface.
     *
     * @param implementedInterface The interface the matching classes should implement.
     * @return The non-abstract classes that were found.
     */
    <I> Collection<Class<? extends I>> getClasses(Class<I> implementedInterface);
}