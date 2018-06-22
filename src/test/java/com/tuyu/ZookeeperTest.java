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

    /**
     * sessionTimeout小于4000取4000，大于4000取真实值
     * @throws IOException
     */
    @Before
    public void testBefore() throws IOException{
        zooKeeper = new ZooKeeper("127.0.0.1:2181", 4000, this);
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
    public void testAdd() throws KeeperException, InterruptedException {
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
        // 使用实例化zookeeper的watcher来监听
        List<String> children = zooKeeper.getChildren("/hello", true);
        System.out.println(children);
        Thread.currentThread().sleep(10000);
    }

    /**
     * 获取节点数据,如果设置了监听，当节点被删除，更新数据也会触发监听,而新增子节点不会触发监听,删除子节点也不会触发监听
     * @throws KeeperException 如果没有指定的节点，将报KeeperException$NoNodeException异常
     * @throws InterruptedException
     */
    @Test
    public void testGetData() throws KeeperException, InterruptedException {
        Stat stat = new Stat();
        stat.setVersion(1);
        byte[] data = zooKeeper.getData("/hello/tuyu", true, new Stat());
        System.out.println("---> " + new String(data));
        Thread.sleep(10000);
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

    @Override
    public void process(WatchedEvent event) {
        System.out.println("====>");
        doProcess(event);
    }

    private void doProcess(WatchedEvent event) {
        String path = event.getPath();
        Event.KeeperState state = event.getState();
        Event.EventType type = event.getType();
        WatcherEvent wrapper = event.getWrapper();
        System.out.println("path : " + path);
        System.out.println("stat : " + state);
        System.out.println("type : " + type);
        System.out.println("wrapper : " + wrapper);
    }
}
