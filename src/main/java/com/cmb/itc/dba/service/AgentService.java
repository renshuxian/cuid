package com.cmb.itc.dba.service;

import com.alibaba.druid.util.StringUtils;
import com.cmb.itc.dba.cache.Cache;
import com.cmb.itc.dba.callback.WatchAgentHeartbeat;
import com.cmb.itc.dba.config.MonitorProperties;
import com.cmb.itc.dba.dao.AgentDao;
import com.cmb.itc.dba.dao.DbDao;
import com.cmb.itc.dba.entity.Agent;
import com.cmb.itc.dba.entity.Constant;
import com.cmb.itc.dba.entity.DBUID;
import com.cmb.itc.dba.entity.DbInfo;
import com.cmb.itc.dba.schedule.MonitorSchedule;
import com.cmb.itc.dba.schedule.ScanLastAgent;
import com.cmb.itc.dba.util.SpringContextUtil;
import com.cmb.itc.dba.util.ZkUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.security.x509.AVA;

import java.util.*;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

@Service
public class AgentService {

    private Logger log = LoggerFactory.getLogger(AgentService.class);


    @Autowired
    private AgentDao agentDao;

    @Autowired
    private DbDao dbDao;


    /**
     * 扫描agent最后一条入库信息判断状态
     */
    public void startScanAgentLastMessage() {
        MonitorProperties config = SpringContextUtil.getApplicationContext().getBean(MonitorProperties.class);
        JobDetail jobDetail = newJob(ScanLastAgent.class)
                .withIdentity(ScanLastAgent.class.toString())
                .build();
        log.info("Build Job " + jobDetail.getKey().getName());
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(ScanLastAgent.class.toString())
                .startNow()
                .withSchedule(simpleSchedule()
                        .withIntervalInSeconds(config.getRefreshCacheInterval())
                        .repeatForever())
                .build();
        log.info("Build Trigger " + trigger.getKey().getName());
        try {
            MonitorSchedule.scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            log.error(e.getStackTrace().toString());
        }


    }

    /**
     * 监听agent建立的临时节点
     */
    public void startAgentHeartbeat() {
        List<Agent> agentList = new ArrayList<>();
        if (Cache.AGENTCACHE.size() > 0) {
            for (Map.Entry<String, Agent> entry : Cache.AGENTCACHE.entrySet()) {
                agentList.add(entry.getValue());
            }
        } else {
            agentList = agentDao.findAllAgentInfo();
            //添加缓存
            Cache.setAgentCache(agentList);
        }
        WatchAgentHeartbeat agentHeartbeat = null;
        if (agentList != null && agentList.size() > 0) {
            for (Agent agent : agentList) {
                agentHeartbeat = new WatchAgentHeartbeat(agent.getName(), agent.getGroup(), agent.getName());
                String path = Constant.HEARTBEAT_PREFIX + agent.getGroup() + Constant.VIRGULE + agent.getName();
                ZkUtil.setPathCacheListener(path, false, agentHeartbeat);
            }
        }
    }

    //第一次启动或者备机接管主机 查看agent状态 分发db根据配置
    public void findAgentAndDistributeDb() {
        //找到当前是否又心跳不正常的agent
        List<Agent> errorAgentList = findErrorAgentList();
        Map<String, List<DbInfo>> agentManageDbMap;
        //所有agent都正常稳定的工作不用管
        if (errorAgentList.size() == 0) {
            log.info("standby become master and agent all alive");
            return;
        } else {
            //找到有问题的agent探测的db 根据相应规则开始分发 返回agent和db的关系map
            agentManageDbMap = makeDbBelongAgentMap(errorAgentList);
        }
        if (agentManageDbMap.size() > 0) {
            //根据map 建立zk相应的addjob任务
            writeZkPath(agentManageDbMap);
        }
    }


