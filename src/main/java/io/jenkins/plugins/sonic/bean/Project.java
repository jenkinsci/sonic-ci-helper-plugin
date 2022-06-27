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
package io.jenkins.plugins.sonic.bean;

/**
 * editTime: "2021-12-31 11:39:10"
 * id: 1
 * projectDes: "1"
 * projectImg: ""
 * projectName: "test"
 * robotSecret: ""
 * robotToken: ""
 * robotType: 1
 */
public class Project {
    private Integer id;
    private String projectName;
    private String projectImg;
    private String projectDes;
    private String robotToken;
    private String robotType;

    public Project() {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectImg() {
        return projectImg;
    }

    public void setProjectImg(String projectImg) {
        this.projectImg = projectImg;
    }

    public String getProjectDes() {
        return projectDes;
    }

    public void setProjectDes(String projectDes) {
        this.projectDes = projectDes;
    }

    public String getRobotToken() {
        return robotToken;
    }

    public void setRobotToken(String robotToken) {
        this.robotToken = robotToken;
    }

    public String getRobotType() {
        return robotType;
    }

    public void setRobotType(String robotType) {
        this.robotType = robotType;
    }
}
