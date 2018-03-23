package com.cmb.itc.dba.util;

import com.alibaba.druid.util.StringUtils;
import com.cmb.itc.dba.callback.WatchRemoveJob;
import com.cmb.itc.dba.config.ZkProperties;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZkUtil {
    private static final Logger log = LoggerFactory.getLogger(ZkUtil.class);

    /**
     * 返回一个可用的zk连接
     *
     * @return
     */
    public static CuratorFramework getZkClient() {
        CuratorFramework client = null;
        ZkProperties zkProperties = null;
        try {
            zkProperties = SpringContextUtil.getApplicationContext().getBean(ZkProperties.class);
            if (zkProperties != null) {
                RetryPolicy retryPolicy = new ExponentialBackoffRetry(zkProperties.getRetryTime(), zkProperties.getRetry());
                client = CuratorFrameworkFactory.builder()
                        .connectString(zkProperties.getIpAndPort())
                        .sessionTimeoutMs(zkProperties.getTimeout())
                        .retryPolicy(retryPolicy).build();
            }
        } catch (Exception e) {
            log.error("create zkClient error", e.toString());
        }
        if (client != null) {
            client.start();
        }
        return client;
    }

    /**
     * 关闭可用连接
     *
     * @param client
     */
    public static void closeZkClient(CuratorFramework client) {
        if (client != null) {
            client.close();
        }
    }


    /*
     *  设置Path Cache, 监控本节点的子节点被创建,更新或者删除，注意是子节点, 子节点下的子节点不能递归监控
     *  事件类型有3个, 可以根据不同的动作触发不同的动作
     *  本例子只是演示, 所以只是打印了状态改变的信息, 并没有在PathChildrenCacheListener中实现复杂的逻辑
     *  @Param path 监控的节点路径, cacheData 是否缓存data
     *  可重入监听
     * */
    public static void setPathCacheListener(String path, boolean cacheData, PathChildrenCacheListener listen) {
        PathChildrenCache pathChildrenCache = null;
        CuratorFramework client = getZkClient();
        if (client != null) {
            try {
                //如果没有呢
                pathChildrenCache = new PathChildrenCache(client, path, cacheData);
                pathChildrenCache.getListenable().addListener(listen);
                pathChildrenCache.start(StartMode.POST_INITIALIZED_EVENT);
            } catch (Exception e) {
                log.error("PathCache listen error, path=", path);
            }
        }
    }

    public static Stat checkPathExist(String path) throws Exception {
        CuratorFramework zkClient = getZkClient();
        Stat resultPath = null;
        if (zkClient != null) {
            resultPath = zkClient.checkExists().forPath(path);
        } else {
            log.error("zkClient is null");
            return null;
        }
        ZkUtil.closeZkClient(zkClient);
        return resultPath;
    }

    public static String createNodeWithMode(CreateMode createMode, String path, byte[] data) throws Exception, KeeperException.NodeExistsException {
        CuratorFramework zkClient = getZkClient();
        String resultPath = null;
        if (zkClient != null) {
            resultPath = zkClient.create().creatingParentsIfNeeded().withMode(createMode).forPath(path, data);
        } else {
            log.error("zkClient is null");
        }
        return resultPath;

    }

    public static String getData(String path) {
        CuratorFramework zkClient = getZkClient();
        String data = null;
        if (!StringUtils.isEmpty(path) && zkClient != null) {
            try {
                data = new String(zkClient.getData().forPath(path));
            } catch (Exception e) {
                log.error("zkClient get data error and path is {}", path);
                return null;
            }
        }
        return data;
    }


}
