/*
 * Copyright © Red Gate Software Ltd 2010-2020
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
package org.flywaydb.core.internal.configuration;


import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConfigUtilsTest {

    @Test
    public void maskPasswordInTheEndOfUrl() {
        assertEquals("jdbc:postgresql://postgres:5432/test_db?user=test_user&password=*****",
                ConfigUtils.maskPasswordInUrl("jdbc:postgresql://postgres:5432/test_db?user=test_user&password=wrong_pass")
        );
    }

    @Test
    public void maskPasswordInTheMiddleOfUrl() {
        assertEquals("jdbc:postgresql://postgres:5432/test_db?password=*****&user=test_user",
                ConfigUtils.maskPasswordInUrl("jdbc:postgresql://postgres:5432/test_db?password=wrong_pass&user=test_user")
        );
    }
}
