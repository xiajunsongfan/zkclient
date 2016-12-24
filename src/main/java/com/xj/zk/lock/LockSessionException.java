package com.xj.zk.lock;

/**
 * Author: xiajun
 * Date: 2016/12/24 19:47
 * zookeeper session过期后中断锁线程异常
 */
public class LockSessionException extends RuntimeException {
    public LockSessionException(String msg) {
        super(msg);
    }
}
