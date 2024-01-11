package org.flywaydb.core.internal.proprietaryStubs;

import lombok.CustomLog;
import org.flywaydb.core.extensibility.ConfigurationExtension;
import org.flywaydb.core.extensibility.Plugin;

import static org.flywaydb.core.internal.util.FlywayDbWebsiteLinks.REDGATE_EDITION_DOWNLOAD;

@CustomLog
public class LicensingConfigurationExtensionStub implements ConfigurationExtension {

    private static final String LICENSE_KEY = "flyway.licenseKey";

    private String licenseKey; //This is actually needed. Config discovers valid parameters by looking at declared fields on config extensions.

    @Override
    public String getNamespace() {
        return "";
    }

    @Override
    public String getConfigurationParameterFromEnvironmentVariable(String environmentVariable) {
        if ("FLYWAY_LICENSE_KEY".equals(environmentVariable)) {
            return LICENSE_KEY;
        }
        return null;
    }

    public void setLicenseKey(String licenseKey) {
        LOG.warn("Attempting to set a license key in Flyway open-source. Redgate features will not be available. Download Redgate Flyway at " + REDGATE_EDITION_DOWNLOAD);
    }

    public String getLicenseKey() {
        return null;
    }

    @Override
    public int getPriority() {
        return -100;
    }

    @Override
    public Plugin copy() {
        return this;
    }

    @Override
    public boolean isStub() {
        return true;
    }
}