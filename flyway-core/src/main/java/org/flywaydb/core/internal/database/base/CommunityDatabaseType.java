/*
 * Copyright (C) Red Gate Software Ltd 2010-2024
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
package org.flywaydb.core.internal.database.base;

import static org.flywaydb.core.internal.util.FlywayDbWebsiteLinks.COMMUNITY_CONTRIBUTED_DATABASES;
import org.flywaydb.core.internal.database.DatabaseType;

public interface CommunityDatabaseType extends DatabaseType {

     default String announcementForCommunitySupport() {
        return getName() + " is a community contributed database, see "+ COMMUNITY_CONTRIBUTED_DATABASES + " for more details";
    }

}