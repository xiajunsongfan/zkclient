/*
 * Copyright (c) 2010-2016. Mogujie Inc. All Rights Reserved.
 */

package com.xj.zk.lock;

import com.xj.zk.ZkClient;
import com.xj.zk.ZkClientException;
import com.xj.zk.listener.Listener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;

import java.net.SocketException;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 *  该锁为进程间的锁，同一个对象不能在多线程中使用，如果要在多个线程中使用可以创建不同的对象来实现
 * Author: baichuan - xiajun
 * Date: 16/04/17 12:57
 */
public class SimpleLock {
    private final Semaphore lockObj = new Semaphore(0);
    private ZkClient client;
    private String lockPath = "/zk/lock/";
    private String currentLock;
    private String seqPath;

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
    }

    /**
     * 等待获得锁的时间
     *
     * @param timeout 0 或者 大于0的 毫秒数，当设置为0时，程序将一直等待，直到获取到锁，
     *                当设置大于0时，等待获得锁的最长时间为timeout的值
     * @return 是否获取到锁，当超时时返回false
     */
    public boolean lock(long timeout) {
        String newPath = this.client.create(this.seqPath, "".getBytes(), CreateMode.EPHEMERAL_SEQUENTIAL);
        this.currentLock = newPath;
        String[] paths = newPath.split("/");
        final String seq = paths[paths.length - 1];
        lockObj.drainPermits();
        client.listenChild(this.lockPath, new Listener() {
            String lock = seq;

            @Override
            public void listen(String path, Watcher.Event.EventType eventType, byte[] data) throws ZkClientException, SocketException {
                List<String> locks = client.getChild(lockPath, false);
                if (check(lock, locks)) {
                    lockObj.release();
                }
            }
        });
        try {
            boolean islock;
            if (timeout > 0) {
                islock = lockObj.tryAcquire(timeout, TimeUnit.MILLISECONDS);
            } else {
                islock = lockObj.tryAcquire();
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
    public void unlock(){
        client.delete(this.currentLock);
    }

    private boolean check(String seq, List<String> locks) {
        boolean isLock = true;
        for (String lock : locks) {
            Long lock_ = Long.parseLong(lock);
            Long seq_ = Long.parseLong(seq);
            if (seq_ > lock_) {
                isLock = false;
                continue;
            }
        }
        return isLock;
    }
}
