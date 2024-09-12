package com.example.rediswrite.server;

import com.example.rediswrite.Entry;
import com.example.rediswrite.model.Record;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;


import java.nio.ByteBuffer;

import java.util.*;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    Map<String, Record> map = new HashMap<>();
    ByteBuffer buffer = ByteBuffer.allocate(1024*1024*1024);
    //list()方法，返回所有的key
    //stat()方法，返回所有的value
    //delete()方法，删除数据

    //面向对象的写法

    //handler拆成三分，管理连接的、管理内存的和一个别的
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //获取客户端发送过来的消息
        String[] split = getStrings((ByteBuf) msg);
        for(int i = 0;i < split.length;i++){
            String[] strings = split[i].split(" ");
            String command = strings[0];
            String response;

            switch(command){
                case "set":
                    set(strings);
                    response = "set ok";
                    break;
                case "get":
                    response = get(strings[1]);
                    break;
                case "list":
                    response = "key一共有"+list()+"个";
                    break;
                case "stat":
                    response = "value一共有"+stat()+"个";
                    break;
                case "delete":
                    response = delete(strings[1]);
                    break;
                default:
                    response = "wrong";
            }
            System.out.println("收到客户端" + ctx.channel().remoteAddress() + "发送的消息：" + response);
        }
    }

    private String delete(String key){
        String response;
        if(!map.containsKey(key)){
            response = "key is null";
        }

        response = "delete ok";
        return response;
    }

    private Integer stat(){
        int size = 0;
        Set<String> strings = map.keySet();
        for(String s:strings){
            String value = get(s);
            System.out.println("value:"+value);
            size++;
        }
        return size;
    }

    private Integer list() {
        Set<String> strings = map.keySet();
        for(String s:strings){
            System.out.println("key:"+s);
        }
        return strings.size();
    }

    private static String[] getStrings(ByteBuf msg) {
        String message = msg.toString(CharsetUtil.UTF_8);
        return message.split("\n\t");
    }

    private String get(String key) {
        String value;
        if(map.containsKey(key)){
            buffer.limit(1024*1024*1024);
            Record record = map.get(key);
            buffer.position(record.getPosition());
            buffer.limit(record.getPosition() + record.getLength());
            ByteBuffer slice = buffer.slice();
            byte[] bytes = new byte[slice.remaining()];
            slice.get(bytes);

            value = new String(bytes, CharsetUtil.UTF_8);
        }else {
            value = "null";
        }
        return value;
    }

    private void set(String[] strings) {
        byte[] valueByte = strings[2].getBytes();
        int valurLength = valueByte.length;
        buffer.position(0);
        int size = buffer.getInt();
        buffer.position(0);
        buffer.putInt(size + 1);
        int position = 4 + 8 * size;
        int startPosition;
        if(size == 0){
            startPosition = 1024*1024*1024 - valurLength;
        }else {
            buffer.position(position - 8);
            int lastPosition = buffer.getInt();
            startPosition = lastPosition - valurLength;
        }
        buffer.position(position);
        buffer.putInt(startPosition);
        buffer.putInt(valurLength);
        buffer.position(startPosition);
        buffer.put(valueByte);

        Record record = new Record(startPosition, valurLength);
        map.put(strings[1],record);
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
