package io.jenkins.plugins.sonic.bean;

import hudson.util.Secret;

public class ParamBean {
    private String host;
    private Secret apiKey;
    private String scanDir;
    private String wildcard ;
    private String updateDescription;
    private String qrcodePath;
    private String projectId;

    public ParamBean(){}

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Secret getApiKey() {
        return apiKey;
    }

    public void setApiKey(Secret apiKey) {
        this.apiKey = apiKey;
    }

    public String getScanDir() {
        return scanDir;
    }

    public void setScanDir(String scanDir) {
        this.scanDir = scanDir;
    }

    public String getWildcard() {
        return wildcard;
    }

    public void setWildcard(String wildcard) {
        this.wildcard = wildcard;
    }

    public String getUpdateDescription() {
        return updateDescription;
    }

    public void setUpdateDescription(String updateDescription) {
        this.updateDescription = updateDescription;
    }

    public String getQrcodePath() {
        return qrcodePath;
    }

    public void setQrcodePath(String qrcodePath) {
        this.qrcodePath = qrcodePath;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectId() {
        return projectId;
    }
}