    /**
     * 根据agent和db的关系 在对应路径建立节点
     *
     * @param agentManageDbMap
     */
    public void writeZkPath(Map<String, List<DbInfo>> agentManageDbMap) {
        CuratorFramework zkClient = ZkUtil.getZkClient();
        if (zkClient != null) {
            for (Map.Entry<String, List<DbInfo>> entry : agentManageDbMap.entrySet()) {
                String agentName = entry.getKey();
                List<DbInfo> dbInfos = entry.getValue();
                StringBuffer dbs = new StringBuffer("");
                //TODO 查阅一下是否需要更新还是可以覆盖创建
                for (DbInfo dbInfo : dbInfos) {
                    dbs.append(dbInfo.getDbName() + Constant.SPLIT);
                }
                String agentGroup = Cache.AGENTTOGROUP.get(agentName);
                String path = Constant.DISTRIBUTE_PREFIX + agentGroup + Constant.VIRGULE + agentName;
                //添加进缓存
                Cache.AGENTPROBEDDBS.put(agentName, dbs.toString());
                try {
                    ZkUtil.createNodeWithMode(CreateMode.PERSISTENT, path, dbs.toString().getBytes());
                } catch (Exception e) {
                    log.error("create distributePath error and agentName is {}", agentName);
                    continue;
                }
            }
        }
    }

    /**
     * 找到有问题的agent探测的db 根据规则分发给别的agent
     *
     * @param errorAgentList
     * @return
     */
    public synchronized Map<String, List<DbInfo>> makeDbBelongAgentMap(List<Agent> errorAgentList) {
        Map<String, List<DbInfo>> nextTurnAgentsProbeDbs = new HashMap<>();
        if (errorAgentList == null || errorAgentList.size() == 0) {
            log.info("without error agent ");
            return nextTurnAgentsProbeDbs;
        } else {
            Map<String, List<DbInfo>> errorAgentsProbeDbs = new HashMap<>();
            for (Agent agent : errorAgentList) {
                //找到agent探测的db从缓存 or zk
                List<DbInfo> agentProbedDbs = getAgentProbedDbs(agent);
                if (agentProbedDbs.size() > 0) {
                    errorAgentsProbeDbs.put(agent.getName(), agentProbedDbs);
                }

            }
            nextTurnAgentsProbeDbs = distributeDbsByRule(errorAgentsProbeDbs, nextTurnAgentsProbeDbs);
        }

        return nextTurnAgentsProbeDbs;
    }

    private List<DbInfo> getAgentProbedDbs(Agent agent) {
        List<DbInfo> dbInfoList = new ArrayList<>();
        String dbs;
        if (Cache.AGENTPROBEDDBS.containsKey(agent.getName())) {
            dbs = Cache.AGENTPROBEDDBS.get(agent.getName());
        } else if ((dbs = ZkUtil.getData(Constant.DISTRIBUTE_PREFIX + agent.getGroup() + Constant.VIRGULE + agent.getName())) != null) {
        } else {
            //没拿到 去结果表取 取出最近探测过的那些db
            dbInfoList = agentDao.findAgentProbeDBs(agent.getName());
            List<DbInfo> allDbInfo = dbDao.findAllDbInfo();
            Cache.setDbInfo(allDbInfo);
            return dbInfoList;

        }
        if (!StringUtils.isEmpty(dbs)) {
            String[] dbsArray = dbs.split(Constant.SPLIT);
            for (String dbNameAndId : dbsArray) {
                if (Cache.DBCACHE.containsKey(dbNameAndId)) {
                    dbInfoList.add(Cache.DBCACHE.get(dbNameAndId));
                } else {
                    DbInfo dbInfo = dbDao.findDbInfoByNameAndId(dbNameAndId.split("//|")[0], dbNameAndId.split("//|")[1]);
                    if (dbInfo != null) {
                        dbInfoList.add(dbInfo);
                    }
                    //在添加一次缓存
                    List<DbInfo> allDbInfo = dbDao.findAllDbInfo();
                    Cache.setDbInfo(allDbInfo);
                }
            }
        }
        return dbInfoList;
    }

