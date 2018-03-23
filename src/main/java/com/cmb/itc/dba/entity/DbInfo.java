package com.cmb.itc.dba.entity;

import java.io.Serializable;

public class DbInfo implements Serializable {
    private static final long serialVersionUID = -684246352929389918L;

    private String advanceApp;

    private String dbName;

    private String lDbId;

    private String group;


    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getlDbId() {
        return lDbId;
    }

    public void setlDbId(String lDbId) {
        this.lDbId = lDbId;
    }

    public String getAdvanceApp() {
        return advanceApp;
    }

    public void setAdvanceApp(String advanceApp) {
        this.advanceApp = advanceApp;
    }
}
