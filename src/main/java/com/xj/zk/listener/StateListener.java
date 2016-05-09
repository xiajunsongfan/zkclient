package com.xj.zk.listener;


import org.apache.zookeeper.Watcher.Event.KeeperState;

/**
 * Author: baichuan - xiajun
 * Date: 16/05/04 06:23
 * zooleeper 客户端状态发生变化时的监听类
 * 目前只有2种事件可以被监听。
 * 1. 连接断开
 * 2. 连接重新连上
 */
public interface StateListener {
    void listen(KeeperState state);
}
