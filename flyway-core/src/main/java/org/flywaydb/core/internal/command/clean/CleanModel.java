package org.flywaydb.core.internal.command.clean;

import lombok.Data;
import lombok.Getter;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.command.clean.CleanModeConfigurationExtension.Mode;

import java.util.Arrays;

@Data
public class CleanModel {
    private SchemaModel schemas = null;
    @Getter
    private String mode = null;


    public void validate(){
        try {
            if(this.mode != null) {
                Mode.valueOf(this.mode);
            }
        } catch (IllegalArgumentException e) {
            throw new FlywayException("Unknown clean mode: " + mode);
        }
    }

    public void setMode(String mode) {
        this.mode = mode != null ? mode.toUpperCase() : null;
    }

    public void setCleanSchemasExclude(String... cleanSchemasExclude) {
        setSchemas(new SchemaModel());
        schemas.setExclude(Arrays.asList(cleanSchemasExclude));
    }
}