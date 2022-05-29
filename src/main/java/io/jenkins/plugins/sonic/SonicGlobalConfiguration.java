package io.jenkins.plugins.sonic;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.util.FormValidation;
import hudson.util.Secret;
import jenkins.model.GlobalConfiguration;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;


@Extension
public class SonicGlobalConfiguration extends GlobalConfiguration {

    private String host;
    private Secret apiKey;

    public SonicGlobalConfiguration() {
        load();
    }

    @DataBoundSetter
    public void setHost(String host) {
        this.host = host;
        save();
    }

    @DataBoundSetter
    public void setApiKey(Secret apiKey) {
        this.apiKey = apiKey;
        save();
    }

    public String getHost() {
        return host;
    }

    public Secret getApiKey() {
        return apiKey;
    }

    public static String planApiKey(Secret apiKey){
        String globalApiKey = Secret.toString(SonicGlobalConfiguration.get().getApiKey());
        String currentApiKey = Secret.toString(apiKey);
        return StringUtils.isBlank(currentApiKey) ? globalApiKey: currentApiKey;
    }

    /** @return the singleton instance */
    public static SonicGlobalConfiguration get() {
        return ExtensionList.lookupSingleton(SonicGlobalConfiguration.class);
    }

    public FormValidation doCheckHost(@QueryParameter String value) {
        if (StringUtils.isEmpty(value)) {
            return FormValidation.warning(Messages.SonicGlobalConfiguration_error_missHost());
        }
        return FormValidation.ok();
    }

    public FormValidation doCheckApiKey(@QueryParameter String value) {
        if (StringUtils.isEmpty(value)) {
            return FormValidation.warning(Messages.SonicGlobalConfiguration_error_missApiKey());
        }
        return FormValidation.ok();
    }
}
