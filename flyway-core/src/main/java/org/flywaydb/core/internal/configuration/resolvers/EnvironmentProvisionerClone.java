/*
 * Copyright (C) Red Gate Software Ltd 2010-2023
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
package org.flywaydb.core.internal.configuration.resolvers;

import lombok.CustomLog;
import org.flywaydb.core.ProgressLogger;

@CustomLog
public class EnvironmentProvisionerClone implements EnvironmentProvisioner {
    @Override
    public String getName() {
        return "clone";
    }

    @Override
    public void preProvision(PropertyResolverContext context, ProgressLogger progress) {
        LOG.warn("Provisioner 'clone' specified for environment " + context.getEnvironmentName() + " was requested (provision) but not run.");
    }

    @Override
    public void preReprovision(PropertyResolverContext context, ProgressLogger progress) {
        LOG.warn("Provisioner 'clone' specified for environment " + context.getEnvironmentName() + " was requested (reprovision) but not run.");
    }
}