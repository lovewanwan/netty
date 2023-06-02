package com.th.basic.echo.shortconnect;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.Promise;

/**
 * @ClassName: ClientHandler
 * @Description:
 * @Author: 唐欢
 * @Date: 2023/6/2 16:47
 * @Version 1.0
 */
public class ClientHandler  extends ChannelInboundHandlerAdapter {
    private Promise<Response>  promise;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 读取服务端返回的响应结果,并将其转换成Response对象
        //由于经过了StringDecoder解码器,所以msg为String类型
        Response response = JSONObject.parseObject(msg.toString(),Response.class);
        // 设置响应结果并唤醒主线程
        promise.setSuccess(response);
    }

    public Promise<Response> getPromise() {
        return promise;
    }

    public void setPromise(Promise<Response> promise) {
        this.promise = promise;
    }
}