package io.jenkins.plugins.sonic;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import io.jenkins.plugins.sonic.bean.HttpResult;
import io.jenkins.plugins.sonic.bean.ParamBean;
import io.jenkins.plugins.sonic.bean.Project;
import io.jenkins.plugins.sonic.utils.HttpUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * upload to jenkins
 *
 * @author yaming116
 */
public class UploadBuilder extends Builder {
    private static final Logger LOGGER = Logger.getLogger(UploadBuilder.class.getName());
    private final Secret apiKey;
    private final String scanDir;
    private final String wildcard;
    private final String updateDescription;

    private final String qrcodePath;

    private String projectId;

    @DataBoundConstructor
    public UploadBuilder(String apiKey, String scanDir, String wildcard,String updateDescription, String qrcodePath) {
        this.apiKey = Secret.fromString(apiKey);
        this.scanDir = scanDir;
        this.wildcard = wildcard;
        this.updateDescription = updateDescription;
        this.qrcodePath = qrcodePath;
    }

    @DataBoundSetter
    public void setProjectId(String projectId) {
        this.projectId = projectId;
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

    public String getProjectId() {
        return projectId;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        String host = SonicGlobalConfiguration.get().getHost();

        ParamBean paramBean = new ParamBean();
        paramBean.setHost(host);
        paramBean.setApiKey(SonicGlobalConfiguration.planApiKey(apiKey));
        paramBean.setScanDir(this.scanDir);
        paramBean.setWildcard(this.wildcard);
        paramBean.setUpdateDescription(this.updateDescription);
        paramBean.setQrcodePath(this.qrcodePath);
        paramBean.setProjectId(this.projectId);
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

        //所有项目
        public ListBoxModel doFillProjectIdItems(@QueryParameter String scanDir, @QueryParameter String wildcard,
                                                 @QueryParameter String apiKey) {
            ListBoxModel items = new ListBoxModel();
            items.add(Messages._UploadBuilder_DescriptorImpl_choose_project().toString(), "");
            try {
                HttpResult<List<Project>> httpResult = HttpUtils.listProject(apiKey);
                List<Project> list = httpResult.getData();
                if (list != null && list.size() > 0) {
                    for (Project c : list) {
                        items.add(c.getProjectName(), String.valueOf(c.getId()));
                    }
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, e.getMessage(), e);
            }
            return items;
        }


        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        public String getDisplayName() {
            return Messages.UploadBuilder_DescriptorImpl_displayName();
        }
    }
}
