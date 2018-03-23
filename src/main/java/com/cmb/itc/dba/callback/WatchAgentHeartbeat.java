package com.cmb.itc.dba.callback;

import com.cmb.itc.dba.cache.Cache;
import com.cmb.itc.dba.dao.AgentDao;
import com.cmb.itc.dba.entity.Agent;
import com.cmb.itc.dba.entity.Constant;
import com.cmb.itc.dba.entity.DbInfo;
import com.cmb.itc.dba.service.AgentService;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WatchAgentHeartbeat implements PathChildrenCacheListener {
    private Logger log = LoggerFactory.getLogger(WatchAgentHeartbeat.class);
    @Autowired
    private AgentService agentService;

    @Autowired
    private AgentDao agentDao;

    private String agentName;

    private String agentGroupName;

    private String lDbId;

    String listenPath = "";

    public WatchAgentHeartbeat(String agentName, String agentGroupName, String lDbId) {
        this.agentName = agentName;
        this.agentGroupName = agentGroupName;
        this.lDbId = lDbId;
        this.listenPath = Constant.HEARTBEAT_PREFIX + agentGroupName + Constant.VIRGULE + agentName;

    }

    @Override
    public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) throws Exception {
        ChildData data = event.getData();
        switch (event.getType()) {
            case CHILD_REMOVED:
                //从缓存中移除
                Cache.HEALTHAGENTS.remove(agentName);
                distributeDb(data);
                log.info("node remove, path={}, data={}", data.getPath(), data.getData());
                break;
            default:
                break;
        }
    }

    private void distributeDb(ChildData data) {
        if (Constant.MASTERPATH.equals(data.getPath())) {
            //缓存中有才继续处理否则可能被别的先处理了
            if (Cache.HEALTHAGENTS.containsKey(agentName)) {
                List<Agent> errorAgentList = new ArrayList<>();
                DbInfo dbInfo = null;
                //从缓存拿
                if (Cache.AGENTCACHE.containsKey(agentName)) {
                    errorAgentList.add(Cache.AGENTCACHE.get(agentName));
                } else {
                    Agent errorAgent = agentDao.findAgentInfoByName(agentName);
                    if (errorAgent != null) {
                        errorAgentList.add(Cache.AGENTCACHE.get(agentName));
                    }
                }
                //agent挂掉
                Map<String, List<DbInfo>> agentManageDbMap = agentService.makeDbBelongAgentMap(errorAgentList);
                if (agentManageDbMap.size() > 0) {
                    //根据map 建立zk相应的addjob任务
                    agentService.writeZkPath(agentManageDbMap);
                }
            }
        }

    }
}
