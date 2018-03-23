package com.cmb.itc.dba.dao.impl;

import com.cmb.itc.dba.dao.AgentDao;
import com.cmb.itc.dba.entity.Agent;
import com.cmb.itc.dba.entity.DBUID;
import com.cmb.itc.dba.entity.DbInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AgentDaoImpl implements AgentDao {
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public List<Agent> findAllAgentInfo() {
        return jdbcTemplate.query("SELECT * FROM DBI.AGENT", new Object[]{}, new BeanPropertyRowMapper(DbInfo.class));
    }

    @Override
    public DBUID selectLastAgentInfo(String agentName) {
        List<DBUID> list = jdbcTemplate.query("SELECT * FROM DBI.DBUID where uidapp = ? fetch first 1 rows only", new Object[]{agentName}, new BeanPropertyRowMapper(DBUID.class));
        if (list != null && list.size() > 0) {
            DBUID dbuid = list.get(0);
            return dbuid;
        } else {
            return null;
        }
    }

    @Override
    public Agent findAgentInfoByName(String agentName) {
        List<Agent> list = jdbcTemplate.query("SELECT * FROM DBI.AGENT where agentName = ? ", new Object[]{agentName}, new BeanPropertyRowMapper(Agent.class));
        if (list != null && list.size() > 0) {
            Agent agent = list.get(0);
            return agent;
        } else {
            return null;
        }
    }

    @Override
    public List<DbInfo> findAgentProbeDBs(String agentName) {
        //TODO 结果表筛选
        return null;
    }
}
