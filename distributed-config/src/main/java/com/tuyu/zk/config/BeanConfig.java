package com.tuyu.zk.config;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
 * tuyu于6/25/18祈祷...
 *
 * @author tuyu
 * @date 6/25/18
 * Stay Hungry, Stay Foolish.
 */
// 配置类需要使用@Configuration注解
@Configuration
// 加载属性配置文件需要使用@propertySource注解
@PropertySource(value = {"classpath:zk.properties"})
public class BeanConfig {

    private static final Logger logger = LoggerFactory.getLogger(BeanConfig.class);

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    @Autowired
    private Map myMap;

    // 定义bean需要使用@Bean注解
    // 获取配置文件中的属性值需要使用@Value("${}")注解，可以设置默认值用冒号分割
    @Bean
    public ZooKeeper zooKeeper(
            @Value("${zk.hostPort:127.0.0.1:2181}") String hostPort,
            @Value("${zk.sessionTimeout:5000}")int sessionTimeout,
            @Value("${rootPath:/dev}")String rootPath)
            throws IOException, InterruptedException, KeeperException {
        ZooKeeper zooKeeper = new ZooKeeper(hostPort, sessionTimeout, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                final Event.KeeperState STATE = event.getState();
                switch (STATE) {
                    case SyncConnected:
                        countDownLatch.countDown();
                        logger.info("成功连接zookeeper服务器");
                        break;
                    case Disconnected:
                        logger.warn("与zookeeper服务器断开连接");
                        break;
                    case Expired:
                        logger.error("session会话失效...");
                        break;
                    default:
                        break;
                }
            }
        });
        countDownLatch.await();
        // 从zk上拉取配置，实例化bean
//        if (StringUtils.isEmpty(paths)) {
//            logger.info("inti zk config fail, the paths param is empty");
//        }
//        String[] pathArr = paths.split(",");
//        for (String path : pathArr) {
//            int lastIndex = path.lastIndexOf("/");
//            int lastTwoIndex = path.substring(0, lastIndex).lastIndexOf("/");
//            String clazzStr = path.substring(lastTwoIndex + 1, lastIndex);
//            String key = path.substring(lastIndex);
//            String value = new String(zooKeeper.getData(path, null, new Stat()));
//
//        }
        // 配置监听
//        zooKeeper.exists("/dev/zk.ip", addressWatcher);
//        zooKeeper.exists("/dev/com.tuyu.zk.config/port", addressWatcher);

        System.out.println(myMap);
        return zooKeeper;
    }
}
