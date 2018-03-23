package com.cmb.itc.dba.schedule;

import com.cmb.itc.dba.config.MonitorProperties;
import com.cmb.itc.dba.entity.DBUID;
import com.cmb.itc.dba.cache.Cache;
import com.cmb.itc.dba.dao.AgentDao;
import com.cmb.itc.dba.entity.Agent;
import com.cmb.itc.dba.entity.Constant;
import com.cmb.itc.dba.entity.DbInfo;
import com.cmb.itc.dba.service.AgentService;
import com.cmb.itc.dba.util.SpringContextUtil;
import com.fasterxml.jackson.databind.annotation.JsonAppend;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 扫描结果表每个agent的最新一条数据 和当前时间做对比 超过阈值认为有问题触发分配
 */
public class ScanLastAgent implements Job{

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AgentDao agentDao;

    @Autowired
    private AgentService agentService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        List<Agent> agentList = new ArrayList<>();
        if(Cache.AGENTCACHE.size() > 0){
            for(Map.Entry<String,Agent> entry: Cache.AGENTCACHE.entrySet()){
                agentList.add(entry.getValue());
            }
            checkLastAgentInfo(agentList);

        }else{
            agentList = agentDao.findAllAgentInfo();
            checkLastAgentInfo(agentList);
        }

    }

    private void checkLastAgentInfo(List<Agent> agents) {
        if(agents == null || agents.size() == 0){
            log.error("agents is null check db");
            return;
        }
        List<Agent> errorAgentList = new ArrayList<>();
        for(Agent agent : agents){
            DBUID dbuid = agentDao.selectLastAgentInfo(agent.getName());
            //查不到最后一条记录并且 健康的agent缓存有他才会处理
            if(dbuid == null && Cache.HEALTHAGENTS.containsKey(agent.getName())){
                //说明agent不在工作或者已经死了
                errorAgentList.add(agent);
            }else {
                if(dbuid.getLastUpdateTime() == null){
                    errorAgentList.add(agent);
                    //认为他坏了
                }else{
                    long now = System.currentTimeMillis();
                    long lastUpdateTime = dbuid.getLastUpdateTime().getTime();
                    MonitorProperties monitorConfig = SpringContextUtil.getApplicationContext().getBean(MonitorProperties.class);
                    long expireTime = monitorConfig.getExpireTime();
                    if(now - lastUpdateTime >= expireTime && Cache.HEALTHAGENTS.containsKey(agent.getName())){
                        //超过设定的时间认为坏了
                        errorAgentList.add(agent);
                    }
                }
            }
        }
        if(errorAgentList.size() > 0){
            Map<String, List<DbInfo>> agentManageDbMap = agentService.makeDbBelongAgentMap(errorAgentList);
            agentService.writeZkPath(agentManageDbMap);
        }
    }
}
