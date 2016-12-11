package com.xj.zk;

import com.xj.zk.lock.Lock;

/**
 * Author: xiajun
 * Date: 2016/12/11 09:51
 */
public class LockTest {
    public static void main(String[] args) throws InterruptedException {
        final ZkClient zk = new ZkClient("127.0.0.1:2181");
        final Lock lock = zk.getLock("/lock1");
        new Thread(new Runnable() {
            @Override
            public void run() {
                lock.lock();
                System.out.println("---------1");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                lock.unlock();
            }
        }).start();
        Thread.sleep(1000);
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean s = lock.lock();
                System.out.println(s+"---------2");
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                lock.unlock();
            }
        }).start();
        Thread.sleep(100000);
    }
}
