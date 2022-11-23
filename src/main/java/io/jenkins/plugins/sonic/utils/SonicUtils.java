package io.jenkins.plugins.sonic.utils;

import hudson.EnvVars;

public final class SonicUtils {

    private static final String IS_SONIC_UPLOAD = "isUploadSonic";
    private static final String IS_RUN_SUITE = "isRunSuite";

    /**
     *
     * @param envVars
     * @param listener
     * @return true is skip
     */
    public static boolean isSkipSonicUpload(EnvVars envVars, hudson.model.TaskListener listener) {
        boolean r = true;
        try {
            String isUploadSonic = envVars.get(IS_SONIC_UPLOAD, "true");
            r = Boolean.parseBoolean(isUploadSonic);
            if (!r) {
                Logging.logging(listener, "isUploadSonic: false , Uploading files to sonic will be skip ");
            }
        }catch (Exception e) {
            r = true;
            Logging.logging(listener, e.getMessage());
        }
        return !r;
    }

    /**
     *
     * @param envVars
     * @param listener
     * @return true is skip
     */
    public static boolean isSkipRunSuite(EnvVars envVars, hudson.model.TaskListener listener) {
        boolean r = true;
        try {
            String isRunSuite = envVars.get(IS_RUN_SUITE, "true");
            r = Boolean.parseBoolean(isRunSuite);
            if (!r) {
                Logging.logging(listener, "isRunSuite: false , run suite will be skip ");
            }
        }catch (Exception e) {
            r = true;
            Logging.logging(listener, e.getMessage());
        }
        return !r;
    }
}
