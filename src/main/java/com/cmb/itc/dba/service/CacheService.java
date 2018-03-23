package com.cmb.itc.dba.service;

import com.cmb.itc.dba.config.MonitorProperties;
import com.cmb.itc.dba.schedule.MonitorSchedule;
import com.cmb.itc.dba.schedule.RefreshCache;
import com.cmb.itc.dba.util.SpringContextUtil;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

@Service
public class CacheService {
    private static final Logger log = LoggerFactory.getLogger(CacheService.class);


    /**
     * 刷新自己的缓存
     */
    public void startFlushCache() {
        MonitorProperties config = SpringContextUtil.getApplicationContext().getBean(MonitorProperties.class);
        JobDetail jobDetail = newJob(RefreshCache.class)
                .withIdentity(RefreshCache.class.toString())
                .build();
        log.info("Build Job " + jobDetail.getKey().getName());
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(RefreshCache.class.toString())
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
}
