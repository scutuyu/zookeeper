Distributed Lock
===

使用zookeeper来实现分布式锁

# 原理
监听zookeeper的临时有序节点，监听到NodeDeleted事件，就会让线程重新获取锁

# 测试方法
 ```java
public class ZookeeperLockTest {

    public static void main(String[] args) throws InterruptedException {

        int threadNum = 5;
        for (int i = 0; i < threadNum; i++) {
            new Thread(){
                @Override
                public void run() {
                    ZookeeperLock lock = new ZookeeperLock();
                    lock.lock();
                    System.out.println(LogUtil.getMachineInfo() + " 开始执行任务.....");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(LogUtil.getMachineInfo() + " 任务执行完成.");
                    lock.unlock();
                }
            }.start();
        }

        Thread.sleep(5000);
    }
}
```

# 获取锁
```
    public void lock() {
        try {
            // 1. 创建临时节点
            currNode = zk.create(root + "/" + lockName + splitStr, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            while (true){
                // 2. 获取服务器获取锁的所有临时节点
                List<String> allNodes = zk.getChildren(root, false);
                // 3. 取出最小的临时节点，并与新创建的节点比较
                List<String> sortedNodes = sortedNode(allNodes);
                // 如果相等，则获取锁，并返回
                if (currNode.equals(root + "/" + sortedNodes.get(0))) {
                    System.out.println(LogUtil.getMachineInfo() + " get the lock : " + sortedNodes.get(0));
                    return;
                }else {
                    // 如果不相等，则获取锁失败，并监听前一个临时节点，然后阻塞
                    preNode = root + "/" + sortedNodes.get(Collections.binarySearch(sortedNodes, currNode.substring(currNode.lastIndexOf("/") + 1)) - 1);
                    System.out.println(LogUtil.getMachineInfo() + " list " + sortedNodes + " currNode " + currNode + " wait " + preNode + " release the lock");
                    Stat preData = zk.exists(preNode, this);
                    if (preData == null) {
                        System.out.println(LogUtil.getMachineInfo() + " get lock " + currNode.substring(currNode.lastIndexOf("/") + 1) + " success.");
                        return;
                    }
                    latch = new CountDownLatch(1);
                    latch.await();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

```

# 释放锁
```
    public void unlock() {
            String lock = currNode.substring(currNode.lastIndexOf("/") + 1);
        try {
            System.out.println(LogUtil.getMachineInfo() + " release lock " + lock);
            zk.delete(currNode, -1);
        } catch (Exception e) {
            System.out.println(LogUtil.getMachineInfo() + " release lock " + lock + " error.");
            throw new RuntimeException(e.getMessage());
        }
    }
```

# 问题与不足

- 当sessionTime的值设置太小，在debug调试时就会报错，当sesionTimeout值设置大一点，就好了，目前木有搞明白原因
```
Exception in thread "Thread-1" Exception in thread "Thread-3" java.lang.RuntimeException: KeeperErrorCode = ConnectionLoss for /locks/lock_0000000087
	at com.tuyu.listen.ZookeeperLock.lock(ZookeeperLock.java:102)
	at com.tuyu.listen.ZookeeperLockTest$1.run(ZookeeperLockTest.java:97)
java.lang.RuntimeException: KeeperErrorCode = ConnectionLoss for /locks/lock_0000000087
	at com.tuyu.listen.ZookeeperLock.unlock(ZookeeperLock.java:124)
	at com.tuyu.listen.ZookeeperLockTest$1.run(ZookeeperLockTest.java:105)

```