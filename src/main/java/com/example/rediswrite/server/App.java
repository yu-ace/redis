package com.example.rediswrite.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class App {

    @Value("${Netty.server.inetHost:127.0.0.1}")
    private String inetHost;

    @Value("${Netty.server.inetPort:6666}")
    private Integer inetPort;

    public static void main(String[] args) throws Exception {
        connection();
    }
    public static void connection() throws Exception{
        //创建两个线程组 boosGroup、workerGroup
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            //创建服务端的启动对象，设置参数
            ServerBootstrap bootstrap = new ServerBootstrap();
            //设置两个线程组boosGroup和workerGroup
            bootstrap.group(bossGroup, workerGroup)
                    //设置服务端通道实现类型
                    .channel(NioServerSocketChannel.class)
                    //设置线程队列得到连接个数
                    .option(ChannelOption.SO_BACKLOG, 128)
                    //设置保持活动连接状态
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    //使用匿名内部类的形式初始化通道对象
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            //给pipeline管道设置处理器
                            socketChannel.pipeline().addLast(new ServerHandler());
                        }
                    });//给workerGroup的EventLoop对应的管道设置处理器
            //System.out.println("java技术爱好者的服务端已经准备就绪...");
            //绑定端口号，启动服务端
            ChannelFuture channelFuture = bootstrap.bind(6666).sync();
            //对关闭通道进行监听
            channelFuture.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

//        switch(command){
//            case "set":
//                recordServer.setKey(strings[1],strings[2]);
//                response = "set ok";
//                break;
//            case "get":
//                response = recordServer.getValue(strings[1]);
//                break;
//            case "list":
//                response = "key一共有"+recordServer.list()+"个";
//                break;
//            case "stat":
//                response = "value一共有"+recordServer.stat()+"个";
//                break;
//            case "delete":
//                response = recordServer.deleteKey(strings[1]);
//                break;
//            default:
//                response = "wrong";
//        }

}
