package org.flywaydb.core.api.output;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HoldingResult extends HtmlResult {
    private String tabTitle;
    private String bodyText;
}