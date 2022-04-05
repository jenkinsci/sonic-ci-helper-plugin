package io.jenkins.plugins.sonic;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.util.FormValidation;
import hudson.util.Secret;
import jenkins.model.GlobalConfiguration;
import jenkins.model.GlobalConfigurationCategory;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
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
