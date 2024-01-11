package org.flywaydb.core.api.output;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DashboardResult extends HtmlResult {
    private List<HtmlResult> results;
}