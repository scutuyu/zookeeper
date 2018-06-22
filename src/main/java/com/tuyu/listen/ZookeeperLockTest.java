package com.tuyu.listen;

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
public class ZookeeperLockTest {

    public static void main(String[] args) throws InterruptedException {

        /*
        当只有一个线程时，没有监听它创建的临时节点，那么当删除临时节点时，并没有调用process方法
        10.10.70.88_Thread[Thread-0-EventThread,5,main] connect to zk server successfully.
        10.10.70.88_Thread[Thread-0,5,main] get the lock : lock_0000000074
        10.10.70.88_Thread[Thread-0,5,main] 开始执行任务.....
        10.10.70.88_Thread[Thread-0,5,main] 任务执行完成.
        10.10.70.88_Thread[Thread-0,5,main] release lock lock_0000000074
        */

        /*
        当有两个线程时，第二个线程监听第一个线程的临时节点，当第一个线程的临时节点被删除后，第二个线程就会再次去获取锁
        10.10.70.88_Thread[Thread-1-EventThread,5,main] connect to zk server successfully.
        10.10.70.88_Thread[Thread-0-EventThread,5,main] connect to zk server successfully.
        10.10.70.88_Thread[Thread-1,5,main] get the lock : lock_0000000077
        10.10.70.88_Thread[Thread-0,5,main] list [lock_0000000077, lock_0000000078] currNode /locks/lock_0000000078 wait /locks/lock_0000000077 release the lock
        10.10.70.88_Thread[Thread-1,5,main] 开始执行任务.....
        10.10.70.88_Thread[Thread-1,5,main] 任务执行完成.
        10.10.70.88_Thread[Thread-1,5,main] release lock lock_0000000077
        10.10.70.88_Thread[Thread-0-EventThread,5,main] event type NodeDeleted event path /locks/lock_0000000077
        10.10.70.88_Thread[Thread-0-EventThread,5,main] preNode has release the lock lock_0000000077
        10.10.70.88_Thread[Thread-0,5,main] get the lock : lock_0000000078
        10.10.70.88_Thread[Thread-0,5,main] 开始执行任务.....
        10.10.70.88_Thread[Thread-0,5,main] 任务执行完成.
        10.10.70.88_Thread[Thread-0,5,main] release lock lock_0000000078
        */

        /*
        三个线程
        10.10.70.88_Thread[Thread-2-EventThread,5,main] event type None event path null
        10.10.70.88_Thread[Thread-1-EventThread,5,main] event type None event path null
        10.10.70.88_Thread[Thread-1-EventThread,5,main] connect to zk server successfully.
        10.10.70.88_Thread[Thread-0-EventThread,5,main] event type None event path null
        10.10.70.88_Thread[Thread-2-EventThread,5,main] connect to zk server successfully.
        10.10.70.88_Thread[Thread-0-EventThread,5,main] connect to zk server successfully.
        10.10.70.88_Thread[Thread-1,5,main] get the lock : lock_0000000079
        10.10.70.88_Thread[Thread-0,5,main] list [lock_0000000079, lock_0000000080, lock_0000000081] currNode /locks/lock_0000000081 wait /locks/lock_0000000080 release the lock
        10.10.70.88_Thread[Thread-1,5,main] 开始执行任务.....
        10.10.70.88_Thread[Thread-2,5,main] list [lock_0000000079, lock_0000000080, lock_0000000081] currNode /locks/lock_0000000080 wait /locks/lock_0000000079 release the lock
        10.10.70.88_Thread[Thread-1,5,main] 任务执行完成.
        10.10.70.88_Thread[Thread-1,5,main] release lock lock_0000000079
        10.10.70.88_Thread[Thread-2-EventThread,5,main] event type NodeDeleted event path /locks/lock_0000000079
        10.10.70.88_Thread[Thread-2-EventThread,5,main] preNode has release the lock lock_0000000079
        10.10.70.88_Thread[Thread-2,5,main] get the lock : lock_0000000080
        10.10.70.88_Thread[Thread-2,5,main] 开始执行任务.....
        10.10.70.88_Thread[Thread-2,5,main] 任务执行完成.
        10.10.70.88_Thread[Thread-2,5,main] release lock lock_0000000080
        10.10.70.88_Thread[Thread-0-EventThread,5,main] event type NodeDeleted event path /locks/lock_0000000080
        10.10.70.88_Thread[Thread-0-EventThread,5,main] preNode has release the lock lock_0000000080
        10.10.70.88_Thread[Thread-0,5,main] get the lock : lock_0000000081
        10.10.70.88_Thread[Thread-0,5,main] 开始执行任务.....
        10.10.70.88_Thread[Thread-0,5,main] 任务执行完成.
        10.10.70.88_Thread[Thread-0,5,main] release lock lock_0000000081
        */
        int threadNum = 5;
        for (int i = 0; i < threadNum; i++) {
            new Thread(){
                @Override
                public void run() {
                    ZookeeperLock lock = new ZookeeperLock();
                    lock.lock();
                    System.out.println(LogUtil.getMachineInfo() + " 开始执行任务.....");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(LogUtil.getMachineInfo() + " 任务执行完成.");
                    lock.unlock();
                }
            }.start();
        }

        Thread.sleep(5000);
    }
}
