package com.tuyu.zk.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.test.context.web.WebAppConfiguration;

import java.rmi.registry.Registry;
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
 * tuyu于6/25/18祈祷...
 * 注册spring bean 的类
 * @author tuyu
 * @date 6/25/18
 * Stay Hungry, Stay Foolish.
 */
@Component
@WebAppConfiguration
public class Register implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(Register.class);

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void register(Class clazz, String beanName, Map<String, Object> maps) {
        DefaultListableBeanFactory factory = (DefaultListableBeanFactory) applicationContext;

        if (factory.isBeanNameInUse(beanName)) {
            factory.removeBeanDefinition(beanName);
            logger.info("remove bean {}", beanName);
        }

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
        for (Map.Entry<String, Object> entry : maps.entrySet()) {
            builder.addPropertyValue(entry.getKey(), entry.getValue());
        }
        factory.registerBeanDefinition(beanName, builder.getRawBeanDefinition());
        logger.info("register bean {}, maps : {}", beanName, maps);
    }

    public void printBeanNames() {
//        ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) applicationContext;
//        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) configurableApplicationContext;
//        Iterator<String> iterator = defaultListableBeanFactory.getBeanNamesIterator();
//        while (iterator.hasNext()) {
//            String next = iterator.next();
//            System.out.println("----> " + next);
//        }


        GenericApplicationContext genericApplicationContext = (GenericApplicationContext) applicationContext;
        DefaultListableBeanFactory defaultListableBeanFactory = genericApplicationContext.getDefaultListableBeanFactory();
        Iterator<String> iterator = defaultListableBeanFactory.getBeanNamesIterator();
        while (iterator.hasNext()) {
            System.out.println("---> " + iterator.next());
        }
    }
}
