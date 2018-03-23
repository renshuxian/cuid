package com.cmb.itc.dba.callback;

import com.cmb.itc.dba.cache.Cache;
import com.cmb.itc.dba.entity.Constant;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type.CHILD_REMOVED;

public class WatchAddJob implements PathChildrenCacheListener {
    private Logger log = LoggerFactory.getLogger(WatchAddJob.class);

    private String agentName;

    private String dbNameList;

    private String addJobPath;

    public WatchAddJob(String agentName, String dbNameList) {
        this.agentName = agentName;
        this.dbNameList = dbNameList;
        this.addJobPath = Constant.ADD_JOB_PREFIX + agentName;
    }

    @Override
    public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) throws Exception {
        ChildData data = event.getData();
        switch (event.getType()) {
            case CHILD_REMOVED:
                if(addJobPath.equals(data.getPath())){
                    Cache.ADDJOBWITHOUTRETURN.remove(agentName);
                }
                log.info("node remove, path={}, data={}", data.getPath(), data.getData());
                break;
            default:
                break;
        }
    }
}
