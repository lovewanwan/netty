package com.th.basic.echo.shortconnect;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @ClassName: NettyServer
 * @Description: 服务端
 * @Author: 唐欢
 * @Date: 2023/6/2 14:26
 * @Version 1.0
 */
public class NettyServer {
    public static void main(String[] args) {
        /**
         * step1:创建两个线程组，分别为Boss线程组和Worker线程组。
         * Boss线程专门用于接收来自客户端的连接；Worker线程用于处理已经被Boss线程接收的连接。
         *
         *  新建两个线程组,Boss线程组启动一条线程,监听OP_ACCEPT请求,
         *  Worker 线程组默认启动CPU 核数*2的线程,
         *  监听客户端连接的OP_READ 和OP_WRITE事件,处理IO请求
         */
        EventLoopGroup bossGroup =  new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try{
            /**
             * step2:运用服务启动辅助类ServerBootstrap创建一个对象，并配置一系列启动参数，
             * 如参数ChannelOption .SO_RCVBUF和ChannelOption .SO_SNDBUF分别对应接收缓冲区和发送缓冲区的大小。
             */
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup,workerGroup);

            serverBootstrap.channel(NioServerSocketChannel.class);

            /**
             * step3:当Boss线程把接收到的连接注册到Worker线程中后，需要交给连接初始化消息处理Handler链。
             * 由于不同的应用需要用到不同的Handler链，所以Netty提供了ChannelInitializer接口，由用户实现此接口，
             * 完成Handler链的初始化工作。
             */
            serverBootstrap.option(ChannelOption.SO_BACKLOG,128)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            /**
                             * 以前缀为4B的int类型作为长度的解码器;
                             * 第一个参数是包的长度;
                             * 第二个参数是长度值偏移量,由于编码时长度值在最前面,无偏移,所以此处设置为0;
                             * 第三个参数是长度值占用的字节数
                             * 第四个参数是长度值的调节,假设请求包的大小是20B;若长度值不包含本身应该是20B,若长度值包含本身这莺歌是24B,需要调整4个字节
                             * 第五个参数是子啊解析时需要跳过的字节数
                             */
                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4));

                            //把接收到的ByteBuf数据包转换成String;
                            ch.pipeline().addLast(new StringDecoder());

                            //step4:编写业务处理Handler链，并实现对接收客户端消息的处理逻辑。
                            /**
                             * 向worker线程的管道双向链表中添加处理类ServerHandler,
                             * 整个处理流向如下:
                             * HeadContext-channelRead 读数据-->
                             * LengthFieldBasedFrameDecoder-->
                             * StringDecoder-->
                             * ServerHandler-channelRead 读取数据进行业务逻辑判断
                             *
                             * 最后将结果返回客户端-->
                             * TailContext-write->
                             * StringEncoder-->
                             * LengthFieldPrepender -->
                             * HeadContext-write
                             */
                            ch.pipeline().addLast(new ServerHandler());
                            // 在消息体前面新增4个字节的长度值,第一个参数是长度值占用的字节数
                            //第二个参数是长度值的调用,表明是否包含长度值本身
                            ch.pipeline().addLast(new LengthFieldPrepender(4,false));

                            // 把字符串消息转换成ByteBufe
                            ch.pipeline().addLast(new StringEncoder());

                            /**
                             * 注意:
                             *  解码器和编译器的顺序
                             *  两者的执行顺序正好相反,解码器执行顺序从上往下,编码器执行顺序从下往上.
                             */
                        }
                    });
            // step5:同步绑定端口。由于端口绑定需要由Boss线程完成，所以主线程需要执行同步阻塞方法，等待Boss线程完成绑定操作。
            ChannelFuture future = serverBootstrap.bind(9090).sync();

            //阻塞主线程,直到Socker 通道并关闭
            future.channel().closeFuture().sync();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            // 最终关闭线程组
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }


    }
}