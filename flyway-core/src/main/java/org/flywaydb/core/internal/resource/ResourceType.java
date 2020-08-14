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
package org.flywaydb.core.internal.resource;

public enum ResourceType {
    MIGRATION,



    REPEATABLE_MIGRATION,
    CALLBACK;

    /**
     * Whether the given resource type represents a resource that is versioned.
     */
    public static boolean isVersioned(ResourceType type) {
        return (type == ResourceType.MIGRATION



            );
    }
}