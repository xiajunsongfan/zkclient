package com.xj.zk;

import com.xj.zk.lock.Lock;

/**
 * Author: baichuan - xiajun
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
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                lock.unlock();
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                lock.lock();
                System.out.println("---------2");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                lock.unlock();
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                lock.lock();
                System.out.println("---------3");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                lock.unlock();
            }
        }).start();
        Thread.sleep(100000);
    }
}
