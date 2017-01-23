package com.xj.zk.watcher;

import com.xj.zk.ZkClient;
import com.xj.zk.ZkClientException;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Semaphore;


/**
 * Author: xiajun
 * Date: 14/5/20
 * zookeeper 事件接收类
 */
public class ZkWatcher implements Watcher, AsyncCallback.ChildrenCallback {
    private final static Logger LOGGER = LoggerFactory.getLogger(ZkWatcher.class);
    private WatcherProcess process;
    private ZkClient zkClient;
    private Semaphore connLock;

    public ZkWatcher(Semaphore connLock, ZkClient zkClient) {
        this.connLock = connLock;
        this.zkClient = zkClient;
    }

    public void processResult(int i, String s, Object o, List<String> list) {

    }

    /**
     * 处理事件：会话超时，连接断开，节点变化等
     *
     * @param event
     */
    public void process(WatchedEvent event) {
        switch (event.getState()) {
            case ConnectedReadOnly:
            case SyncConnected:
                if (!zkClient.isConnection()) {
                    zkClient.setIsConnection(true);
                    connLock.release();//连接成功
                    LOGGER.warn("Zookeeper connection or retry success......");
                }
                break;
            case Expired://会话超时
                this.stateChange(event.getState());
                resetSession();
                break;
            case Disconnected://连接断开
                zkClient.setIsConnection(false);
                this.stateChange(event.getState());
                LOGGER.warn("Zookeeper connection break......");
                break;
            default:
                LOGGER.warn("Zookeeper state: " + event.getState());
                break;
        }
        switch (event.getType()) {
            case NodeChildrenChanged: //子节点变化
                this.childChange(event.getPath());
                break;
            case NodeDataChanged: //节点数据变化
                this.dataChange(event.getPath());
        }

    }

    /**
     * 重置会话信息
     */
    private void resetSession() {
        LOGGER.warn("Zookeeper session timeout......");
        try {
            zkClient.reconnection();
            LOGGER.warn("Zookeeper session timeout,retry success. ");
        } catch (ZkClientException e) {
            LOGGER.error("Zookeeper reset session faiil.", e);
        }
    }

    /**
     * 数据变化处理
     *
     * @param path
     */
    private void dataChange(String path) {
        try {
            process.dataChange(path);
        } catch (ZkClientException e) {
            LOGGER.error("Data change watcher exception.", e);
        }
    }

    /**
     * 子节点发生变化
     *
     * @param path
     */
    private void childChange(String path) {
        try {
            process.childChange(path, false);
        } catch (ZkClientException e) {
            LOGGER.error("Child change watcher exception.", e);
        }
    }

    /**
     * 状态变化监听
     *
     * @param state
     */
    private void stateChange(Watcher.Event.KeeperState state) {
        process.listen(state);
    }

    /**
     * 添加zookeeper事件处理类
     *
     * @param process
     */
    public void setWatcherProcess(WatcherProcess process) {
        this.process = process;
    }
}

