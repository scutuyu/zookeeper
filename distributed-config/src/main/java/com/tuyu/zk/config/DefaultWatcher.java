package com.tuyu.zk.config;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

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
 * tuyu于6/27/18祈祷...
 * zookeeper默认的监听器
 * @author tuyu
 * @date 6/27/18
 * Stay Hungry, Stay Foolish.
 */
@Component
public class DefaultWatcher implements Watcher {

    private static final Logger logger = LoggerFactory.getLogger(DefaultWatcher.class);

    @Autowired
    private ZooKeeper zooKeeper;

//    public void setZooKeeper(ZooKeeper zooKeeper) {
//        this.zooKeeper = zooKeeper;
//    }

    @Autowired
    private Map myMap;

    @Override
    public void process(WatchedEvent event) {
        if (event == null) {
            logger.info("event is null");
            return;
        }

        Event.KeeperState keeperState = event.getState();
        Event.EventType eventType = event.getType();
        logger.info(" event type " + eventType + " event path " + event.getPath());
        if (keeperState == Event.KeeperState.SyncConnected) {
            if (eventType == Event.EventType.None) {
                logger.info(" connect to zk server successfully.");
            } else if (eventType == Event.EventType.NodeDeleted) {
                logger.info("node {} has been deleted", event.getPath());
            } else if (eventType == Event.EventType.NodeDataChanged) {
                logger.info("node {} data has changed", event.getPath());
                try {
                    zooKeeper.exists(event.getPath(), this); // 从新监听
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                refreshProps(event.getPath());
            }
        } else if (keeperState == Event.KeeperState.Disconnected) {
            logger.info("disconnect with zk server.");
        } else if (keeperState == Event.KeeperState.AuthFailed) {
            logger.info("auth check error.");
        } else if (keeperState == Event.KeeperState.Expired) {
            logger.info("session error");
        }
    }

    private void refreshProps(String path) {
        try {
            byte[] data = zooKeeper.getData(path, this, new Stat());
            String key = path.substring(path.lastIndexOf("/") + 1);
            String value = new String(data);
            myMap.put(key, value);
            System.out.println(myMap);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
