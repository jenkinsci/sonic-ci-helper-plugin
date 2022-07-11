/*
 *  Copyright (C) [SonicCloudOrg] Sonic Project
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package io.jenkins.plugins.sonic;

import hudson.util.FormValidation;
import org.apache.commons.lang.StringUtils;

class ValidationParameters {

    public static FormValidation doCheckApiKey(String value) {
        if (StringUtils.isEmpty(value)) {
            return FormValidation.error(Messages.UploadBuilder_DescriptorImpl_error_missApiKey());
        }
        return checkHost();
    }

    public static FormValidation doCheckProjectId(Integer value) {
        if (value == null ||
                value == 0) {
            return FormValidation.error(Messages.UploadBuilder_DescriptorImpl_error_missProjectId());
        }

        return checkHost();
    }

    private static FormValidation checkHost() {
        String host = SonicGlobalConfiguration.get().getHost();
        if (StringUtils.isEmpty(host)) {
            return FormValidation.error(Messages.SonicGlobalConfiguration_error_exception());
        }
        return FormValidation.ok();
    }

    public static FormValidation doCheckScanDir(String value) {
//        if (value.length() == 0) {
//            return FormValidation.error(Messages.UploadBuilder_DescriptorImpl_error_missScanDir());
//        }
        return checkHost();
    }

}
