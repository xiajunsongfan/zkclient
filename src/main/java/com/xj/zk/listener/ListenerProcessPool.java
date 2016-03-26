package com.xj.zk.listener;

import com.xj.zk.watcher.ZkWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 监听器回调处理线程池
 * Author: xiajun
 * Date: 14/5/20
 */
public class ListenerProcessPool {
    private final static Logger LOGGER = LoggerFactory.getLogger(ZkWatcher.class);
    private volatile ThreadPoolExecutor processPool;

    public ListenerProcessPool() {
        this(2);
    }

    public ListenerProcessPool(int listenerPoolSize) {
        processPool = new ThreadPoolExecutor(1, listenerPoolSize, 30000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(100), new ThreadProcessFactory());
    }

    /**
     * 执行监听回调函数
     *
     * @param path    监听的节点
     * @param manager 回调信息
     */
    public void invoker(final String path, final ListenerManager manager) {
        if (manager != null) {
            processPool.submit(new Runnable() {
                public void run() {
                    Listener listener = manager.getListener();
                    if (listener != null) {
                        try {
                            listener.listen(path, manager.getEventType(), manager.getData());
                        } catch (Exception e) {
                            LOGGER.error("Invoker listener callback error.", e);
                        }
                    }
                }
            });
        }
    }
}
