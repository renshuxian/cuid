package com.cmb.itc.dba.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "MonitorProperties")
public class MonitorProperties {


    private String heartbeatPath;

    private String addJob;

    private String removeJob;

    private String distribute;
    //内存刷新时间间隔  s
    private Integer refreshCacheInterval;
    //扫描最后一条agent记录 间隔 s
    private Integer scanLastAgentInterval;

    private long expireTime;

    private String name;

    public String getHeartbeatPath() {
        return heartbeatPath;
    }

    public void setHeartbeatPath(String heartbeatPath) {
        this.heartbeatPath = heartbeatPath;
    }

    public String getAddJob() {
        return addJob;
    }

    public void setAddJob(String addJob) {
        this.addJob = addJob;
    }

    public String getRemoveJob() {
        return removeJob;
    }

    public void setRemoveJob(String removeJob) {
        this.removeJob = removeJob;
    }

    public String getDistribute() {
        return distribute;
    }

    public void setDistribute(String distribute) {
        this.distribute = distribute;
    }

    public Integer getRefreshCacheInterval() {
        return refreshCacheInterval;
    }

    public void setRefreshCacheInterval(Integer refreshCacheInterval) {
        this.refreshCacheInterval = refreshCacheInterval;
    }

    public Integer getScanLastAgentInterval() {
        return scanLastAgentInterval;
    }

    public void setScanLastAgentInterval(Integer scanLastAgentInterval) {
        this.scanLastAgentInterval = scanLastAgentInterval;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
