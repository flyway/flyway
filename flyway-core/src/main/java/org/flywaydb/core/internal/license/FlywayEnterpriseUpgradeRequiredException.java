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
package org.flywaydb.core.internal.license;

import org.flywaydb.core.api.FlywayException;

/**
 * Thrown when an attempt was made to use a Flyway Enterprise Edition feature not supported by
 * Flyway Community Edition or Flyway Pro Edition.
 */
public class FlywayEnterpriseUpgradeRequiredException extends FlywayException {
    public FlywayEnterpriseUpgradeRequiredException(String feature) {
        super(Edition.ENTERPRISE + " upgrade required: " + feature
                + " is not supported by " + Edition.COMMUNITY + " or " + Edition.PRO + ".");
    }
}