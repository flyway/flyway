/*
 * Copyright 2010-2020 Redgate Software Ltd
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

import org.flywaydb.core.api.logging.Log;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Color;
import org.fusesource.jansi.AnsiConsole;

import java.io.PrintStream;

/**
 * Wrapper which adds color to a Console output.
 */
class ColorizedConsoleLog implements Log {
    private final ConsoleLog log;

    public static void install(boolean force) {
        if (force) {
            System.setProperty("jansi.force", "true");
        }

        AnsiConsole.systemInstall();
    }

    public ColorizedConsoleLog(ConsoleLog log) {
        this.log = log;
    }

    @Override
    public boolean isDebugEnabled() {
        return this.log.isDebugEnabled();
    }

    @Override
    public void debug(String message) {
        colorizeBright(System.out, Color.BLACK);
        this.log.debug(message);
        reset(System.out);
    }

    @Override
    public void info(String message) {
        if (message.startsWith("Successfully")) {
            colorize(System.out, Color.GREEN);
            this.log.info(message);
            reset(System.out);
        } else {
            this.log.info(message);
        }
    }

    @Override
    public void warn(String message) {
        colorize(System.out, Color.YELLOW);
        this.log.warn(message);
        reset(System.out);
    }

    @Override
    public void error(String message) {
        colorize(System.err, Color.RED);
        this.log.error(message);
        reset(System.err);
    }

    @Override
    public void error(String message, Exception e) {
        colorize(System.err, Color.RED);
        this.log.error(message, e);
        reset(System.err);
    }

    private void colorize(PrintStream stream, Color color) {
        stream.print(Ansi.ansi().fg(color));
    }

    private void colorizeBright(PrintStream stream, Color color) {
        stream.print(Ansi.ansi().fgBright(color));
    }

    private void reset(PrintStream stream) {
        stream.print(Ansi.ansi().reset());
    }
}