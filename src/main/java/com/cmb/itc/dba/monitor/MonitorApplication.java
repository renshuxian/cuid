package com.cmb.itc.dba.monitor;

import com.cmb.itc.dba.schedule.MonitorSchedule;
import com.cmb.itc.dba.util.SpringContextUtil;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.io.IOException;

@SpringBootApplication
@ComponentScan("com.cmb.*")
public class MonitorApplication {

    private Logger log = LoggerFactory.getLogger(MonitorApplication.class);

    //启动时创建scheduler 并启动相关任务
    public MonitorApplication() throws IOException, SchedulerException {
        MonitorSchedule monitorSchedule = new MonitorSchedule();
    }

    public static void main(String[] args) throws IOException, SchedulerException {

        ConfigurableApplicationContext applicationContext = SpringApplication.run(MonitorApplication.class, args);
        SpringContextUtil.setApplicationContext(applicationContext);
    }

}
