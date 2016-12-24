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
                System.out.println("1---------start");
                lock.lock();
                System.out.println("1---------getLock");
                try {
                    Thread.sleep(2000);
                    lock.lock();
                    System.out.println("1---------re getLock");
                    Thread.sleep(3000);
                    lock.unlock();
                    System.out.println("1-2--------unlock");
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("1-1--------unlock");
                lock.unlock();
            }
        }).start();
        Thread.sleep(1000);
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("2----------start");
                boolean s = lock.lock();
                System.out.println("2----------getLock");
                try {
                    Thread.sleep(10000);
                    System.out.println("2---------unlock");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                lock.unlock();
            }
        }).start();
        Thread.sleep(100000);
    }
}
