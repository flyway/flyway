/**
 * Copyright (C) 2009-2010 the original author or authors.
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

package com.googlecode.flyway.core.dbsupport.oracle;

import com.googlecode.flyway.core.SqlScript;
import org.springframework.core.io.Resource;

import java.util.Map;

/**
 * SqlScript supporting Oracle-specific PL/SQL constructs.
 */
public class OracleSqlScript extends SqlScript {
    /**
     * Creates a new sql script from this resource with these placeholders to replace.
     *
     * @param resource     The resource containing the sql script.
     * @param placeholders A map of <placeholder, replacementValue> to replace in sql statements.
     * @throws IllegalStateException Thrown when the script could not be read from this resource.
     */
    public OracleSqlScript(Resource resource, Map<String, String> placeholders) {
        super(resource, placeholders);
    }

    @Override
    protected String checkForNewDelimiter(String line) {
        if (line.toUpperCase().startsWith("DECLARE") || line.toUpperCase().startsWith("BEGIN")) {
            return "/";
        }

        return null;
    }
}
