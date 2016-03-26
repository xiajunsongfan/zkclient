package com.xj.zk;

import com.xj.zk.listener.Listener;
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
            zk = new ZkClient("127.0.0.1:2181", 5000, 3000);
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
