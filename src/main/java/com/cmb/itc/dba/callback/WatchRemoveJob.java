package com.cmb.itc.dba.callback;

import com.cmb.itc.dba.cache.Cache;
import com.cmb.itc.dba.entity.Constant;
import com.cmb.itc.dba.util.ZkUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WatchRemoveJob implements PathChildrenCacheListener {
    private Logger log = LoggerFactory.getLogger(WatchRemoveJob.class);

    private String agentName;

    private String needWorkAgentName;

    private String dbNameList;

    private String removeJobPath;

    public WatchRemoveJob(String agentName, String dbNameList,String needWorkAgentName) {
        this.agentName = agentName;
        this.dbNameList = dbNameList;
        this.removeJobPath = Constant.REMOVEJOB + agentName;
        this.needWorkAgentName = needWorkAgentName;
    }

    @Override
    public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) throws Exception {
        ChildData data = event.getData();
        switch (event.getType()) {
            case CHILD_REMOVED:
                if(removeJobPath.equals(data.getPath())){
                    Cache.REMOVEJOBWITHOUTRETURN.remove(agentName);
                    ZkUtil.createNodeWithMode(CreateMode.PERSISTENT,Constant.ADD_JOB_PREFIX + needWorkAgentName,dbNameList.getBytes());
                }
                log.info("node remove, path={}, data={}", data.getPath(), data.getData());
                break;
            default:
                break;
        }
    }
}
