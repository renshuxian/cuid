package com.cmb.itc.dba.dao;

import com.cmb.itc.dba.entity.DBUID;
import com.cmb.itc.dba.entity.DbInfo;
import com.cmb.itc.dba.entity.Agent;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentDao {
    /**
     * 找到所有agent的信息
     * @return
     */
    List<Agent> findAllAgentInfo();

    /**
     * 查询agent最后一次更新结果表的信息
     * @param agentName
     * @return
     */
    DBUID selectLastAgentInfo(String agentName);

    /**
     * 通过agentName 找到agent具体信息
     * @param agentName
     * @return
     */
    Agent findAgentInfoByName(String agentName);

    /**
     * 查看该agent最近探测过的db集合
     * @param agentName
     * @return
     */
    List<DbInfo> findAgentProbeDBs(String agentName);


}

