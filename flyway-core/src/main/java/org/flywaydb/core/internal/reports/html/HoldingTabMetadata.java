package org.flywaydb.core.internal.reports.html;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public class HoldingTabMetadata {
    private String name;
private List<String> supportedEditions;
public HoldingTabMetadata(String name, String... supportedEditions) {
        this.name = name;
        this.supportedEditions = Arrays.asList(supportedEditions);
    }
}