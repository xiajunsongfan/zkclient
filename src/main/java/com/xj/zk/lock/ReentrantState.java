package com.xj.zk.lock;

/**
 * Author: baichuan - xiajun
 * Date: 2016/12/24 17:10
 * 锁重入状态
 */
public class ReentrantState {
    private String lockPath;
    private volatile int count;

    public ReentrantState(String lockPath) {
        this.lockPath = lockPath;
        this.count = 1;
    }

    /**
     * 加1
     */
    public void add() {
        this.count++;
    }

    /**
     * 减1 并获得结果
     *
     * @return int
     */
    public int decrementAndGet() {
        this.count--;
        return this.count;
    }

    public String getLockPath() {
        return this.lockPath;
    }
}
