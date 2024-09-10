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
    // 建两个文件，一个byteBuffer专门存放数据，另一个存放索引，当需要替换某一个数值时，字节写入一个新的值，然后更新一下索引，旧的值不需要管，没有指针指引就可以了；
    // 索引主要记录key，position和length
    // 先用hashmap，之后再自己实现一个hash函数爱处理；
    private static final int INITIAL_SIZE = 16;
    private LinkedList<Entry>[] table;
    private int size;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        init();
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

                    RandomAccessFile indexWrite = new RandomAccessFile(
                            "C:\\Users\\cfcz4\\OneDrive\\Desktop\\index.bin", "rw");
                    FileChannel indexChannel = indexWrite.getChannel();

                    LinkedList<Entry> entryLinkedList = put(strings, startPosition, endPosition);

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
                    objectOutputStream.writeObject(entryLinkedList);
                    objectOutputStream.flush();
                    byte[] indexBytes = byteArrayOutputStream.toByteArray();
                    ByteBuffer wrap = ByteBuffer.wrap(indexBytes);
                    indexChannel.write(wrap);
                    response = "ok";
                    indexWrite.close();

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

                    RandomAccessFile indexRead = new RandomAccessFile(
                            "C:\\Users\\cfcz4\\OneDrive\\Desktop\\index.bin", "rw");
                    FileChannel indexReadChannel = indexRead.getChannel();
                    ByteBuffer indexReaderBuffer = ByteBuffer.allocate((int) indexRead.length());
                    indexReadChannel.read(indexReaderBuffer);
                    indexReaderBuffer.flip();

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

    private void init(){
        table = new LinkedList[INITIAL_SIZE];
        for(int i = 0;i < INITIAL_SIZE;i++){
            table[i] = new LinkedList<>();
        }
        size = 0;
    }
    private LinkedList<Entry> put(String[] strings, int startPosition, int endPosition) {
        int index = Math.abs(strings[1].hashCode() % table.length);
        LinkedList<Entry> linkedList = table[index];

        ArrayList<Integer> arrayList = new ArrayList<>();
        arrayList.add(startPosition);
        arrayList.add(endPosition);

        for(Entry entry:linkedList){
            if(entry.getKey().equals(strings[1])){
                entry.setValueList(arrayList);
            }
        }
        linkedList.add(new Entry(strings[1],arrayList));
        size++;
        return linkedList;
    }

    private ArrayList<Integer> get(String[] strings) {
        int index = Math.abs(strings[1].hashCode() % table.length);
        LinkedList<Entry> linkedList = table[index];
        for(Entry entry:linkedList){
            if(entry.getKey().equals(strings[1])){
                return entry.getValueList();
            }
        }
        return null;
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
