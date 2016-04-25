package com.xj.zk;

import com.xj.zk.listener.Listener;
import com.xj.zk.lock.SimpleLock;
import org.apache.zookeeper.Watcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.SocketException;

/**
 * Author: xiajun
 * Date: 14/10/21
 */
public class ZkClientTest {
    ZkClient zk = null;

    @Before
    public void init() {
        try {
            zk = new ZkClient("10.13.128.214:2181", 5000, 3000);
        } catch (ZkClientException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void create() throws ZkClientException {
        zk.create("/zk/test/1", "{test:12}".getBytes(), true);
    }

    @Test
    public void listener() throws ZkClientException {
        zk.listenData("/zk/test/1", new Listener() {
            public void listen(String path, Watcher.Event.EventType eventType, byte[] data) throws ZkClientException, SocketException {
                System.out.println(path + "  " + new String(data) + "   " + eventType.name());
            }
        });
    }

    @Test
    public void listenChild() throws ZkClientException {
        zk.listenChild("/zk/test", new Listener() {
            public void listen(String path, Watcher.Event.EventType eventType, byte[] data) throws ZkClientException, SocketException {
                System.out.println(path + " " + eventType.name());
            }
        });
    }

    @Test
    public void listenChildData() {
        zk.listenChildData("/zk/test", new Listener() {
            @Override
            public void listen(String path, Watcher.Event.EventType eventType, byte[] data) throws ZkClientException, SocketException {
                System.out.println(path + "  " + eventType.name() + "   " + new String(data));
            }
        });
    }

    @Test
    public void lock() {
        final SimpleLock lock = zk.getLock("/zk/lock");//创建锁对象
        try {
            if (lock.lock(0)) {//获得锁
                //处理业务
            }
        } finally {
            lock.unlock();//释放锁
        }
        //不在使用时要销毁这个锁
        lock.destroy();
    }

    @After
    public void close() {
        try {
            //需要阻塞是因为zkclient中的线程都是守护线程，当主线程结束时进程退出，所以要阻塞主线程。
            Thread.sleep(2 * 60 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (zk != null) {//在一个进程中应该只创建一个zkclient，当确定以后不会再使用时应该关闭zkclient
            try {
                zk.close();
            } catch (ZkClientException e) {
                e.printStackTrace();
            }
        }
    }
}
