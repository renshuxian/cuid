package com.cmb.itc.dba.dao.impl;

import com.cmb.itc.dba.dao.DbDao;
import com.cmb.itc.dba.entity.DbInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DbDaoImpl implements DbDao {
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public List<DbInfo> findAllDbInfo() {
        return jdbcTemplate.query("SELECT * FROM DBI.V_UIDLIST", new Object[]{}, new BeanPropertyRowMapper(DbInfo.class));
    }

    @Override
    public DbInfo findDbInfoByNameAndId(String dbName, String lDbId) {
        List<DbInfo> dbList = jdbcTemplate.query("SELECT * FROM DBI.V_UIDLISTDBI.V_UIDLIST WHERE DBNAME = ? AND LDBID = ?", new Object[]{dbName, lDbId}, new BeanPropertyRowMapper(DbInfo.class));
        if (dbList != null && dbList.size() > 0) {
            DbInfo dbInfo = dbList.get(0);
            return dbInfo;
        } else {
            return null;
        }

    }
}
