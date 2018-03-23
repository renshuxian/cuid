package com.cmb.itc.dba.entity;

import java.io.Serializable;
import java.util.Date;

public class DBUID implements Serializable {
    private static final long serialVersionUID = -4706787309053869766L;

    private Date lastUpdateTime;

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
}
