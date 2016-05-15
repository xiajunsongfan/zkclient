package com.xj.zk.lock;

import com.xj.zk.ZkClient;
import com.xj.zk.listener.StateListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Author: baichuan - xiajun
 * Date: 16/04/30 07:23
 * <p>
 * 主从服务锁，当主启动成功后从服务处于监听状态
 * 主服务挂掉后，从服务开始运行。
 * </p>
 * <p>该类非线程安全</p>
 */
public class HALock implements Lock {
    private ZkClient client;
    private String lockPath = "/zk/halock/";
    private String seqPath;

    public HALock(ZkClient client, String path) {
        this.client = client;
        if (path != null) {
            if (path.indexOf("/") == 0) {
                this.lockPath = path;
            }
            if (path.lastIndexOf("/") != path.length() - 1) {
                this.seqPath = this.lockPath + "/1";
            } else {
                this.seqPath = this.lockPath + "1";
            }
        }
        if (!client.exists(lockPath)) {
            this.client.create(lockPath, CreateMode.PERSISTENT);
        }
    }

    @Override
    public boolean lock(long timeout) {
        final Semaphore lockObj = new Semaphore(0);
        final HALockListener lockListener = new HALockListener(lockPath, this.client, lockObj);
        lockListener.init(this.seqPath);
        this.client.listenChild(lockPath, lockListener);
        this.client.listenState(Watcher.Event.KeeperState.Expired, new StateListener() {
            @Override
            public void listen(Watcher.Event.KeeperState state) {
                lockListener.expired(seqPath);
            }
        });
        this.client.listenState(Watcher.Event.KeeperState.Disconnected, new StateListener() {
            @Override
            public void listen(Watcher.Event.KeeperState state) {
                lockListener.disconnect();
            }
        });
        try {
            lockObj.acquire();
            return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void unlock() {

    }

    @Override
    public void destroy() {
        this.client.unlistenState(Watcher.Event.KeeperState.Expired);
        this.client.unlistenState(Watcher.Event.KeeperState.Disconnected);
    }
}
