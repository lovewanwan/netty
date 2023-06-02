package com.th.basic.echo.shortconnect;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @ClassName: RequestFuture
 * @Description: 模拟客户端请求类，主要用于构建请求对象（拥有每次的请求id，并对每次请求对象进行了缓存），最核心的部分在于它的同步等待和结果通知方法。
 * @Author: 唐欢
 * @Date: 2023/6/2 15:42
 * @Version 1.0
 */
public class RequestFuture {

    // 请求缓存类,key为每次请求id,value为请求对象
    public  static Map<Long,RequestFuture> futureMap = new ConcurrentHashMap<Long,RequestFuture>();

    // 对于每个请求id,可以设置原子性增长
    private  long id;
    //请求参数
    private  Object  request;
    // 响应结果
    private  Object result;
    // 超时时间默认5s.
    private  long timeout = 5000;
    //把请求放入缓存中
    public  static void addFuture(RequestFuture future){
        futureMap.put(future.getId(),future);
    }

    //同步获取响应结果
    public  Object get()   {
        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        while (this.result  == null){
            lock.lock();
            try {
                // 主线程默认等待5s,然后查看是否获取到结果
                condition.wait();;
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            finally {
                lock.unlock();
            }
        }
        return  result;
    }

    // 异步线程将结果返回主线程
    public  static void received(Response response){
        RequestFuture future = futureMap.remove(response.getId());
        //设置响应结果
        if (future != null){
            future.setRequest(response.getResult());
        }

        //通知主线程
        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        lock.lock();
        try {
            condition.signal();
        }finally {
            lock.unlock();
        }
    }

    public static Map<Long, RequestFuture> getFutureMap() {
        return futureMap;
    }

    public static void setFutureMap(Map<Long, RequestFuture> futureMap) {
        RequestFuture.futureMap = futureMap;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Object getRequest() {
        return request;
    }

    public void setRequest(Object request) {
        this.request = request;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}