/**
 * Copyright 2010-2016 Boxfuse GmbH
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
package org.flywaydb.core.internal.batch;

import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.internal.dbsupport.DbSupport;

/**
 * Created on 07/08/16.
 *
 * @author Reda.Housni-Alaoui
 */
public interface MigrationBatchService {

    /**
     * @param dbSupport
     * @param migrationInfo
     * @return True if the migration is to be considered as the last of its batch.<br>
     *      A migration marked as last of batch will be followed by a commit on success.
     */
    boolean isLastOfBatch(DbSupport dbSupport, MigrationInfo migrationInfo);

}
