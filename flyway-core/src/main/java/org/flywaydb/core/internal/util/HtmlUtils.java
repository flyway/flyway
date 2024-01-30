package org.flywaydb.core.internal.util;

import org.apache.commons.text.StringEscapeUtils;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.HtmlResult;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.output.CompositeResult;

import java.io.File;
import java.io.FileWriter;
import java.time.format.DateTimeFormatter;

import static org.flywaydb.core.internal.reports.html.HtmlReportGenerator.generateHtml;
import static org.flywaydb.core.internal.util.FileUtils.createDirIfNotExists;

public class HtmlUtils {
    public static String toHtmlFile(String filename, CompositeResult<HtmlResult> results, Configuration config) {
        String fileContents = generateHtml(results, config);

        File file = new File(filename);

        try {
            createDirIfNotExists(file);
        } catch (UnsupportedOperationException ignore) {

        }

        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(fileContents);
            return file.getCanonicalPath();
        } catch (Exception e) {
            throw new FlywayException("Unable to write HTML to file: " + e.getMessage());
        }

    }

    public static String getFormattedTimestamp(HtmlResult result) {
        if (result == null || result.getTimestamp() == null) {
            return "--";
        }
        return result.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static String htmlEncode(String input) {
        return StringEscapeUtils.escapeHtml4(input);
    }
}