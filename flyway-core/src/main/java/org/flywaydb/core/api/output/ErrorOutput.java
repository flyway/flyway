package org.flywaydb.core.api.output;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.flywaydb.core.api.ErrorCode;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.command.DbMigrate;
import org.flywaydb.core.internal.sqlscript.FlywaySqlScriptException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class ErrorOutput implements OperationResult {

    @AllArgsConstructor(access = AccessLevel.PACKAGE)
    public static class ErrorOutputItem {
        public ErrorCode errorCode;
        public String message;
        public String stackTrace;
        public Integer lineNumber;
        public String path;
    }

    public ErrorOutputItem error;

    public ErrorOutput(ErrorCode errorCode, String message, String stackTrace, Integer lineNumber, String path) {
        this.error = new ErrorOutputItem(errorCode, message, stackTrace, lineNumber, path);
    }

    public static ErrorOutput fromException(Exception exception) {
        String message = exception.getMessage();

        if (exception instanceof DbMigrate.FlywayMigrateException && exception.getCause() instanceof FlywaySqlScriptException) {
            FlywaySqlScriptException flywaySqlScriptException = (FlywaySqlScriptException) exception.getCause();

            return new ErrorOutput(
                    ((DbMigrate.FlywayMigrateException) exception).getMigrationErrorCode(),
                    message == null ? "Error occurred" : message,
                    null,
                    flywaySqlScriptException.getLineNumber(),
                    flywaySqlScriptException.getResource().getAbsolutePathOnDisk());
        }

        if (exception instanceof FlywayException) {
            FlywayException flywayException = (FlywayException) exception;

            return new ErrorOutput(
                    flywayException.getErrorCode(),
                    message == null ? "Error occurred" : message,
                    null,
                    null,
                    null);
        }

        return new ErrorOutput(
                ErrorCode.FAULT,
                message == null ? "Fault occurred" : message,
                getStackTrace(exception),
                null,
                null);
    }

    public static MigrateErrorResult fromMigrateException(DbMigrate.FlywayMigrateException exception) {
        return exception.getErrorResult();
    }

    public static OperationResult toOperationResult(Exception exception) {
        if (exception instanceof DbMigrate.FlywayMigrateException) {
            return fromMigrateException((DbMigrate.FlywayMigrateException) exception);
        } else {
            return fromException(exception);
        }
    }

    private static String getStackTrace(Exception exception) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream printStream;

        try {
            printStream = new PrintStream(output, true, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            return "";
        }

        exception.printStackTrace(printStream);

        return new String(output.toByteArray(), StandardCharsets.UTF_8);
    }
}