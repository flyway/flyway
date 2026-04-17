/*-
 * ========================LICENSE_START=================================
 * flyway-nc-core
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
package org.flywaydb.nc;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.configuration.models.ResolvedEnvironment;
import org.flywaydb.core.internal.nc.AbstractNativeConnectorsHybridDatabase;

public abstract class NativeConnectorsHybrid<T, U extends NativeConnectorsJdbc, V extends NativeConnectorsNonJdbc> extends AbstractNativeConnectorsHybridDatabase<T> {
    protected U innerJdbc;
    protected V innerNonJdbc;

    public U toNativeConnectorsJdbc() {
        return innerJdbc;
    }

    public V NativeConnectorsNonJdbc() {
        return innerNonJdbc;
    }

    @Override
    public void initialize(final ResolvedEnvironment environment, final Configuration configuration) {
        innerJdbc.initialize(environment, configuration);
        innerNonJdbc.initialize(environment, configuration);
    }

    @Override
    public void close() {
        innerJdbc.close();
        innerNonJdbc.close();
    }
}
