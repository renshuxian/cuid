package com.cmb.itc.dba.cache;

import com.cmb.itc.dba.dao.AgentDao;
import com.cmb.itc.dba.entity.Agent;
import com.cmb.itc.dba.entity.Constant;
import com.cmb.itc.dba.entity.DbInfo;
import com.cmb.itc.dba.util.SpringContextUtil;
import com.cmb.itc.dba.util.ZkUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Cache implements Serializable {


    private static Logger log = LoggerFactory.getLogger(Cache.class);

    private static final long serialVersionUID = -625650103214000442L;


    //key dbname|ldbid
    public static Map<String, DbInfo> DBCACHE = new ConcurrentHashMap<>();
    //key agentName
    public static Map<String, Agent> AGENTCACHE = new ConcurrentHashMap<>();

    //agent group
    public static Map<String, String> AGENTTOGROUP = new ConcurrentHashMap<>();

    //groupToAgent
    public static Map<String, List<Agent>> GROUPTOAGENT = new ConcurrentHashMap<>();

    //health agent
    //TODO  命名
    public static Map<String, Agent> HEALTHAGENTS = new ConcurrentHashMap<>();

    //sendWithoutRetrun 发送了addjob  但是无响应的 认为agent也挂掉 还是要重新分配

    public static Map<String,Agent> ADDJOBWITHOUTRETURN = new ConcurrentHashMap<>();

    public static Map<String,Agent> REMOVEJOBWITHOUTRETURN = new ConcurrentHashMap<>();
    //agent处理过哪些db

    public static Map<String, String> AGENTPROBEDDBS = new ConcurrentHashMap<>();


    public static void setAgentCache(List<Agent> agents) {
        if (agents == null || agents.size() == 0) {
            log.error("refresh cache error agentInfo is null");
            return;
        }

        //刷新agent缓存
        CuratorFramework zkClient = ZkUtil.getZkClient();
        for (Agent agent : agents) {
            AGENTCACHE.put(agent.getName(), agent);
            //刷新agentgroup缓存
            AGENTTOGROUP.put(agent.getName(), agent.getGroup());
            //刷新groupagent缓存
            if (GROUPTOAGENT.containsKey(agent.getGroup())) {
                List<Agent> agentsList = GROUPTOAGENT.get(agent.getGroup());
                agentsList.add(agent);
                GROUPTOAGENT.put(agent.getGroup(), agentsList);
            } else {
                List<Agent> agentList = new ArrayList<>();
                agentList.add(agent);
                GROUPTOAGENT.put(agent.getGroup(), agentList);
            }
            Stat stat = null;
            try {
                stat = zkClient.checkExists().forPath(Constant.HEARTBEAT_PREFIX + agent.getName());
            } catch (Exception e) {
                log.error("check exist for agent error and agent name is " + agent.getName());
                continue;
            }
            if (stat != null) {
                HEALTHAGENTS.put(agent.getName(), agent);
            }
        }
        ZkUtil.closeZkClient(zkClient);
    }

    public static void setDbInfo(List<DbInfo> dbInfos) {
        if (dbInfos == null || dbInfos.size() == 0) {
            log.error("refresh cache error dbInfo is null");
            return;
        }
        for (DbInfo dbInfo : dbInfos) {
            DBCACHE.put(dbInfo.getDbName() + Constant.DBNAME_SPLIT + dbInfo.getlDbId(), dbInfo);
        }
    }


//    public Map getOrAddIt(Map map,String key,String mode){
//        if (map.containsKey(key)){
//            return map;
//        }else{
//            if(Constant.AGENT_MODE.equals(mode)){
//                AgentDao dao = (AgentDao) SpringContextUtil.getApplicationContext().getBean("agentDao");
//                dao.findAgentInfoByName()
//            }
//        }
//    }


}
