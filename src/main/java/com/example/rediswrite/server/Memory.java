package com.example.rediswrite.server;

import com.example.rediswrite.model.Record;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Memory {
    Map<String, Record> map = new HashMap<>();

    ByteBuffer buffer = ByteBuffer.allocate(1024*1024);

    private static final Memory recordDao = new Memory();
    private Memory(){
    }
    public static Memory getInstance(){
        return recordDao;
    }

//    public void init() throws Exception {
//        String path = "C:\\Users\\cfcz4\\OneDrive\\Desktop\\data.bin";
//        File file = new File(path);
//        if(file.exists()){
//            RandomAccessFile read = new RandomAccessFile(path, "rw");
//            long length = read.length();
//            buffer = ByteBuffer.allocate((int) length);
//        }else{
//            buffer = ByteBuffer.allocate(1024*1024*1024);
//        }
//    }

    public void shutDown() throws Exception{
        String path = "C:\\Users\\cfcz4\\OneDrive\\Desktop\\data.txt";
        RandomAccessFile file = new RandomAccessFile(path,"rw");
        FileChannel channel = file.getChannel();
        buffer.flip();
        channel.write(buffer);
        file.close();
    }

    public String get(String key) {
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

    public void set(String key,Object value) {
        byte[] valueByte = ((String) value).getBytes();
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
        map.put(key,record);
    }

    public Set<String> keyList(){
        return map.keySet();
    }

    public String delete(String key){
        String response;
        if(!map.containsKey(key)){
            response = "key is null";
        }else {
            map.remove(key);
            response = "delete ok";
        }
        return response;
    }
}