    /**
     * 根据规则分发有问题agent探测的db
     *
     * @param errorAgentsProbeDbs
     * @param nextTurnAgentsProbeDbs
     */
    private Map<String, List<DbInfo>> distributeDbsByRule(Map<String, List<DbInfo>> errorAgentsProbeDbs, Map<String, List<DbInfo>> nextTurnAgentsProbeDbs) {
        if (errorAgentsProbeDbs.size() > 0) {
            List<DbInfo> spareDbList = new ArrayList<>();
            for (Map.Entry<String, List<DbInfo>> entry : errorAgentsProbeDbs.entrySet()) {
                List<DbInfo> needDistributeDbInfoList = entry.getValue();
                Iterator<DbInfo> iterator = needDistributeDbInfoList.iterator();
                //遍历db
                while (iterator.hasNext()) {
                    DbInfo dbInfo = iterator.next();
                    String advanceApp = dbInfo.getAdvanceApp();
                    //advanceapp配置且现在agent还健康
                    if (!StringUtils.isEmpty(advanceApp) && Cache.HEALTHAGENTS.containsKey(advanceApp) && !Cache.ADDJOBWITHOUTRETURN.containsKey(advanceApp) && !Cache.REMOVEJOBWITHOUTRETURN.containsKey(advanceApp)) {
                        addNextTurnAgentsProbeDbs(nextTurnAgentsProbeDbs, dbInfo, entry.getKey());
                        continue;
                    }
                    String groupName = dbInfo.getGroup();
                    List<Agent> agentList = Cache.GROUPTOAGENT.get(groupName);
                    for (Agent agent : agentList) {
                        //现在还是健康的状态
                        if (Cache.HEALTHAGENTS.containsKey(agent.getName())) {
                            //添加到结果map 跳出循环
                            addNextTurnAgentsProbeDbs(nextTurnAgentsProbeDbs, dbInfo, agent.getName());
                            break;
                        } else {
                            //不是健康的继续查找
                            continue;
                        }
                    }
                    //剩下的db
                    spareDbList.add(dbInfo);

                }
            }
            if (spareDbList.size() > 0 && Cache.HEALTHAGENTS.size() > 0) {
                List<Agent> healthAgentList = new ArrayList<>();
                for (Map.Entry<String, Agent> entry : Cache.HEALTHAGENTS.entrySet()) {
                    healthAgentList.add(entry.getValue());
                }
                int healthSize = healthAgentList.size();
                Random random = new Random();

                for (DbInfo dbInfo : spareDbList) {
                    int position = random.nextInt(healthSize);
                    Agent agent = healthAgentList.get(position);
                    addNextTurnAgentsProbeDbs(nextTurnAgentsProbeDbs, dbInfo, agent.getName());
                }
            }
        }

        return errorAgentsProbeDbs;
    }

    private void addNextTurnAgentsProbeDbs(Map<String, List<DbInfo>> nextTurnAgentsProbeDbs, DbInfo dbInfo, String agentName) {
        if (nextTurnAgentsProbeDbs.containsKey(agentName)) {
            List<DbInfo> dbInfoList = nextTurnAgentsProbeDbs.get(agentName);
            dbInfoList.add(dbInfo);
        } else {
            List<DbInfo> dbInfoList = new ArrayList<>();
            dbInfoList.add(dbInfo);
            nextTurnAgentsProbeDbs.put(agentName, dbInfoList);
        }
    }


    private List<Agent> findErrorAgentList() {
        List<Agent> errorList = new ArrayList<>();
        if (Cache.AGENTCACHE.size() > 0) {
            for (Map.Entry<String, Agent> entry : Cache.AGENTCACHE.entrySet()) {
                String agentName = entry.getKey();
                String agentGroupName = Cache.AGENTTOGROUP.get(agentName);
                Stat stat = null;
                try {
                    stat = ZkUtil.checkPathExist(Constant.HEARTBEAT_PREFIX + agentGroupName + Constant.VIRGULE + agentName);
                } catch (Exception e) {
                    log.error("check agent heartbeat error and agentName is {}", agentName);
                }
                if (stat == null) {
                    //error
                    errorList.add(new Agent(agentName));
                }
            }
        }
        return errorList;
    }


}
