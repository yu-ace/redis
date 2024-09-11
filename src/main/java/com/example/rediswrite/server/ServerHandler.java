package com.example.rediswrite.server;

import com.example.rediswrite.Entry;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;


import java.nio.ByteBuffer;

import java.util.*;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    Map<String, List<Integer>> map = new HashMap<>();
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //获取客户端发送过来的消息
        ByteBuf byteBuf = (ByteBuf) msg;
        String message = byteBuf.toString(CharsetUtil.UTF_8);
        String[] split = message.split("\n\t");
        ByteBuffer buffer = ByteBuffer.allocate(8*1024*1024);
        int size;
        int startPosition = 8*1024*1024;
        for(int i = 0;i < split.length;i++){
            String[] strings = split[i].split(" ");
            String command = strings[0];
            String response;

            switch(command){
                case "set":
                    byte[] valueByte = strings[2].getBytes();
                    int valurLength = valueByte.length;
                    buffer.position(0);
                    size = buffer.getInt();
                    int newSize = size + 1;
                    buffer.position(0);
                    buffer.putInt(newSize);
                    int position = 4 + 8 * size;
                    buffer.position(position);
                    startPosition = startPosition - valurLength;
                    buffer.putInt(startPosition);
                    buffer.putInt(valurLength);
                    buffer.position(startPosition);
                    buffer.put(valueByte);

                    ArrayList<Integer> list = new ArrayList<>();
                    list.add(startPosition);
                    list.add(valurLength);
                    map.put(strings[1],list);
                    response = "ok";
                    break;

                case "get":
                    if(map.containsKey(strings[1])){
                        List<Integer> getPositionList = map.get(strings[1]);
                        buffer.position(getPositionList.get(0));
                        buffer.limit(getPositionList.get(0) + getPositionList.get(1));

                        ByteBuffer slice = buffer.slice();
                        byte[] bytes = new byte[slice.remaining()];
                        slice.get(bytes);

                        response = new String(bytes,CharsetUtil.UTF_8);
                    }else {
                        response = "null";
                    }

                    break;
                default:
                    response = "wrong";
            }
            System.out.println("收到客户端" + ctx.channel().remoteAddress() + "发送的消息：" + response);
        }
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
