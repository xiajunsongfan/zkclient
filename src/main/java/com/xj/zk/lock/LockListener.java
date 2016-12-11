package com.xj.zk.lock;

import com.xj.zk.ZkClient;
import com.xj.zk.ZkClientException;
import com.xj.zk.listener.Listener;
import org.apache.zookeeper.Watcher;

import java.net.SocketException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

/**
 * Author: baichuan - xiajun
 * Date: 16/04/25 14:49
 */
public class LockListener implements Listener {
    private String lockPath;
    private ZkClient client;
    private Map<String, Semaphore> waitLocks = new ConcurrentHashMap<String, Semaphore>();

    public LockListener(String lockPath, ZkClient client) {
        this.lockPath = lockPath;
        this.client = client;
    }

    @Override
    public void listen(String path, Watcher.Event.EventType eventType, byte[] data) throws ZkClientException, SocketException {
        if (!waitLocks.isEmpty()) {
            List<String> locks = client.getChild(lockPath, false);
            String acqLock = null;
            for (Map.Entry<String, Semaphore> entry : waitLocks.entrySet()) {
                String lock = entry.getKey();
                if (check(lock, locks)) {
                    acqLock = lock;
                    entry.getValue().release();
                    break;
                }
            }
            if (acqLock != null) {
                waitLocks.remove(acqLock);
            }
        }
    }

    /**
     * 添加等待队列
     *
     * @param path      锁节点
     * @param semaphore 信号量对象
     */
    public void addQueue(String path, Semaphore semaphore) {
        waitLocks.put(path, semaphore);
    }

    /**
     * 检查是否可以获得锁
     *
     * @param seq   临时节点值
     * @param locks 所以临时节点
     * @return boolean
     */
    private boolean check(String seq, List<String> locks) {
        boolean isLock = true;
        Long seq_ = Long.parseLong(seq);
        for (String lock : locks) {
            Long lock_ = Long.parseLong(lock);
            if (seq_ > lock_) {
                isLock = false;
                break;
            }
        }
        return isLock;
    }
}
