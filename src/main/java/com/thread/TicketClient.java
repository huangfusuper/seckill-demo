package com.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author huangfu
 */
public class TicketClient {
    public static void main(String[] args) {
        ExecutorService cacheExecutorService = Executors.newCachedThreadPool();
        for(int i = 0;i<3;i++){
            cacheExecutorService.execute(new TicketService(i));
        }
        cacheExecutorService.shutdown();
        while (true){
            //所有线程全部结束
            if (cacheExecutorService.isTerminated()) {
                break;
            }else{
                try {
                    Thread.sleep((int)(Math.random()*2000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
