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
