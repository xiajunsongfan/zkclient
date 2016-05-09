package com.xj.zk.lock;

/**
 * Author: baichuan - xiajun
 * Date: 16/04/30 07:16
 */

public interface Lock {
    /**
     * 获得锁
     * @param timeout
     * @return
     */
    boolean lock(long timeout);

    /**
     * 释放锁
     */
    void unlock();

    /**
     * 销毁锁
     */
    void destroy();
}
