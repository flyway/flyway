/**
 * Copyright 2010-2014 Axel Fontaine
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
package org.flywaydb.core.internal.resolver.groovy;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.resolver.MigrationExecutor;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.scanner.Resource;

import java.sql.Connection;

public class GroovyMigrationExecutor implements MigrationExecutor {

    private final Resource resource;
    private final PlaceholderReplacer placeholderReplacer;
    private final String encoding;

    public GroovyMigrationExecutor(Resource resource,PlaceholderReplacer placeholderReplacer,String encoding) {
        this.resource = resource;
        this.placeholderReplacer = placeholderReplacer;
        this.encoding = encoding;
    }

    public void execute(Connection connection) {
        try {
            String groovyScriptSource = resource.loadAsString(encoding);
            groovy.lang.Script groovyMigration = new GroovyShell().parse(groovyScriptSource);
            groovyMigration.getBinding().setVariable("connection", connection);
            groovyMigration.run();
        } catch (Exception e) {
            throw new FlywayException("Migration failed !", e);
        }
    }

    @Override
    public boolean executeInTransaction() {
        return true;
    }
}
