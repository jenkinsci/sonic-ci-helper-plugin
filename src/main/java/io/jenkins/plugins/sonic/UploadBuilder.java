package io.jenkins.plugins.sonic;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.Secret;
import io.jenkins.plugins.sonic.bean.ParamBean;
import io.jenkins.plugins.sonic.utils.HttpUtils;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;

/**
 * upload to jenkins
 *
 * @author yaming116
 */
public class UploadBuilder extends Builder {

    private final Secret apiKey;
    private final String scanDir;
    private final String wildcard;
    private final String updateDescription;

    private final String qrcodePath;

    @DataBoundConstructor
    public UploadBuilder(String apiKey, String scanDir, String wildcard,String updateDescription, String qrcodePath) {
        this.apiKey = Secret.fromString(apiKey);
        this.scanDir = scanDir;
        this.wildcard = wildcard;
        this.updateDescription = updateDescription;
        this.qrcodePath = qrcodePath;
    }

    public Secret getApiKey() {
        return apiKey;
    }

    public String getScanDir() {
        return scanDir;
    }

    public String getWildcard() {
        return wildcard;
    }

    public String getUpdateDescription() {
        return updateDescription;
    }

    public String getQrcodePath() {
        return qrcodePath;
    }


    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        String host = SonicGlobalConfiguration.get().getHost();

        ParamBean paramBean = new ParamBean();
        paramBean.setHost(host);
        String globalApiKey = Secret.toString(SonicGlobalConfiguration.get().getApiKey());
        String currentApiKey = Secret.toString(apiKey);
        paramBean.setApiKey(StringUtils.isEmpty(currentApiKey) ? globalApiKey: currentApiKey);
        paramBean.setScanDir(this.scanDir);
        paramBean.setWildcard(this.wildcard);
        paramBean.setUpdateDescription(this.updateDescription);
        paramBean.setQrcodePath(this.qrcodePath);
        return HttpUtils.upload(build, listener, paramBean);
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Symbol("upload-sonic")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public DescriptorImpl() {
            load();
        }

        public FormValidation doCheckApiKey(@QueryParameter String value) {
            return ValidationParameters.doCheckApiKey(value);
        }

        public FormValidation doCheckScanDir(@QueryParameter String value) {
            return ValidationParameters.doCheckScanDir(value);
        }

        public FormValidation doCheckWildcard(@QueryParameter String value) {
            return ValidationParameters.doCheckWildcard(value);
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        public String getDisplayName() {
            return Messages.UploadBuilder_DescriptorImpl_displayName();
        }
    }
}
