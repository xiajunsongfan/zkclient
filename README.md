## zkclient 项目
---
###项目介绍：
zkclient 是对zookeeper java客户端进行的封装，主要实现了断线重连，session过期重注册，watch事件改为listen监听事件等


###使用方式：

	<dependency>
		<artifactId>zk-client</artifactId>
    	<groupId>com.xj.zk</groupId>
    	<version>1.0.0-SNAPSHOT</version>
	</dependency>

	1. 创建zkclient实例
	//参数：zookeeper地址，session超时时间（毫秒），连接超时时间（毫秒）
	ZkClient zk = new ZkClient("127.0.0.1:2181", 5000, 3000);

	2. 创建一个临时节点(第3个参数为true)，当session过期所有临时节点被删除时，这种类型的节点会在session重连后自动重新创建
	zk.create("/zk/test/ex", "{name:12}".getBytes(), true);

	3. 监听器的使用，注册监听后只要监听的对象发生变化都会接收到事件回调(断线重连和session过期都不再需要进行重新注册)，
	不会像原生的watch那样只有一次触发。
	3.1. 监听本节点数据变化，参数：节点路径，监听器对象
	zk.listenData("/zk/test/1", new Listener() {
		//发生变化节点的绝对路径，事件类型，变化后的数据
    	public void listen(String path, Watcher.Event.EventType eventType, byte[] data) throws ZkException, SocketException {
    		String msg = new String(data);
		}
    });
    3.2. 监听节点下的孩子子节点变化，注意这个时候listen方法中data是没有数据的
    zk.listenChild("/zk/test", new Listener() {
    	public void listen(String path, Watcher.Event.EventType eventType, byte[] data) throws ZkException, SocketException {
                System.out.println(path + " " + eventType.name());
		}
	});
	3.3. 监听节点下子节点的数据变化, 参数：父节点路径，监听器对象
	   listen方法中的eventType 有2种类型 NodeDeleted节点被删除 和 NodeDataChanged节点数据发生变化
	zk.listenChildData("/zk/test", new Listener() {
		public void listen(String path, Watcher.Event.EventType eventType, byte[] data) throws ZkException, SocketException {
		System.out.println(path + " " + eventType.name() + " " + new String(data));
		}
	});
	4.  其它API的使用基本和zookeeper原生的一样
