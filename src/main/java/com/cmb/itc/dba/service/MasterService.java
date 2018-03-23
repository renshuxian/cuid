package com.cmb.itc.dba.service;

import com.cmb.itc.dba.callback.WatchMasterPathRemove;
import com.cmb.itc.dba.entity.Constant;
import com.cmb.itc.dba.util.ZkUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class MasterService {
    private static Logger log = LoggerFactory.getLogger(MasterService.class);

    @Autowired
    private AgentService agentService;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private DbService dbService;


    /**
     * 检查monitor master节点是否创建
     *
     * @return
     */
    public Stat checkMasterPathExist() {
        //master路径结果信息
        Stat hasMaster;
        CuratorFramework zkClient = ZkUtil.getZkClient();
        if (zkClient == null) {
            log.error("createMaster error can 't create zkClient");
            return null;
        }
        try {
            hasMaster = ZkUtil.checkPathExist(Constant.MASTERPATH);
        } catch (Exception e) {
            log.error("checkExist master error", e.toString());
            return null;
        }
        return hasMaster;
    }

    /**
     * 创建master路径 如果创建时已经存在就监听该路径
     */
    public void createMasterPathOrWatchingIt(Stat hasMaster) {
        String resultPath = "";
        CuratorFramework zkClient = ZkUtil.getZkClient();
        if (zkClient != null) {
            //master路径存在
            if (hasMaster == null) {
                try {
                    //创建成功后返回改路径的字符串
                    ZkUtil.createNodeWithMode(CreateMode.EPHEMERAL, Constant.MASTERPATH, Constant.MONITOR.getBytes());
                } catch (KeeperException.NodeExistsException e) {
                    //捕获这个异常证明已经存在，自己备，所以需要监听该主节点
                    log.info("monitor master is exist watch it");
                    watchMasterUtilDead(zkClient, Constant.PARENTPATH);
                    return;
                } catch (Exception e) {
                    log.error("create master path error", resultPath.toString());
                }
                //创建成功返回值和想要创建的路径相同，自己是主
                if (Constant.MASTERPATH.equals(resultPath)) {
                    startMonitorMisson();
                }
            } else {
                watchMasterUtilDead(zkClient, Constant.PARENTPATH);
            }
        }
    }


    /**
     * 启动作为主master要做的任务
     */
    public void startMonitorMisson() {
        //找到agent的信息并分配db
        agentService.findAgentAndDistributeDb();
        //刷新内存任务
        cacheService.startFlushCache();
        //启动扫描agent最新记录任务
        agentService.startScanAgentLastMessage();
        //启动监听agent zk路径任务
        agentService.startAgentHeartbeat();
        //启动多次db探测失败任务
        dbService.startCheckDb();

    }

    /**
     * 监听主master的临时节点直到挂掉
     *
     * @param zkClient
     * @param parentPath
     */
    private void watchMasterUtilDead(CuratorFramework zkClient, String parentPath) {
        ZkUtil.setPathCacheListener(parentPath, false, zkClient, new WatchMasterPathRemove());
    }

    public void createMaster() {
        log.info("start createMaster");
        //判断master是否创建
        Stat hasMaster = checkMasterPathExist();
        //证明路径不存在可以创建路径
        createMasterPathOrWatchingIt(hasMaster);
        log.info("end createMaster");
    }
}
