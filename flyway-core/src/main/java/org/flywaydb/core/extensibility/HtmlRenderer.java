package org.flywaydb.core.extensibility;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.HtmlResult;
import java.util.List;

public interface HtmlRenderer<T extends HtmlResult> extends Plugin {
    String render(T result, Configuration config);
    String tabTitle(T result, Configuration config);
    Class<T> getType();
    default List<HtmlReportSummary> getHtmlSummary(T result) {
        return null;
    }
}