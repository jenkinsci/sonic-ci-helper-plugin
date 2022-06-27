package io.jenkins.plugins.sonic;

import hudson.util.FormValidation;
import org.apache.commons.lang.StringUtils;

class ValidationParameters {

    public static FormValidation doCheckApiKey(String value) {
        if (StringUtils.isEmpty(value) &&
                value.length() == 0) {
            return FormValidation.error(Messages.UploadBuilder_DescriptorImpl_error_missApiKey());
        }
        return FormValidation.ok();
    }

    public static FormValidation doCheckProjectId(Integer value) {
        if (value == null ||
                value == 0) {
            return FormValidation.error(Messages.UploadBuilder_DescriptorImpl_error_missProjectId());
        }
        return FormValidation.ok();
    }

    public static FormValidation doCheckScanDir(String value) {
        if (value.length() == 0) {
            return FormValidation.error(Messages.UploadBuilder_DescriptorImpl_error_missScanDir());
        }
        return FormValidation.ok();
    }

}
