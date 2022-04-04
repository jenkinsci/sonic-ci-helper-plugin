package io.jenkins.plugins.sonic.utils;

import hudson.model.BuildListener;

public class Logging {
    private static final String TAG = "Sonic: ";


    public static void logging(BuildListener listener, String message) {
        listener.getLogger().println(TAG + message);
    }

    public static void printHeader(BuildListener listener) {
        listener.getLogger().println("===========================");
        listener.getLogger().println("===========Sonic==========");
        listener.getLogger().println("===========================");
    }
}
