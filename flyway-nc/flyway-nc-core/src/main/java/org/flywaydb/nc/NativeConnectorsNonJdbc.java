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
package org.flywaydb.nc;

import org.flywaydb.core.internal.nc.AbstractNativeConnectorsDatabase;
import org.flywaydb.nc.executors.NonJdbcExecutorExecutionUnit;

public abstract class NativeConnectorsNonJdbc extends AbstractNativeConnectorsDatabase<NonJdbcExecutorExecutionUnit> {

    /**
     * By default, databases that use Batch to implement Transactions cannot support Batch, as it would cause a conflict.
     */
    @Override
    public boolean supportsBatch() {
        return !transactionAsBatch();
    }

    /**
     * Only applies to certain non-JDBC databases that put all statements within a block and execute them as a single transaction.
     * For these databases, transactions are handled via batching
     */
    public boolean transactionAsBatch() {
        return false;
    }

    @Override
    public void doExecuteBatch() {

    }
}
