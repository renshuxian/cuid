package com.cmb.itc.dba.controller;

import com.alibaba.druid.util.StringUtils;
import com.cmb.itc.dba.cache.Cache;
import com.cmb.itc.dba.callback.WatchAddJob;
import com.cmb.itc.dba.callback.WatchRemoveJob;
import com.cmb.itc.dba.entity.Constant;
import com.cmb.itc.dba.service.AgentService;
import com.cmb.itc.dba.util.ZkUtil;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class JobController {

    private Logger log = LoggerFactory.getLogger(WatchAddJob.class);


    @RequestMapping(value = "/addJob/{agentName}/{dbName}", method = RequestMethod.POST)
    public void addJob(@PathVariable("agentName") String agentName, @PathVariable("dbName") String dbName) {
        log.info("addJob agentName is " + agentName + " and dbName is" + dbName);
        if(!StringUtils.isEmpty(agentName) && !StringUtils.isEmpty(dbName)){
            if(Cache.HEALTHAGENTS.containsKey(agentName)){
                try {
                    Cache.ADDJOBWITHOUTRETURN.put(agentName,Cache.HEALTHAGENTS.get(agentName));
                    ZkUtil.createNodeWithMode(CreateMode.PERSISTENT,Constant.ADD_JOB_PREFIX + agentName,dbName.getBytes());
                } catch (Exception e) {
                    log.error("from rest addJob error and agentName is {} dbName is {}",agentName,dbName);
                }
            }else{
                log.warn("this agent {} is not health",agentName);
            }
        }
    }


    @RequestMapping(value = "/addJob/{orgAgentName}/{needWorkAgentName}/dbName", method = RequestMethod.POST)
    public void removeJob(@PathVariable("orgAgentName") String orgAgentName, @PathVariable("needWorkAgentName") String needWorkAgentName, @PathVariable("dbName") String dbName) {
        log.info("removeJob orgAgentName is " + orgAgentName + " and needWorkAgentName is" + needWorkAgentName + "and dbName is" ,dbName);
        if(!StringUtils.isEmpty(orgAgentName) && !StringUtils.isEmpty(needWorkAgentName) && !StringUtils.isEmpty(needWorkAgentName)){
            if(Cache.HEALTHAGENTS.containsKey(orgAgentName) && Cache.HEALTHAGENTS.containsKey(needWorkAgentName)){
                try {
                    //建立监听
                    WatchRemoveJob watchRemoveJob = new WatchRemoveJob(orgAgentName, dbName,needWorkAgentName);
                    ZkUtil.setPathCacheListener(Constant.REMOVE_JOB_PREFIX + orgAgentName,false,watchRemoveJob);
                    Cache.REMOVEJOBWITHOUTRETURN.put(orgAgentName,Cache.HEALTHAGENTS.get(orgAgentName));
                } catch (Exception e) {
                    log.error("from rest removeJob error and orgAgentName is {} dbName is {} needWorkAgentName is {}",orgAgentName,dbName,needWorkAgentName);
                }
            }else{
                log.warn("this agent {} {} is not health",orgAgentName,needWorkAgentName);
            }
        }
    }



}
