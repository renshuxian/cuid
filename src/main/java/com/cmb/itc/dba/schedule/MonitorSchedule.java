package com.cmb.itc.dba.schedule;

import com.cmb.itc.dba.service.MasterService;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * monitor任务调度中心
 */

public class MonitorSchedule {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public static Scheduler scheduler = null;
    @Autowired
    private MasterService masterService;

    public MonitorSchedule() throws IOException, SchedulerException {
        StdSchedulerFactory factory = new StdSchedulerFactory();
        factory.initialize("config/quartz.properties");
        scheduler = factory.getScheduler();
        masterService.createMaster();
    }
}
