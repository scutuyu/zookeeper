package com.tuyu.lock;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * <pre>
 * ////////////////////////////////////////////////////////////////////
 * //                          _ooOoo_                               //
 * //                         o8888888o                              //
 * //                         88" . "88                              //
 * //                         (| ^_^ |)                              //
 * //                         O\  =  /O                              //
 * //                      ____/`---'\____                           //
 * //                    .'  \\|     |//  `.                         //
 * //                   /  \\|||  :  |||//  \                        //
 * //                  /  _||||| -:- |||||-  \                       //
 * //                  |   | \\\  -  /// |   |                       //
 * //                  | \_|  ''\---/''  |   |                       //
 * //                  \  .-\__  `-`  ___/-. /                       //
 * //                ___`. .'  /--.--\  `. . ___                     //
 * //              ."" '<  `.___\_<|>_/___.'  >'"".                  //
 * //            | | :  `- \`.;`\ _ /`;.`/ - ` : | |                 //
 * //            \  \ `-.   \_ __\ /__ _/   .-` /  /                 //
 * //      ========`-.____`-.___\_____/___.-`____.-'========         //
 * //                           `=---='                              //
 * //      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^        //
 * //             佛祖保佑       永无BUG     永不修改                   //
 * ////////////////////////////////////////////////////////////////////
 * </pre>
 * <p>
 * tuyu于6/21/18祈祷...
 * 分布式锁的实现，基于zookeeper
 * <p>每次加锁时，都需要newZookeeperLock对象</p>
 * @author tuyu
 * @date 6/21/18
 * Stay Hungry, Stay Foolish.
 */
public class ZookeeperLock implements Watcher{

    private ZooKeeper zk;
    private final String hostPort = "127.0.0.1:2181";
    private final int sessionTimeout = 40000; // 设置太小了，对debug调试有影响
    private String currNode;
    private String preNode;
    private String root = "/locks";
    private String lockName = "lock";
    private String splitStr = "_";
    private CountDownLatch latch = null;
    private CountDownLatch conLatch = new CountDownLatch(1);


    public ZookeeperLock() {
        try {
            // 由于实例化ZooKeeper的过程是异步的，需要连接成功之后进行后续的操作，故需要CountDownLatch来做同步
            zk = new ZooKeeper(hostPort, sessionTimeout, this);
            conLatch.await(); // 等待，在监听中打开CountDownLatch
            Stat exists = zk.exists(root, false);
            if (exists == null) {
                String s = zk.create(root, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                System.out.println(LogUtil.getMachineInfo() + " create persistent node : " + s);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

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

    private List<String> sortedNode(List<String> list) {
        List<String> re = new ArrayList<>();
        for (String s : list) {
            if (s.startsWith(lockName + splitStr)) {
                re.add(s);
            }
        }
        Collections.sort(re);
        return  re;
    }

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

    @Override
    public void process(WatchedEvent event) {
        if (event == null) {
            return;
        }

        Event.KeeperState keeperState = event.getState();
        Event.EventType eventType = event.getType();
        System.out.println(LogUtil.getMachineInfo() + " event type " + eventType + " event path " + event.getPath());
        if (keeperState == Event.KeeperState.SyncConnected) {
            if (eventType == Event.EventType.None) {
                System.out.println(LogUtil.getMachineInfo() + " connect to zk server successfully.");
                conLatch.countDown(); // 打开CountDownLatch，让ZookeeperLock构造函数继续运行
            } else if (eventType == Event.EventType.NodeDeleted && event.getPath().equals(preNode)) {
                System.out.println(LogUtil.getMachineInfo() + " preNode has release the lock " + preNode.substring(preNode.lastIndexOf("/") + 1));
                if (latch != null) {
                    latch.countDown();
                }
            }
        } else if (keeperState == Event.KeeperState.Disconnected) {
            System.out.println(LogUtil.getMachineInfo() + " disconnect with zk server.");
        } else if (keeperState == Event.KeeperState.AuthFailed) {
            System.out.println(LogUtil.getMachineInfo() + " auth check error.");
        } else if (keeperState == Event.KeeperState.Expired) {
            System.out.println(LogUtil.getMachineInfo() + " session error");
        }
    }
}
