package com.example.rediswrite.dao;

import com.example.rediswrite.model.Record;
import io.netty.util.CharsetUtil;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RecordDao {
    Map<String, Record> map = new HashMap<>();
    ByteBuffer buffer = ByteBuffer.allocate(1024*1024*1024);

    private static final RecordDao recordDao = new RecordDao();
    private RecordDao(){
    }
    public static RecordDao getInstance(){
        return recordDao;
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

    public void set(String[] strings) {
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
