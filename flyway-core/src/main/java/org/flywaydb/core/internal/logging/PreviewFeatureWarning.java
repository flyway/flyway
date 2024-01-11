package org.flywaydb.core.internal.logging;

import lombok.CustomLog;

import java.util.ArrayList;
import java.util.List;

@CustomLog
public class PreviewFeatureWarning {
    private static final List<String> LOGGED_FEATURES = new ArrayList<>();
    public static boolean isPreviewFeatureEnabled(String featureName, String environmentVariable, boolean showHowToEnable) {
        if (System.getenv(environmentVariable) != null) {
            logPreviewFeature(featureName);
            return true;
        } else {
            if (showHowToEnable) {
                LOG.debug("Preview feature '" + featureName + "' is disabled.");
                LOG.debug("Enable it by setting the environment variable " + environmentVariable + "=true");
            }
        }
        return false;
    }
    public static void logPreviewFeature(String featureName) {
        if (LOGGED_FEATURES.contains(featureName)) {
            return;
        }

        LOG.info("-----------------------------------------------------------------------------");
        LOG.info("You are using a preview feature '" + featureName + "'.");
        LOG.info("Please report any issues you encounter to DatabaseDevOps@red-gate.com");
        LOG.info("-----------------------------------------------------------------------------");

        LOGGED_FEATURES.add(featureName);
    }
}