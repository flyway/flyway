import com.googlecode.flyway.core.Flyway;
import com.googlecode.flyway.core.util.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * Main class and central entry point of the Flyway command-line tool.
 */
public class Main {
    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(Main.class);

    /**
     * Main method.
     *
     * @param args The command-line arguments.
     */
    public static void main(String[] args) {
        try {
            Flyway flyway = new Flyway();

            flyway.configure(loadConfigurationFile(args));
            overrideConfiguration(flyway, args);

            String operation = determineOperation(args);
            if ("clean".equals(operation)) {
                flyway.clean();
            } else if ("init".equals(operation)) {
                flyway.init(null, null);
            } else if ("migrate".equals(operation)) {
                flyway.migrate();
            } else if ("validate".equals(operation)) {
                flyway.validate();
            } else if ("status".equals(operation)) {
                flyway.status();
            } else if ("history".equals(operation)) {
                flyway.history();
            }
        } catch (Exception e) {
            LOG.error(e.toString());

            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause != null) {
                LOG.error(rootCause.toString());
            }

            System.exit(1);
        }
    }

    /**
     * Loads the configuration from the configuration file. If a configuration file is specified using the -configfile
     * argument it will be used, otherwise the default config file (conf/flyway.properties) will be loaded.
     *
     * @param args The command-line arguments passed in.
     *
     * @return The loaded configuration.
     */
    private static Properties loadConfigurationFile(String[] args) throws IOException {
        final String configFileParam = "-configFile=";

        String configFile = getInstallationDir() + "/conf/flyway.properties";
        for (String arg : args) {
            if (arg.startsWith(configFileParam)) {
                if (arg.length() == configFileParam.length()) {
                    //Empty value
                    return new Properties();
                }
                configFile = arg.substring(configFileParam.length());
                break;
            }
        }

        Properties properties = new Properties();
        properties.load(new InputStreamReader(new FileInputStream(configFile), determineConfigurationFileEncoding(args)));
        return properties;
    }

    private static String determineConfigurationFileEncoding(String[] args) {
        return null;
    }

    /**
     * @return The installation directory of the Flyway Command-line tool.
     */
    private static String getInstallationDir() {
        return "";
    }

    /**
     * Overrides the configuration from the config file with the properties passed in directly from the command-line.
     *
     * @param flyway The Flyway instance to configure.
     * @param args The command-line arguments that were passed in.
     */
    private static void overrideConfiguration(Flyway flyway, String[] args) {
        for (String arg : args) {
            if (isArgumentForProperty(arg, "")) {
                flyway.setBaseDir(getArgumentValue(arg));
            }
        }
    }

    private static String determineOperation(String[] args) {
        return null;
    }
}
