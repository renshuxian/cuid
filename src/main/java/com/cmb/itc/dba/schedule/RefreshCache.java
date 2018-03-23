package com.cmb.itc.dba.schedule;

import com.cmb.itc.dba.dao.DbDao;
import com.cmb.itc.dba.entity.Constant;
import com.cmb.itc.dba.cache.Cache;
import com.cmb.itc.dba.dao.AgentDao;
import com.cmb.itc.dba.entity.Agent;
import com.cmb.itc.dba.entity.DbInfo;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.List;

/**
 * 定时刷新内存中的数据
 */
public class RefreshCache implements Job {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AgentDao agentDao;
    @Autowired
    private DbDao dbDao;


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        List<Agent> allAgentInfo = agentDao.findAllAgentInfo();
        List<DbInfo> allDbInfo = dbDao.findAllDbInfo();
        Cache.setDbInfo(allDbInfo);
        Cache.setAgentCache(allAgentInfo);
    }
}
