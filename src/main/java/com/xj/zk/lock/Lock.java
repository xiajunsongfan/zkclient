package com.xj.zk.lock;

/**
 * Author: baichuan - xiajun
 * Date: 16/04/30 07:16
 */

public interface Lock {
    /**
     * 获得锁，该锁不可重入
     *
     * @param timeout 超时时间
     * @return boolean
     */
    boolean lock(long timeout);

    /**
     * 获得锁，该锁不可重入
     *
     * @return boolean
     */
    boolean lock();

    /**
     * 释放锁
     */
    void unlock();

    /**
     * 销毁锁
     */
    void destroy();
}
