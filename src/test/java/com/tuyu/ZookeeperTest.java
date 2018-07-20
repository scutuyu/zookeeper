package com.tuyu;

import org.apache.zookeeper.*;
import org.apache.zookeeper.client.ZooKeeperSaslClient;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.proto.WatcherEvent;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
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
 * tuyu于6/20/18祈祷...
 *
 * @author tuyu
 * @date 6/20/18
 * Stay Hungry, Stay Foolish.
 */
public class ZookeeperTest implements Watcher{

    ZooKeeper zooKeeper;
    CountDownLatch latch;

    /**
     * sessionTimeout小于4000取4000，大于4000取真实值
     * @throws IOException
     */
    @Before
    public void testBefore() throws IOException, InterruptedException {
        latch = new CountDownLatch(1);
        zooKeeper = new ZooKeeper("127.0.0.1:2181", 4000, this);
        latch.await();
    }


    /**
     * 创建永久节点[/hello]，没有数据
     * <p>需要传递节点路径，节点存储的数据，访问控制列表（不能为空，否则报空指针异常），</p>
     * <pre>
     *     在zookeeper服务端查看：
     *     -> ./zkCli.sh
     *     -> ls /
     *     [hello]
     *     -> ls /hello
     *     []
     *     -> get /hello
     *   null
         cZxid = 0x47e
         ctime = Wed Jun 20 09:36:15 GMT 2018
         mZxid = 0x47e
         mtime = Wed Jun 20 09:36:15 GMT 2018
         pZxid = 0x47e
         cversion = 0
         dataVersion = 0
         aclVersion = 0
         ephemeralOwner = 0x0
         dataLength = 0
         numChildren = 0
     * </pre>
     * @throws KeeperException
     * @throws InterruptedException
     */
    @Test
    public void testCreate() throws KeeperException, InterruptedException {
        System.out.println();
        String s = zooKeeper.create("/hello", "hello data".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        System.out.println(s);
    }

    /**
     * 查询ACL
     * @throws KeeperException
     * @throws InterruptedException
     */
    @Test
    public void testGetAcl() throws KeeperException, InterruptedException {
        List<ACL> acl = zooKeeper.getACL("/hello", new Stat());
        for (ACL acl1 : acl) {
            System.out.println(acl1);
        }
    }

    /**
     * 获取节点的子节点，并监听，修改节点数据并不触发监听的事件，只有新增子节点，删除子节点才会触发监听
     * <p>
     *     getChildren 监听新增、删除；修改不监听
     *     getData 监听修改、删除
     *     exists 监听新增、删除、修改
     * </p>
     * @throws KeeperException
     * @throws InterruptedException
     */
    @Test
    public void testGetChildren() throws KeeperException, InterruptedException {
        // 使用设置的watcher来监听
//        List<String> children = zooKeeper.getChildren("/hello", new Watcher() {
//            @Override
//            public void process(WatchedEvent event) {
//                System.out.println("--->");
//                doProcess(event);
//            }
//        });
        // 使用实例化zookeeper的watcher来监听,即默认的监听器
        List<String> children = zooKeeper.getChildren("/test", this);
        for (String s : children) {
            System.out.println(s);
        }
        Thread.sleep(Integer.MAX_VALUE);
    }

    /**
     * 获取节点数据,如果设置了监听，当节点被删除，更新数据也会触发监听,而新增子节点不会触发监听,删除子节点也不会触发监听
     * <p>监听也是一次性的</p>
     * @throws KeeperException 如果没有指定的节点，将报KeeperException$NoNodeException异常
     * @throws InterruptedException
     */
    @Test
    public void testGetData() throws KeeperException, InterruptedException {
//        Stat stat = new Stat();
//        stat.setVersion(1);
        byte[] data = zooKeeper.getData("/hello", true, new Stat());
        System.out.println("---> " + new String(data));
        Thread.sleep(Integer.MAX_VALUE);
    }

    /**
     * 获取sessionId
     * 每次都是0
     */
    @Test
    public void testSessionId() {
        long sessionId = zooKeeper.getSessionId();
        System.out.println(sessionId);
    }

    @Test
    public void test() throws KeeperException, InterruptedException {
        System.out.println(zooKeeper.getChildren("/", false));
        System.out.println(zooKeeper.getState());
        System.out.println(zooKeeper.getSessionTimeout());
        ZooKeeperSaslClient saslClient = zooKeeper.getSaslClient();
        System.out.println(saslClient);
        Stat exists = zooKeeper.exists("/hello/tuyu/dd", true); // 如果节点不存在，则返回null，如果节点存在，则返回stat信息
        System.out.println("--> " + exists);
        zooKeeper.addAuthInfo("tuyu", "tuyu".getBytes());
    }

    /**
     * 监听，监听只能使用一次，如果想永远监听，则需要在process方法中重新监听
     * <p>One-time trigger</p>
     */
    @Test
    public void testListen() throws KeeperException, InterruptedException {
        Stat exists = zooKeeper.exists("/hello", this);
        Thread.sleep(Integer.MAX_VALUE);
    }


    @Override
    public void process(WatchedEvent event) {
        System.out.println("====>");
        doProcess(event);
    }

    private void doProcess(WatchedEvent event) {
        String path = event.getPath();
        Event.KeeperState state = event.getState();
        Event.EventType type = event.getType();
        if (state == Event.KeeperState.SyncConnected) {
            switch (type) {
                case None:
                    System.out.println("zk client has connected to zk server");
                    latch.countDown();
                    break;
                case NodeCreated:
                    System.out.println("node " + path + " has been created");
                    break;
                case NodeDataChanged:
                    System.out.println("node " + path + " data has changed to " + getDate(path));
                    break;
                case NodeChildrenChanged:
                    System.out.println("node " + path + " children has changed");
                    break;
                case NodeDeleted:
                    System.out.println("node " + path + " has been deleted");
                    break;
                default:
                    break;
            }
        } else if (state == Event.KeeperState.Disconnected) {
            System.out.println("zk client has disconnected to zk server");
        } else if (state == Event.KeeperState.Expired) {
            System.out.println("session timeout");
        } else if (state == Event.KeeperState.AuthFailed) {
            System.out.println("auth failed");
        }
    }

    private String getDate(String path) {
        try {
            return new String(zooKeeper.getData(path, false, new Stat()));
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "null";
    }

    /**
     * 如果节点被同一个watcher监听了两次以上，会被通知多次吗
     * <p>答案是不,只会被通知一次</p>
     */
    @Test
    public void testTwo() throws KeeperException, InterruptedException {
        String path = "/dev/address.port";
        byte[] data = zooKeeper.getData(path, this, new Stat());
        System.out.println(new String(data));
        zooKeeper.getData(path, this, new Stat());
        Stat exists = zooKeeper.exists(path, this);
        zooKeeper.exists(path, this);
        System.out.println(exists);
        Thread.sleep(Integer.MAX_VALUE);
    }

    /**
     * exist监听不存在的节点时不会报错，而getData监听不存在节点时会报NoNodeException
     * <p>用exist监听的不存在节点，当节点创建时，会得到通知</p>
     */
    @Test
    public void testExsit() throws KeeperException, InterruptedException {
        String path = "/dev";
        Stat exists = zooKeeper.exists(path, this);
        System.out.println(exists);

//        byte[] data = zooKeeper.getData(path, this, new Stat());
//        System.out.println(new String(data));

        Thread.sleep(Integer.MAX_VALUE);
    }

    /**
     * getData的版本控制
     */
    @Test
    public void testGetVersion() throws KeeperException, InterruptedException {
        Stat stat = new Stat();
        stat.setVersion(6);
        byte[] data = zooKeeper.getData("/hello", this, stat);
        System.out.println(" --- > " + new String(data));
    }
}
