package io.jenkins.plugins.sonic;

import hudson.util.FormValidation;
import hudson.util.Secret;
import org.apache.commons.lang.StringUtils;

class ValidationParameters {

    public static FormValidation doCheckApiKey(String value) {
        String globalApiKey = Secret.toString(SonicGlobalConfiguration.get().getApiKey());
        if (StringUtils.isEmpty(globalApiKey ) &&
                value.length() == 0) {
            return FormValidation.error(Messages.UploadBuilder_DescriptorImpl_error_missApiKey());
        }
        return FormValidation.ok();
    }

    public static FormValidation doCheckScanDir(String value) {
        if (value.length() == 0) {
            return FormValidation.error(Messages.UploadBuilder_DescriptorImpl_error_missScanDir());
        }
        return FormValidation.ok();
    }

    public static FormValidation doCheckWildcard(String value) {
        if (value.length() == 0) {
            return FormValidation.error(Messages.UploadBuilder_DescriptorImpl_error_missWildcard());
        }
        return FormValidation.ok();
    }

}
