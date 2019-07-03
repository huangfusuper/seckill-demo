package com.demo;

import com.demo.utils.RedisUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;

/**
 * 一般是和事务一起使用，当对某个key进行watch后如果其他的客户端对这个key进行了更改，
 *      那么本次事务会被取消，事务的exec会返回null。jedis.watch(key)都会返回
 * 顾客线程  模拟1000客户同时并发执行
 * @author huangfu
 */
public class ClientThread implements Runnable{
    Jedis jedis = null;
    // 商品主键
    String key = "prdNum";
    // 抢购到商品的顾客列表主键
    String clientList = "clientList";
    String clientName;

    public ClientThread(int num) {
        clientName = "编号=" + num;
    }
    public void run() {
        // 随机睡眠一下
        try {
            Thread.sleep((int)(Math.random()*5000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while (true){
            System.out.println("顾客:" + clientName + "开始抢商品");
            jedis = RedisUtil.getInstance().getJedis();
            try {
                /**
                 * 一般是和事务一起使用，当对某个key进行watch后如果其他的客户端对这个key进行了更改，
                 * 那么本次事务会被取消，事务的exec会返回null。jedis.watch(key)都会返回OK
                 */
                jedis.watch(key);
                //当前商品的个数
                int prdNum = Integer.parseInt(jedis.get(key));
                if(prdNum>0){
                    /**
                     *  创建Redis事务
                     *  事务是保证事务内的所有命令是原子操作，一般配合watch使用，
                     *  事务的执行结果和pipeline一样都是采用异步的方式获取结果，
                     *  multi.exec()提交事务，
                     *  如果执行成功，其返回的结果和pipeline一样是所有命令的返回值，
                     *  如果事务里面有两个命令那么事务的exec返回值会把两个命令的返回值组合在一起返回。
                     *  如果事务被取消返回null。
                     */
                    Transaction multi = jedis.multi();
                    multi.set(key,String.valueOf(prdNum-1));
                    //如果客户端在使用 MULTI 开启了一个事务之后，
                    // 却因为断线而没有成功执行 EXEC ，那么事务中的所有命令都不会被执行。
                    List<Object> exec = multi.exec();
                    //没抢到
                    if(exec==null || exec.isEmpty()){
                        // 可能是watch-key被外部修改，或者是数据操作被驳回
                        System.out.println("悲剧了，顾客:" + clientName + "没有抢到商品");
                    }else{
                        //将这个抢到商品的用户放倒Redis集合里面
                        jedis.sadd(clientList,clientName);
                        System.out.println("好高兴，顾客:" + clientName + "抢到商品");
                        break;
                    }
                }else{
                    System.out.println("悲剧了，库存为0，顾客:" + clientName + "没有抢到商品");
                    break;
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                //Redis Unwatch 命令用于取消 WATCH 命令对所有 key 的监视。
                jedis.unwatch();
                //回收连接
                RedisUtil.returnPool(jedis);
            }

        }
    }
}
