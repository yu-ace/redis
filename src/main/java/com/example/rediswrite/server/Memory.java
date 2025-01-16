package com.example.rediswrite.server;

import com.example.rediswrite.model.Record;
import io.netty.util.CharsetUtil;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Memory {
    Map<String, Record> map;
    ByteBuffer buffer;
    private static final Integer TYPE_INTEGER = 0;
    private static final Integer TYPE_STRING = 1;
    private static final Memory recordDao = new Memory();
    private Memory(){
    }
    public static Memory getInstance(){
        return recordDao;
    }

    public void init() throws Exception {
        String path = "C:\\Users\\cfcz4\\OneDrive\\Desktop\\data.bin";
        File file = new File(path);
        if(file.exists()){
            try(RandomAccessFile read = new RandomAccessFile(path, "rw")) {
                long length = read.length();
                buffer = ByteBuffer.allocate((int) length);
                FileChannel channel = read.getChannel();
                channel.read(buffer);
                buffer.flip();
            }
        }else{
            buffer = ByteBuffer.allocate(1024*1024);
        }
    }

    public void initMap() throws Exception {
        String path = "C:\\Users\\cfcz4\\OneDrive\\Desktop\\map.bin";
        File file = new File(path);
        map = new HashMap<>();
        if(file.exists()){
            FileInputStream fileInputStream = new FileInputStream(path);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            map = (Map<String,Record>) objectInputStream.readObject();
        }
    }

    public void saveMap() throws Exception {
        String path = "C:\\Users\\cfcz4\\OneDrive\\Desktop\\map.bin";
        FileOutputStream fileOutputStream = new FileOutputStream(path);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(map);
    }

    public void shutDown() throws Exception{
        String path = "C:\\Users\\cfcz4\\OneDrive\\Desktop\\data.bin";
        RandomAccessFile file = new RandomAccessFile(path,"rw");
        FileChannel channel = file.getChannel();
        buffer.flip();
        buffer.limit(buffer.capacity());
        channel.write(buffer);
        file.close();
    }

    public Object get(String key) {
        Object value;
        if(map.containsKey(key)){
            buffer.limit(1024*1024);
            Record record = map.get(key);
            buffer.position(record.getPosition());
            if("String".equals(map.get(key).getType())){
                buffer.limit(record.getPosition() + record.getLength());
                ByteBuffer slice = buffer.slice();
                byte[] bytes = new byte[slice.remaining()];
                slice.get(bytes);
                value = new String(bytes, CharsetUtil.UTF_8);
            }else if("Integer".equals(map.get(key).getType())){
                value = buffer.getInt();
            }else {
                value = getList();
            }
        }else {
            value = "null";
        }
        return value;
    }

    private List<Object> getList() {
        int size = buffer.getInt();
        List<Object> list = new ArrayList<>();
        for(int i = 0;i < size;i++){
            int type = buffer.getInt();
            if(type == TYPE_STRING){
                int length = buffer.getInt();
                byte[] bytes = new byte[length];
                buffer.get(bytes);
                list.add(new String(bytes,CharsetUtil.UTF_8));
            }else {
                list.add(buffer.getInt());
            }
        }
        return list;
    }

    public void set(ByteBuffer byteBuffer,Map<String, Record> map,String key,Object value) {
        byte[] valueByte;
        String type;
        if(value instanceof String){
            valueByte = ((String) value).getBytes(StandardCharsets.UTF_8);
            type = "String";
        }else if(value instanceof Integer){
            valueByte = ByteBuffer.allocate(4).putInt((Integer) value).array();
            type = "Integer";
        }else{
            valueByte = processList((List<?>) value);
            type = "List";
        }
        Record record = getRecord(byteBuffer,valueByte, type);
        map.put(key,record);
        buffer = byteBuffer;
    }

    private Record getRecord(ByteBuffer buffer,byte[] valueByte, String type) {
        int valurLength = valueByte.length;
        buffer.position(0);
        int size = buffer.getInt();
        buffer.position(0);
        buffer.putInt(size + 1);
        int position = 4 + 8 * size;
        int startPosition;
        if(size == 0){
            startPosition = 1024*1024 - valurLength;
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

        return new Record(startPosition, valurLength, type);
    }

    private byte[] processList(List<?> value) {
        try{
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
            dataOutputStream.writeInt(value.size());
            for(Object v:value){
                if(v instanceof String){
                    byte[] bytes = ((String) v).getBytes(StandardCharsets.UTF_8);
                    dataOutputStream.writeInt(TYPE_STRING);
                    dataOutputStream.writeInt(bytes.length);
                    dataOutputStream.write(bytes);
                }else {
                    dataOutputStream.writeInt(TYPE_INTEGER);
                    dataOutputStream.writeInt(4);
                    dataOutputStream.write((Integer) v);
                }
            }
            return byteArrayOutputStream.toByteArray();
        }catch (Exception e){
            e.getStackTrace();
        }
        return null;
    }

    public Object incr(String key){
        Object o = get(key);
        if(o instanceof Integer value){
            value++;
            set(buffer,map,key,value);
            return value;
        }if(o instanceof String && "null".equals((String)o)){
            set(buffer,map,key,1);
            return 1;
        }else {
            return "value的类型不是Integer值";
        }
    }

    public Object decr(String key){
        Object o = get(key);
        if(o instanceof Integer value){
            value--;
            set(buffer,map,key,value);
            return value;
        }if(o instanceof String && "null".equals((String)o)){
            set(buffer,map,key,-1);
            return -1;
        }else {
            return "value的类型不是Integer值";
        }
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
