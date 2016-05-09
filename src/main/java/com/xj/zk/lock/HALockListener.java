package com.xj.zk.lock;

import com.xj.zk.ZkClient;
import com.xj.zk.ZkClientException;
import com.xj.zk.listener.Listener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;

import java.net.SocketException;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Author: baichuan - xiajun
 * Date: 16/04/30 07:40
 */
public class HALockListener implements Listener {
    private String lockPath;
    private ZkClient client;
    private long lockPathSeq;
    private Semaphore semaphore;
    private volatile boolean ownLock;

    public HALockListener(String lockPath, ZkClient client, Semaphore semaphore) {
        this.lockPath = lockPath;
        this.client = client;
        this.semaphore = semaphore;
    }

    public void init(String seqPath) {
        String path = this.client.create(seqPath, "".getBytes(), CreateMode.EPHEMERAL_SEQUENTIAL);
        String[] nodes = path.split("/");
        lockPathSeq = Long.parseLong(nodes[nodes.length - 1]);
    }

    /**
     * session过期后重连处理
     *
     * @param seqPath
     */
    public void expired(String seqPath) {
        if (ownLock) {
            this.init(seqPath);
        } else {
            for (int i = 0; i < 5; i++) {
                List<String> locks = client.getChild(lockPath, false);
                if (locks == null || locks.isEmpty()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    break;
                }
            }
            this.init(seqPath);
        }
    }

    /**
     * session过期处理
     */
    public void disconnect() {
        lockPathSeq = Long.MAX_VALUE;
    }

    @Override
    public void listen(String path, Watcher.Event.EventType eventType, byte[] data) throws ZkClientException, SocketException {
        List<String> locks = client.getChild(lockPath, false);
        if (check(lockPathSeq, locks)) {
            ownLock = true;
            semaphore.release();
        }
    }

    private boolean check(long seq, List<String> locks) {
        boolean isLock = true;
        for (String lock : locks) {
            Long lock_ = Long.parseLong(lock);
            if (seq > lock_) {
                isLock = false;
                continue;
            }
        }
        return isLock;
    }
}
