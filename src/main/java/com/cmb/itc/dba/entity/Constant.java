package com.cmb.itc.dba.entity;

public class Constant {

    public static final String SCHEDULE = "schedule";

    public static final String CONTEXT = "context";

    public static final String MONITOR_NAME = "t_800";

    public static final String DBCUID = "dbCuid";

    public static final String MONITOR = "monitor";

    public static final String VIRGULE = "/";

    public static final String HEARTBEAT = "heartbeat";

    public static final String SPLIT = ",";

    public static final String DBNAME_SPLIT = "|";

    public static final String ADDJOB = "addJob";

    public static final String REMOVEJOB = "removeJob";
    //todo 改到配置
    public static final String HEARTBEAT_PREFIX = "/dbCuid/heartbeat/";

    //parentPath /dbCuid/monitor
    public static final String PARENTPATH = Constant.VIRGULE + Constant.DBCUID + Constant.VIRGULE + Constant.MONITOR;
    //master路径 /dbCuid/monitor/t_800
    public static final String MASTERPATH = PARENTPATH + Constant.VIRGULE + Constant.MONITOR_NAME;

    public static final String AGENT_NAME = "agentName";

    public static final String AGENT_GROUP_NAME = "agentGroupName";

    //public static final String DISTRIBUTE = "distribute";

    public static final String DISTRIBUTE_PREFIX = "/dbCuid/distribute";


    public static final String AGENT_MODE = "agent";

    public static final String DB_MODE = "db";

    public static final String ADD_JOB_PREFIX = "/dbCuid/addJob/";

    public static final String REMOVE_JOB_PREFIX = "dbCuid/removeJob/";







}
