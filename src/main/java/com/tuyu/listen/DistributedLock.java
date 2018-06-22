package com.tuyu.listen;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

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
 *
 * @author tuyu
 * @date 6/21/18
 * Stay Hungry, Stay Foolish.
 */
public class DistributedLock implements Lock {

    private ZooKeeper zk;
    private String currentNode;
    private String root = "/locks";
    private String splitStr = "_";
    private String previousNode;
    private String lockName;

    public DistributedLock(ZooKeeper zk, String lockName) {
        this.zk = zk;
        this.lockName = lockName;
        try {
            Stat exists = zk.exists(root, false);
            if (exists == null) {
                String s = zk.create(root, "root".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            }
        } catch (Exception e) {
            throw new RuntimeException("create root node error");
        }

    }

    @Override
    public void lock() {
        try {
            // 创建临时有序节点
            String s1 = zk.create(root + "/" + lockName + splitStr, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            // 循环判断创建的节点是否是最小的临时有序节点
            while (true) {
                List<String> list = getSortedChildren(zk);

                if (s1.equals(root + "/" + list.get(0))) {
                    System.out.println(LogUtil.getMachineInfo() + " get the lock");
                    currentNode = s1;
                    System.out.println("currentNode " + currentNode);
                    return;
                } else {
                    synchronized (this) {
                        int currIndex = Collections.binarySearch(list, s1.substring(s1.lastIndexOf("/" ) + 1));
                        previousNode = root + "/" + list.get(currIndex - 1);
                        Stat exists = zk.exists(previousNode, new Watcher() {
                            @Override
                            public void process(WatchedEvent event) {
                                System.out.println("path " + event.getPath() + " type " + event.getType());
                                synchronized (this) {
                                    System.out.println(this);
                                    this.notifyAll();
                                }
                            }
                        });
                        if (exists == null) {
                            return;
                        } else {
                            System.out.println(LogUtil.getMachineInfo() + " has wait the lock " + currentNode);
                            this.wait();
                            System.out.println("wake up");
                        }
                    }
                }
            }

        } catch (KeeperException e) {
            throw new RuntimeException(e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private List<String> getSortedChildren(ZooKeeper zk) throws KeeperException, InterruptedException {
        List<String> children = zk.getChildren(root, false);
        if (children == null || children.size() == 0) {
            throw new RuntimeException("have no child node");
        }
        List<String> list = new ArrayList<>();
        for (String s : children) {
            if (s.startsWith(lockName + splitStr)) {
                list.add(s);
            }
        }
        Collections.sort(list);
        return list;
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock() {
        try {
            String s1 = zk.create(root + splitStr, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

            while (true) {
                List<String> children = zk.getChildren(root, false);
                List<String> list = new ArrayList<>();
                for (String s : children) {
                    if (s.startsWith(root + splitStr)) {
                        list.add(s);
                    }
                }
                Collections.sort(list);
                if (s1.equals(list.get(0))) {
                    System.out.println(LogUtil.getMachineInfo() + " get the lock");
                    return true;
                } else {
                    int currIndex = Collections.binarySearch(list, s1);
                    previousNode = list.get(currIndex - 1);
                    Stat exists = zk.exists(previousNode, new Watcher() {
                        @Override
                        public void process(WatchedEvent event) {
                            synchronized (this) {
                                this.notifyAll();
                            }
                        }
                    });
                    if (exists == null) {
                        return true;
                    } else {
                        synchronized (this) {
                            this.wait();
                        }
                    }
                }
            }

        } catch (KeeperException e) {
            throw new RuntimeException(e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
//        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void unlock() {
        try {
            System.out.println(LogUtil.getMachineInfo() + " has unlocked " + currentNode);
            zk.delete(currentNode, -1);
            synchronized (this) {
                this.notifyAll();
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Condition newCondition() {
        return null;
    }
}
