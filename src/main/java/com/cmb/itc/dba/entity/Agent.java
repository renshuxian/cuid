package com.cmb.itc.dba.entity;

import java.io.Serializable;

public class Agent implements Serializable{
    private static final long serialVersionUID = 6006742341653379028L;

    private String name;

    private String advanceApp;

    private String group;

    public Agent (String agentName){
        this.name = agentName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAdvanceApp() {
        return advanceApp;
    }

    public void setAdvanceApp(String advanceApp) {
        this.advanceApp = advanceApp;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }


}
