package com.th.basic.echo.shortconnect;

import com.alibaba.fastjson.JSONObject;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;

import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName: NettyClient
 * @Description:客户端启动类 ,客户端与服务端短链接
 * @Author: 唐欢
 * @Date: 2023/6/2 16:41
 * @Version 1.0
 */
public class NettyClient {
    public  static EventLoopGroup group = null ;
    public  static  Bootstrap boostrap = null ;

    static {
        // 客户端启动辅助类
        boostrap = new Bootstrap();

        //开启一个线程组
        group = new NioEventLoopGroup();

        // 设置Socket通道
        boostrap.channel(NioServerSocketChannel.class);
        boostrap.group(group);

        //设置内存分配器
        boostrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
    }

    public static void main(String[] args) {
        try {
            //新建一个promiss对象
            Promise<Response> promise = new DefaultPromise<>(group.next());
            //业务handle
            final  ClientHandler handler = new ClientHandler();
            //把Promise对象赋给handler,用于获取返回服务端的响应结果
            handler.setPromise(promise);

            //把handler对象加入管道中
            boostrap.handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                     ch.pipeline().addLast( new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4));

                     // 把接收的ByteBufe 数据包转换成String
                    ch.pipeline().addLast(new StringDecoder());

                    // 业务逻辑处理Handler
                    ch.pipeline().addLast(handler);
                    ch.pipeline().addLast(new LengthFieldPrepender(4,false));

                    //把字符串转换成byteBuf
                    ch.pipeline().addLast(new StringEncoder(Charset.forName("utf-8")));
                }
            });
            //连接服务器
            ChannelFuture future = boostrap.connect("127.0.0.1",9090).sync();

            // 构建request请求
            RequestFuture requestFuture = new RequestFuture();
            // 设置请求id,此处请求id
            AtomicInteger i = new AtomicInteger();
            requestFuture.setId( i.incrementAndGet());
            // 请求消息内容,
            requestFuture.setRequest("Hello World!");

            //转换成JSON 格式发送给编码器StringEncode
            //StringEncode编码器再发送给LengthFieldPrepender长度编码器,最终写到TCP缓冲中,并传送给客户端
            String requestStr = JSONObject.toJSONString(requestFuture);
            future.channel().writeAndFlush((requestStr));

            // 同步阻塞等待响应结果
            Response response = promise.get();
            //打印最终结果
            System.out.println(JSONObject.toJSONString(response));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}