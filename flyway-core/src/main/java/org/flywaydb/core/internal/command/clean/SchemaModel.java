package org.flywaydb.core.internal.command.clean;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SchemaModel {
    private List<String> exclude = new ArrayList<>();
}