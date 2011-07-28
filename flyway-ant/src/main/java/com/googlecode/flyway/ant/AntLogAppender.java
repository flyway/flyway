package com.googlecode.flyway.ant;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

public class AntLogAppender extends AppenderSkeleton {

    private static Project antProject;

    public static void startTaskLog(Project project) {
        antProject = project;
    }

    public static void endTaskLog() {
        antProject = null;
    }

    protected void append(LoggingEvent event) {
        if (antProject == null) {
            return;
        }
        Level level = event.getLevel();

        String text = this.layout.format(event);
        Throwable throwable = null;
        if (event.getThrowableInformation() != null) {
            throwable = event.getThrowableInformation().getThrowable();
        }

        Task task = antProject.getThreadTask(Thread.currentThread());

        if (Level.TRACE.equals(level)) {
            antProject.log(task, text, throwable, Project.MSG_DEBUG);
        } else if (Level.DEBUG.equals(level)) {
            antProject.log(task, text, throwable, Project.MSG_VERBOSE);
        } else if (Level.INFO.equals(level)) {
            antProject.log(task, text, throwable, Project.MSG_INFO);
        } else if (Level.WARN.equals(level)) {
            antProject.log(task, text, throwable, Project.MSG_WARN);
        } else if (Level.ERROR.equals(level) || Level.FATAL.equals(level)) {
            antProject.log(task, text, throwable, Project.MSG_ERR);
        } else {
            antProject.log(task, text, throwable, Project.MSG_ERR);
        }
    }

    public void close() {
        antProject = null;
    }

    public boolean requiresLayout() {
        return true;
    }
}