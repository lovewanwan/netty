package com.th.basic.echo.shortconnect;

/**
 * @ClassName: SubThread
 * @Description: 子线程,此线程模拟Netty的异步响应结果
 * @Author: 唐欢
 * @Date: 2023/6/2 16:15
 * @Version 1.0
 */
public class SubThread extends  Thread {
    private  RequestFuture requestFuture;

    public SubThread(RequestFuture requestFuture) {
        this.requestFuture = requestFuture;
    }

    @Override
    public void run() {
        // 模拟额外线程获取响应结果
        Response response = new Response();

        //此处id为请求id,模拟服务器接收到请求后,将请求id直接赋给响应对象id
        response.setId(requestFuture.getId());

        //为响应结果复制
        response.setResult("server response "+Thread.currentThread().getId());

        //子线程sleep 1s
        try {
            Thread.sleep(1000);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        // 将响应结果返回主线程
        RequestFuture.received(response);
    }
}