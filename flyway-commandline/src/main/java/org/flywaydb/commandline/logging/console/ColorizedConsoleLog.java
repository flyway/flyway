package org.flywaydb.commandline.logging.console;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.logging.Log;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Color;
import org.fusesource.jansi.AnsiConsole;

import java.io.PrintStream;

@RequiredArgsConstructor
public class ColorizedConsoleLog implements Log {
    private final ConsoleLog log;

    public static void install(boolean force) {
        if (force) {
            System.setProperty("jansi.force", "true");
        }

        AnsiConsole.systemInstall();
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

    @Override
    public void notice(String message) {
        colorize(System.out, Color.BLUE);
        this.log.notice(message);
        reset(System.out);
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