package com.tuyu.zk.config;

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
 * 保存配置的详细信息
 * @author tuyu
 * @date 6/27/18
 * Stay Hungry, Stay Foolish.
 */
public class ConfigBean {

    private String key;
    private String value;
    private Class beanClass;
    private String beanName;
    private Class watcherClass;
    private String zookeeperPath;


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Class getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(Class beanClass) {
        this.beanClass = beanClass;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public Class getWatcherClass() {
        return watcherClass;
    }

    public void setWatcherClass(Class watcherClass) {
        this.watcherClass = watcherClass;
    }

    public String getZookeeperPath() {
        return zookeeperPath;
    }

    public void setZookeeperPath(String zookeeperPath) {
        this.zookeeperPath = zookeeperPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConfigBean that = (ConfigBean) o;

        if (key != null ? !key.equals(that.key) : that.key != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;
        if (beanClass != null ? !beanClass.equals(that.beanClass) : that.beanClass != null) return false;
        if (beanName != null ? !beanName.equals(that.beanName) : that.beanName != null) return false;
        if (watcherClass != null ? !watcherClass.equals(that.watcherClass) : that.watcherClass != null) return false;
        return zookeeperPath != null ? zookeeperPath.equals(that.zookeeperPath) : that.zookeeperPath == null;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (beanClass != null ? beanClass.hashCode() : 0);
        result = 31 * result + (beanName != null ? beanName.hashCode() : 0);
        result = 31 * result + (watcherClass != null ? watcherClass.hashCode() : 0);
        result = 31 * result + (zookeeperPath != null ? zookeeperPath.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ConfigBean{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", beanClass=" + beanClass +
                ", beanName='" + beanName + '\'' +
                ", watcherClass=" + watcherClass +
                ", zookeeperPath='" + zookeeperPath + '\'' +
                '}';
    }
}
