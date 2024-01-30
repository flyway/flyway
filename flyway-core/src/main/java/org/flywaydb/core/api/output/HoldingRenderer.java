package org.flywaydb.core.api.output;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.extensibility.HtmlRenderer;

public class HoldingRenderer implements HtmlRenderer<HoldingResult> {
    @Override
    public String render(HoldingResult result, Configuration config) {
        return result.getBodyText();
    }

    @Override
    public String tabTitle(HoldingResult result, Configuration config) {
        return result.getTabTitle();
    }

    @Override
    public Class<HoldingResult> getType() {
        return HoldingResult.class;
    }
}