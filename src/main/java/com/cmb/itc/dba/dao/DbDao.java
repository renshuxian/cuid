package com.cmb.itc.dba.dao;

import com.cmb.itc.dba.entity.DbInfo;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface  DbDao {

    List<DbInfo> findAllDbInfo();

    DbInfo findDbInfoByNameAndId(String dbName,String lDbId);

}
