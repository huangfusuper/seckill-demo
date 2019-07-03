package com.thread;

/**
 * 模拟买票  车票服务
 * @author huangfu
 */
public class TicketService implements Runnable{
    private Object obj = new Object();
    /**
     * 车票总数
     */
    private int ticketTotal = 200;

    /**
     * 售票窗口名字
     */
    private String name;

    /**
     * 初始化线程名
     * @param i
     */
    public TicketService(int i) {
        name = "窗口" + i;
    }

    public void run() {
        while (true){
            synchronized(this.obj) {
                if (ticketTotal > 0) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ticketTotal--;
                    System.out.println(name + "卖出去一张票,剩余票数" + ticketTotal);
                } else {
                    System.out.println("不好意思 票没了");
                    break;
                }
            }
        }
    }
}
