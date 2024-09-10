package com.example.rediswrite.server;

import com.example.rediswrite.Entry;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    private static final Map<String, List<Integer>> map = new HashMap<>();
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //获取客户端发送过来的消息
        ByteBuf byteBuf = (ByteBuf) msg;
        String message = byteBuf.toString(CharsetUtil.UTF_8);
        String[] split = message.split("\n\t");
        for(int i = 0;i < split.length;i++){
            String[] strings = split[i].split(" ");
            String command = strings[0];
            String response;


            switch(command){
                case "set":
                    //获取文件
                    RandomAccessFile write = new RandomAccessFile(
                            "C:\\Users\\cfcz4\\OneDrive\\Desktop\\data.bin","rw");
                    FileChannel channel = write.getChannel();
                    ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
                    channel.read(buffer);
                    buffer.flip();

                    byte[] keyByte = strings[1].getBytes();
                    byte[] valueByte = strings[2].getBytes();
                    ByteBuffer newBuffer = ByteBuffer.allocate(
                            buffer.remaining() + valueByte.length);

                    int startPosition = buffer.limit();
                    int endPosition = startPosition + valueByte.length;
                    newBuffer.put(valueByte).flip();
                    channel.write(newBuffer);

                    ArrayList<Integer> arrayList = new ArrayList<>();
                    arrayList.add(startPosition);
                    arrayList.add(endPosition);
                    map.put(strings[1],arrayList);
                    response = "ok";

                    write.close();
                    break;

                case "get":
                    RandomAccessFile read = new RandomAccessFile(
                            "C:\\Users\\cfcz4\\OneDrive\\Desktop\\data.bin","rw");
                    FileChannel readChannel = read.getChannel();
                    long length = read.length();
                    ByteBuffer byteBuffer = ByteBuffer.allocate((int) length);
                    readChannel.read(byteBuffer);
                    byteBuffer.flip();

                    if(map.containsKey(strings[1])){
                        List<Integer> getPositionList = map.get(strings[1]);
                        byteBuffer.position(getPositionList.get(0));
                        byteBuffer.limit(getPositionList.get(1));

                        ByteBuffer slice = byteBuffer.slice();
                        byte[] bytes = new byte[slice.remaining()];
                        slice.get(bytes);

                        response = new String(bytes, StandardCharsets.UTF_8);
                    }else {
                        response = "null";
                    }
                    read.close();
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
