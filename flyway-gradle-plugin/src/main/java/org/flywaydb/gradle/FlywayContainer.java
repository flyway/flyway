package org.flywaydb.gradle;

public class FlywayContainer extends FlywayExtensionBase {
    /** The description used to identify instances of the container */
    public String name;

    /** The required constructor for Gradle containers */
    public FlywayContainer(String name) {
        this.name = name;
    }
}
