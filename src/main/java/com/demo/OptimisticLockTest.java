package com.demo;

import com.demo.utils.RedisUtil;
import redis.clients.jedis.Jedis;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *  Redis乐观锁秒杀实例
 *  大多数是基于数据版本（version）的记录机制实现的。
 *  即为数据增加一个版本标识，在基于数据库表的版本解决方案中，
 *  一般是通过为数据库表增加一个”version”字段来实现读取出数据时，
 *  将此版本号一同读出，之后更新时，对此版本号加1。
 *  此时，将提交数据的版本号与数据库表对应记录的当前版本号进行比对，
 *  如果提交的数据版本号大于数据库当前版本号，则予以更新，否则认为是过期数据。
 *  redis中可以使用watch命令会监视给定的key，
 *  当exec时候如果监视的key从调用watch后发生过变化，
 *  则整个事务会失败。也可以调用watch多次监视多个key。
 *  这样就可以对指定的key加乐观锁了。
 *  注意watch的key是对整个连接有效的，事务也一样。如果连接断开，监视和事务都会被自动清除。
 *  当然了exec，discard，unwatch命令都会清除连接中的所有监视。
 * @author huangfu
 */
public class OptimisticLockTest {
    public static void main(String[] args) throws InterruptedException {
        long starTime=System.currentTimeMillis();

        initProduct();
        initClient();
        printResult();

        long endTime=System.currentTimeMillis();
        long Time=endTime-starTime;
        System.out.println("程序运行时间： "+Time+"ms");

    }

    /**
     * 打印签到商品的用户名单
     */
    public static void printResult(){
        Jedis jedis = RedisUtil.getInstance().getJedis();
        //取所有抢到商品的用户
        Set<String> clientList = jedis.smembers("clientList");
        int i= 1;
        for (String s : clientList) {
            System.out.println("第" + i++ + "个抢到商品，"+s + " ");
        }
        RedisUtil.returnPool(jedis);
    }
    /**
     * 初始化客户端
     */
    public static void initClient(){
        ExecutorService cacheThreadPool = Executors.newCachedThreadPool();
        //模拟客户数目
        int clientNum = 1000;
        for (int i = 0; i < clientNum; i++) {
            cacheThreadPool.execute(new ClientThread(i));
        }
        cacheThreadPool.shutdown();
        //等待线程完毕
        while (true){
            if(cacheThreadPool.isTerminated()){
                System.out.println("所有的线程都结束了");
                break;
            }else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * 初始化商品
     */
    public static void initProduct(){
        //商品个数 100
        int prdNum = 100;
        String key = "prdNum";
        // 抢购到商品的顾客列表
        String clientList = "clientList";
        Jedis jedis = RedisUtil.getInstance().getJedis();
        //存在该键  就删除
        if(jedis.exists(key)){
            jedis.del(key);
        }
        //TODO 待分析
        if (jedis.exists(clientList)) {
            jedis.del(clientList);
        }

        //初始化 商品数目
        jedis.set(key,String.valueOf(prdNum));
        RedisUtil.returnPool(jedis);

    }
}
