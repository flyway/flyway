package org.flywaydb.core.internal.parser;

public class Recorder {
    private StringBuilder recorder;
    private boolean recorderPaused = false;
    private int recorderConfirmedPos = 0;

    public void record(char c) {
        if (isRunning()) {
            recorder.append(c);
        }
    }

    public int length() {
        return recorder.length();
    }

    public void truncate(int length) {
        if (isRunning()) {
            recorder.delete(length, recorder.length());
        }
    }

    private boolean isRunning() {
        return recorder != null && !recorderPaused;
    }

    public void start() {
        recorder = new StringBuilder();
        recorderConfirmedPos = 0;
        recorderPaused = false;
    }

    public void pause() {
        recorderPaused = true;
    }

    public void record(String str) {
        recorder.append(str);
        confirm();
    }

    public void confirm() {
        recorderConfirmedPos = recorder.length();
    }

    public String stop() {
        // Drop unconfirmed parts of recording
        recorder.delete(recorderConfirmedPos, recorder.length());

        String result = recorder.toString();
        recorder = null;
        return result;
    }
}