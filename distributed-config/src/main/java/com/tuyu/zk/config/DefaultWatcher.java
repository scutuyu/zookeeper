package com.tuyu.zk.config;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
public class DefaultWatcher implements Watcher, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(DefaultWatcher.class);

    private Map<String, List<String>> beanNameConfigListMap = new ConcurrentHashMap<>(256);

    @Autowired
    private ZooKeeper zooKeeper;


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
            zooKeeper.exists(path, this); // 重新监听
            byte[] data = zooKeeper.getData(path, this, new Stat());
            String key = path.substring(path.lastIndexOf("/") + 1);
            String value = new String(data);
            ConfigBean configBean = (ConfigBean) myMap.get(key);
            configBean.setValue(value);
            myMap.put(key, configBean);
            System.out.println("refresh ----> " + Thread.currentThread() + "   " + myMap);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("after init defaultWatcher =====> " + myMap);
        List<String> list = null;
        for (Object key : myMap.keySet()) {
            ConfigBean configBean = (ConfigBean) myMap.get(key);
            String beanName = configBean.getBeanName();
            if (beanNameConfigListMap.containsKey(beanName)) {
                list = beanNameConfigListMap.get(beanName);
                list.add(key.toString());
            } else {
                list = new ArrayList<>();
                list.add(key.toString());
                beanNameConfigListMap.put(beanName, list);
            }
        }
        System.out.println("inti beanNameConfigListMap  ---->  " + beanNameConfigListMap);
    }
}
