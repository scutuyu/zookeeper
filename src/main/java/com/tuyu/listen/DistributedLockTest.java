package com.tuyu.listen;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

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
public class DistributedLockTest {
    public static void main(String[] args) throws IOException {
        final ZooKeeper zk = new ZooKeeper("127.0.0.1:2181", 40000, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                System.out.println("type " + event.getType());
            }
        });
        final DistributedLock lock   = new DistributedLock(zk, "lock");
        for (int i = 0; i < 5; i++) {
            new Thread(){
                @Override
                public void run() {

                    lock.lock();
                    //共享资源
                    System.out.println("睡眠5秒");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(lock != null)
                        lock.unlock();
                }
            }.start();
        }

    }
}
