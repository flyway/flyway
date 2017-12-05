/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.maven;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.flywaydb.core.Flyway;

/**
 * <p>Validate applied migrations against resolved ones (on the filesystem or classpath)
 * to detect accidental changes that may prevent the schema(s) from being recreated exactly.</p>
 * <p>Validation fails if</p>
 * <ul>
 *     <li>differences in migration names, types or checksums are found</li>
 *     <li>versions have been applied that aren't resolved locally anymore</li>
 *     <li>versions have been resolved that haven't been applied yet</li>
 * </ul>
 *
 * <img src="https://flywaydb.org/assets/balsamiq/command-validate.png" alt="validate">
 */
@SuppressWarnings({"UnusedDeclaration", "JavaDoc"})
@Mojo(name = "validate",
        requiresDependencyResolution = ResolutionScope.TEST,
        defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST,
        threadSafe = true)
public class ValidateMojo extends AbstractFlywayMojo {
    @Override
    protected void doExecute(Flyway flyway) throws Exception {
        flyway.validate();
    }
}