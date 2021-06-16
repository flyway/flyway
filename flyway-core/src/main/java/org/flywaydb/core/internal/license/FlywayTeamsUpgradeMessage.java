/*
 * Copyright © Red Gate Software Ltd 2010-2021
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

import org.flywaydb.core.internal.util.LinkUtils;

public class FlywayTeamsUpgradeMessage {
    public static String generate(String detectedFeature, String usageMessage) {
        return "Detected " + detectedFeature + ". " +
               "Upgrade to " + Edition.ENTERPRISE + " to " + usageMessage + ". Try " + Edition.ENTERPRISE + " " +
                "for free: " +
                LinkUtils.createFlywayDbWebsiteLinkWithRef("desired-feature_" + detectedFeature,"try-flyway-teams-edition");
    }
}