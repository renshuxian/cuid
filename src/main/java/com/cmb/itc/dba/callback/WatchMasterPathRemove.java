package com.cmb.itc.dba.callback;

import com.cmb.itc.dba.entity.Constant;
import com.cmb.itc.dba.service.MasterService;
import com.cmb.itc.dba.util.ZkUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public class WatchMasterPathRemove implements PathChildrenCacheListener {

    private static Logger log = LoggerFactory.getLogger(WatchMasterPathRemove.class);

    @Autowired
    private MasterService masterService;


    @Override
    public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) throws Exception {
        ChildData data = event.getData();
        switch (event.getType()) {
            case CHILD_REMOVED:
                //加入判断 这里测试是全路径
                if (Constant.MASTERPATH.equals(data.getPath())) {
                    //主机挂掉
                    prepareToMaster();
                }
                log.info("node remove, path={}, data={}", data.getPath(), data.getData());
                break;
            default:
                break;
        }
    }

    /**
     * 监听到原主master挂掉 自己准备接管并启动任务
     */
    private void prepareToMaster() {
        log.info("begin to prepareToMaster");
        CuratorFramework zkClient = ZkUtil.getZkClient();
        if (zkClient != null) {
            try {
                //创建自己是主的节点
                ZkUtil.createNodeWithMode(CreateMode.EPHEMERAL, Constant.MASTERPATH, Constant.MONITOR.getBytes());
            } catch (Exception e) {
                log.error("standBy create master error", e.toString());
                return;
            }
            //启动自己作为主机该处理的任务
            masterService.startMonitorMisson();
        }
    }
}
