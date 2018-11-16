/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.api.migration;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.resolver.MigrationInfoHelper;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.Pair;

/**
 * <p>This is the recommended class to extend for implementing Java-based Migrations.</p>
 * <p>Subclasses should follow the default Flyway naming convention of having a class name with the following structure:</p>
 * <ul>
 * <li><strong>Versioned Migrations:</strong> V2__Add_new_table</li>
 * <li><strong>Undo Migrations:</strong> U2__Add_new_table</li>
 * <li><strong>Repeatable Migrations:</strong> R__Add_new_table</li>
 * </ul>
 *
 * <p>The file name consists of the following parts:</p>
 * <ul>
 * <li><strong>Prefix:</strong> V for versioned migrations, U for undo migrations, R for repeatable migrations</li>
 * <li><strong>Version:</strong> Underscores (automatically replaced by dots at runtime) separate as many parts as you like (Not for repeatable migrations)</li>
 * <li><strong>Separator:</strong> __ (two underscores)</li>
 * <li><strong>Description:</strong> Underscores (automatically replaced by spaces at runtime) separate the words</li>
 * </ul>
 * <p>If you need more control over the class name, you can override the default convention by implementing the
 * JavaMigration interface directly. This will allow you to name your class as you wish. Version, description and
 * migration category are provided by implementing the respective methods.</p>
 */
public abstract class BaseJavaMigration implements JavaMigration {
    private final MigrationVersion version;
    private final String description;




    /**
     * Creates a new instance of a Java-based migration following Flyway's default naming convention.
     */
    public BaseJavaMigration() {
        String shortName = ClassUtils.getShortName(getClass());
        String prefix;



        boolean repeatable = shortName.startsWith("R");
        if (shortName.startsWith("V") || repeatable



        ) {
            prefix = shortName.substring(0, 1);
        } else {
            throw new FlywayException("Invalid Java-based migration class name: " + getClass().getName()
                    + " => ensure it starts with V" +



                    " or R," +
                    " or implement org.flywaydb.core.api.migration.JavaMigration directly for non-default naming");
        }
        Pair<MigrationVersion, String> info =
                MigrationInfoHelper.extractVersionAndDescription(shortName, prefix, "__", new String[]{""}, repeatable);
        version = info.getLeft();
        description = info.getRight();
    }

    @Override
    public MigrationVersion getVersion() {
        return version;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Integer getChecksum() {
        return null;
    }

    @Override
    public boolean isUndo() {




        return false;

    }

    @Override
    public boolean canExecuteInTransaction() {
        return true;
    }
}