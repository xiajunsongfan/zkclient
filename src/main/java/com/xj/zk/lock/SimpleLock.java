/*
 * Copyright (c) 2010-2016. Mogujie Inc. All Rights Reserved.
 */

package com.xj.zk.lock;

import com.xj.zk.ZkClient;
import org.apache.zookeeper.CreateMode;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 该锁为进程间的锁,该对象线程安全支持多线程调用
 * Author: xiajun
 * Date: 16/04/17 12:57
 */
public class SimpleLock {
    private final ThreadLocal<String> currentLock = new ThreadLocal<String>();
    private ZkClient client;
    private String lockPath = "/zk/lock/";
    private String seqPath;
    private LockListener lockListener;

    public SimpleLock(ZkClient client, String path) {
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
        lockListener = new LockListener(this.lockPath, this.client);
        client.listenChild(this.lockPath, lockListener);
    }

    /**
     * 等待获得锁的时间
     *
     * @param timeout 0 或者 大于0的 毫秒数，当设置为0时，程序将一直等待，直到获取到锁，
     *                当设置大于0时，等待获得锁的最长时间为timeout的值
     * @return 是否获取到锁，当超时时返回false
     */
    public boolean lock(long timeout) {
        final Semaphore lockObj = new Semaphore(0);
        String newPath = this.client.create(this.seqPath, "".getBytes(), CreateMode.EPHEMERAL_SEQUENTIAL);
        currentLock.set(newPath);
        String[] paths = newPath.split("/");
        final String seq = paths[paths.length - 1];
        lockListener.addQueue(seq, lockObj);
        try {
            boolean islock;
            if (timeout >= 1) {
                islock = lockObj.tryAcquire(timeout, TimeUnit.MILLISECONDS);
            } else {
                lockObj.acquire();
                islock = true;
            }
            return islock;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 释放锁
     */
    public void unlock() {
        client.delete(currentLock.get());
    }

    /**
     * 销毁锁
     */
    public void destroy(){
        client.unlintenChild(this.lockPath);
    }
}
