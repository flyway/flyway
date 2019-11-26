/*
 * Copyright 2010-2019 Boxfuse GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.commandline;

import org.flywaydb.commandline.PrintStreamLog.Level;

class CommandLineFlags {
    private boolean isSuppressPrompt;
    private boolean printVersionAndExit;
    private boolean jsonOutput;
    private boolean printUsage;
    private Level logLevel;

    private CommandLineFlags(
            boolean isSuppressPrompt,
            boolean printVersionAndExit,
            boolean jsonOutput,
            boolean printUsage,
            Level logLevel) {

        this.isSuppressPrompt = isSuppressPrompt;
        this.printVersionAndExit = printVersionAndExit;
        this.jsonOutput = jsonOutput;
        this.printUsage = printUsage;
        this.logLevel = logLevel;
    }

    public boolean getIsSuppressPrompt() {
        return isSuppressPrompt;
    }

    public boolean getPrintVersionAndExit() {
        return printVersionAndExit;
    }

    public boolean getJsonOutput() {
        return jsonOutput;
    }

    public boolean getPrintUsage() {
        return printUsage;
    }

    public Level getLogLevel() {
        return logLevel;
    }


    public static CommandLineFlags createFromArguments(String[] args) {
        boolean isSuppressPrompt = false;
        boolean printVersionAndExit = false;
        boolean jsonOutput = false;
        boolean printUsage = false;
        Level logLevel = Level.INFO;

        if (isFlagSet(args, "-n")) {
            isSuppressPrompt = true;
        }

        if (isFlagSet(args, "-v")) {
            printVersionAndExit = true;
        }

        if (isFlagSet(args, "-?")) {
            printUsage = true;
        }

        if (isFlagSet(args, "-X")) {
            logLevel = Level.DEBUG;
        }

        if (isFlagSet(args, "-q")) {
            logLevel = Level.WARN;
        }

        if (isFlagSet(args, "-json")) {
            jsonOutput = true;
        }

        return new CommandLineFlags(
                isSuppressPrompt,
                printVersionAndExit,
                jsonOutput,
                printUsage,
                logLevel);
    }

    private static boolean isFlagSet(String[] args, String flag) {
        for (String arg : args) {
            if (flag.equals(arg)) {
                return true;
            }
        }
        return false;
    }
}