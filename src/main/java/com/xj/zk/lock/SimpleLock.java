package com.xj.zk.lock;

import com.xj.zk.ZkClient;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 该锁为进程间的锁,该对象线程安全支持多线程调用
 * Author: xiajun
 * Date: 16/04/17 12:57
 */
public class SimpleLock implements Lock {
    private final static Logger LOGGER = LoggerFactory.getLogger(SimpleLock.class);
    private final ThreadLocal<ReentrantState> currentLock = new ThreadLocal<ReentrantState>();
    private ZkClient client;
    private String lockPath = "/zk/lock/";
    private String seqPath;
    private LockListener lockListener;
    private static SimpleLock simpleLock = new SimpleLock();
    private volatile boolean isInit = false;

    private SimpleLock() {
    }

    /**
     * 获取锁实例
     *
     * @return
     */
    public static SimpleLock getInstance() {
        return simpleLock;
    }

    /**
     * 初始化
     *
     * @param client
     * @param path
     */
    public synchronized void init(ZkClient client, String path) {
        if (isInit) {
            LOGGER.warn("Repeat init simpleLock.");
            return;
        }
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
        List<String> nodes = client.getChild(this.lockPath, false);
        lockListener = new LockListener(nodes);
        client.listenChild(this.lockPath, lockListener);
        isInit = true;
    }

    /**
     * 获得锁，一直等待，该锁不可重入
     *
     * @return
     */
    public boolean lock() {
        return this.lock(0);
    }

    /**
     * 获得锁，该锁可重入
     *
     * @param timeout 0 或者 大于0的 毫秒数，当设置为0时，程序将一直等待，直到获取到锁，
     *                当设置大于0时，等待获得锁的最长时间为timeout的值
     * @return 是否获取到锁，当超时时返回false
     */
    public boolean lock(long timeout) {
        ReentrantState state = currentLock.get();
        if (state == null) {
            final Semaphore lockObj = new Semaphore(0);
            String newPath = this.client.create(this.seqPath, "".getBytes(), CreateMode.EPHEMERAL_SEQUENTIAL);
            state = new ReentrantState(newPath);
            currentLock.set(state);
            String[] paths = newPath.split("/");
            final String seq = paths[paths.length - 1];
            lockListener.addQueue(seq, lockObj);
            boolean islock = false;
            try {
                if (timeout >= 1) {
                    islock = lockObj.tryAcquire(timeout, TimeUnit.MILLISECONDS);
                } else {
                    lockObj.acquire();
                    islock = true;
                }
                return islock;
            } catch (InterruptedException e) {
                LOGGER.error("Lock exception", e);
            } finally {
                if (!islock) {
                    this.unlock();
                }
            }
        } else {
            state.add();
        }
        return false;
    }

    /**
     * 释放锁
     */
    public void unlock() {
        ReentrantState state = currentLock.get();
        if (state != null) {
            int count = state.decrementAndGet();
            if (count < 1) {
                client.delete(state.getLockPath());
                currentLock.remove();
            }
        }
    }

    /**
     * 销毁锁
     */
    public void destroy() {
        client.unlintenChild(this.lockPath);
    }
}
