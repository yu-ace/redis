package com.example.rediswrite.server;

import com.example.rediswrite.model.Command;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

//1. 数据类型支持 String Int List
//2. 命令支持set setnx get incr decr exists del shutdown(退出服务器) stat(返回服务器信息）
//3. shutdown时将内存里面的数据保存到磁盘
//4. 应用启动时读取配置文件，将磁盘上的数据载入到内存中，磁盘没有数据就初始化
//5. 定时重建内存，移除已经删除的数据。定时配置保存到配置文件中
public class ServerHandler extends ChannelInboundHandlerAdapter {
    private static final CommandServer commandServer = CommandServer.getInstance();
    private static final Memory memory = Memory.getInstance();
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        memory.initMap();
        memory.init();
        String result;
        //获取客户端发送过来的消息
        String message = ((ByteBuf) msg).toString(CharsetUtil.UTF_8);
        String[] split = message.split("\n\t");
        for(int i = 0;i < split.length;i++){
            String[] strings = split[i].split(" ");
            String name = strings.length > 0 ? strings[0] : null;
            String key = strings.length > 1 ? strings[1] : null;
            String value = strings.length > 2 ? strings[2] : null;
            Command command = new Command(name,key,value);
            result = commandServer.executeCommand(command);
            System.out.println("收到客户端" + ctx.channel().remoteAddress() + "发送的消息" + result);
        }
        memory.shutDown();
        memory.saveMap();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        //发送消息给客户端
        ctx.writeAndFlush(Unpooled.copiedBuffer("服务端已连接?", CharsetUtil.UTF_8));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //发生异常，关闭通道
        ctx.close();
    }


}
