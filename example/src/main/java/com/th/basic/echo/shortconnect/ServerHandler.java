package com.th.basic.echo.shortconnect;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.json.JsonObjectDecoder;

import java.nio.charset.Charset;

/**
 * @ClassName: ServerHandler
 * @Description: 业务逻辑处理Handler
 * Handler需要读取客户端数据，并对请求进行业务逻辑处理，最终把响应结果返回给客户端。
 * @Author: 唐欢
 * @Date: 2023/6/2 14:39
 * @Version 1.0
 */
public class ServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        //获取客户端发送的请求,并将其转换成RequestFuture对象,
        //由于经过了StringDecoder解码器,所以msg为String类型.
        RequestFuture  requestFuture = JSONObject.parseObject(msg.toString(),RequestFuture.class);


        // 获取请求id
        long id = requestFuture.getId();
        System.out.println("请求信息为==="+msg.toString());

        //构建响应结果
        Response response = new Response();
        response.setId(id);
        response.setResult("服务器响应ok");

        //把响应结果返回客户端
        ctx.channel().writeAndFlush(JSONObject.toJSONString(response));

    }
}