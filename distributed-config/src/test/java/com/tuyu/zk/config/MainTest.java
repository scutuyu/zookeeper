package com.tuyu.zk.config;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Iterator;
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
 * tuyu于6/22/18祈祷...
 *
 * @author tuyu
 * @date 6/22/18
 * Stay Hungry, Stay Foolish.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ComponentScan({"com.tuyu.zk.config.BeanConfig"})
@ContextConfiguration({"classpath:applicationContext.xml"})
public class MainTest {

//    @Autowired
//    IAddress address;

//    @Autowired
//    Register register;

    @Value("${rootPath:/dev}")
    private String rootPath;

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    @Autowired
    ZooKeeper zooKeeper;

    @Autowired
    DefaultWatcher defaultWatcher;

    @Autowired
    Map myMap;

    @Test
    public void testAddress() throws InterruptedException, KeeperException {

        // 将配置写到zk中
        Assert.hasText(rootPath, "rootPath must has text");
        Stat stat = new Stat();
        Map<String, Object> zkProps = new HashMap<>();
        for (Object obj : myMap.keySet()) {
            String key = obj.toString();
            String value = myMap.get(key).toString();
            String path = rootPath + "/" + key;
            Stat exists = zooKeeper.exists(path, defaultWatcher); // 设置监听
            if (exists == null) {
                // 更新到zk
                zooKeeper.create(path, value.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            } else {
                byte[] data = zooKeeper.getData(path, defaultWatcher, stat);
                String zkValue = new String(data);
                zkProps.put(key, zkValue); // 从zk获取的值放到zkProps中
            }
        }
        myMap.putAll(zkProps); // 用zkProps更新props
        System.out.println(myMap);
        Thread.sleep(Integer.MAX_VALUE);
    }
}
