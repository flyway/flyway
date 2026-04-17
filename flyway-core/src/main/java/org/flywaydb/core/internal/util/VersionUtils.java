/*-
 * ========================LICENSE_START=================================
 * flyway-core
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
package org.flywaydb.core.internal.util;

import java.lang.module.ModuleDescriptor.Version;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.flywaydb.core.internal.license.VersionPrinter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VersionUtils {
    public static boolean isLowerThan(final Version x, final Version y){
        return x.compareTo(y) < 0;
    }
    
    public static boolean isHigherThan(final Version x, final Version y){
        return x.compareTo(y) > 0;
    }
    
    public static boolean isEquivalentTo(final Version x, final Version y){
        return x.compareTo(y) == 0;
    }

    public static boolean isHigherThanOrEquivalentTo (final Version x, final Version y) {
        return isHigherThan(x, y) || isEquivalentTo(x, y);
    }

    public static boolean currentVersionIsHigherThanOrEquivalentTo(final Version current, final Version target) {
        return isHigherThanOrEquivalentTo(current, target);
    }
}
