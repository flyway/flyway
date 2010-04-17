package com.google.code.flyway.core.java;

import com.google.code.flyway.core.Migration;
import com.google.code.flyway.core.SchemaVersion;
import com.google.code.flyway.core.util.MigrationUtils;
import org.springframework.util.ClassUtils;

/**
 * Base class for java migration classes whose name conforms to Vmajor_minor.
 */
public abstract class BaseJavaMigration implements Migration {
    @Override
    public SchemaVersion getVersion() {
        String className = ClassUtils.getShortName(getClass());
        return MigrationUtils.extractSchemaVersion(className);
    }

    @Override
    public String getScriptName() {
        return "Java Class: " + ClassUtils.getShortName(getClass());
    }
}
