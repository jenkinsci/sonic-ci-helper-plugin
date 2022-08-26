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

import hudson.*;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import io.jenkins.plugins.sonic.bean.HttpResult;
import io.jenkins.plugins.sonic.bean.ParamBean;
import io.jenkins.plugins.sonic.bean.Project;
import io.jenkins.plugins.sonic.utils.HttpUtils;
import io.jenkins.plugins.sonic.utils.Logging;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.jetbrains.annotations.NotNull;
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
 * @author yaming116, Eason
 */
public class UploadBuilder extends Builder implements SimpleBuildStep {
    private static final Logger LOGGER = Logger.getLogger(UploadBuilder.class.getName());
    private final Secret apiKey;
    private final String scanDir;
    private String suiteId;
    private String projectId;

    @DataBoundConstructor
    public UploadBuilder(String apiKey, String scanDir) {
        this.apiKey = Secret.fromString(apiKey);
        this.scanDir = scanDir;
    }

    @DataBoundSetter
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    @DataBoundSetter
    public void setSuiteId(String suiteId) {
        this.suiteId = suiteId;
    }

    public Secret getApiKey() {
        return apiKey;
    }

    public String getScanDir() {
        return scanDir;
    }

    public String getSuiteId() {
        return suiteId;
    }

    public String getProjectId() {
        return projectId;
    }

    @Override
    public void perform(@NotNull Run<?, ?> run, @NotNull FilePath workspace, @NotNull EnvVars env, @NotNull Launcher launcher, @NotNull TaskListener listener) throws InterruptedException, IOException {
        boolean status = build(run, workspace, launcher, listener);
        run.setResult(status ? Result.SUCCESS : Result.FAILURE);
        if (!status) throw new InterruptedException("upload fail");
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {

        return build(build, build.getWorkspace(), launcher, listener);
    }

    private boolean build(@NotNull Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener) throws IOException, InterruptedException {

        if (workspace == null) {
            throw new AbortException("no workspace for " + build);
        }

        String host = SonicGlobalConfiguration.get().getHost();

        ParamBean paramBean = new ParamBean();
        paramBean.setWorkspace(workspace);
        paramBean.setHost(host);
        paramBean.setApiKey(this.apiKey);
        paramBean.setScanDir(this.scanDir);
        paramBean.setSuiteId(this.suiteId);
        paramBean.setProjectId(this.projectId);
        Logging.printHeader(listener);
        boolean status = HttpUtils.uploadAction(build, listener, paramBean);
        Logging.printTail(listener);
        return status;
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

        public FormValidation doCheckProjectId(@QueryParameter Integer value) {
            return ValidationParameters.doCheckProjectId(value);
        }

        public FormValidation doCheckApiKey(@QueryParameter String value) {
            return ValidationParameters.doCheckApiKey(value);
        }

        public FormValidation doCheckScanDir(@QueryParameter String value) {
            return ValidationParameters.doCheckScanDir(value);
        }

        //所有项目
        public ListBoxModel doFillProjectIdItems() {
            ListBoxModel items = new ListBoxModel();
            items.add(Messages._UploadBuilder_DescriptorImpl_choose_project().toString(), "");
            try {
                HttpResult<List<Project>> httpResult = HttpUtils.listProject();
                List<Project> list = httpResult == null ? null : httpResult.getData();
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
