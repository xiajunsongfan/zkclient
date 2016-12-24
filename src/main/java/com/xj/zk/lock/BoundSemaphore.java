/*
 * Copyright (c) 2016. mogujie
 */

package com.xj.zk.lock;

import java.util.concurrent.Semaphore;

/**
 * Author: baichuan - xiajun
 * Date: 2016/12/24 19:37
 */
public class BoundSemaphore {
    private Thread thread;
    private Semaphore semaphore;

    public BoundSemaphore(Thread thread, Semaphore semaphore) {
        this.thread = thread;
        this.semaphore = semaphore;
    }

    public Thread getThread() {
        return thread;
    }

    public Semaphore getSemaphore() {
        return semaphore;
    }
}
