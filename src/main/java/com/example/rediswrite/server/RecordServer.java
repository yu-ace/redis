package com.example.rediswrite.server;

import com.example.rediswrite.dao.RecordDao;

import java.util.Set;

public class RecordServer {
    private final RecordDao recordDao = RecordDao.getInstance();
    private static final RecordServer recordServer = new RecordServer();
    private RecordServer(){
    }
    public static RecordServer getInstance(){
        return recordServer;
    }

    public void setKey(String[] strings){
        recordDao.set(strings);
    }

    public String getValue(String key){
        return recordDao.get(key);
    }
    public Integer stat(){
        int size = 0;
        Set<String> strings = recordDao.keyList();
        for(String s:strings){
            String value = recordDao.get(s);
            System.out.println("value:"+value);
            size++;
        }
        return size;
    }

    public String deleteKey(String key){
        return recordDao.delete(key);
    }

    public Integer list() {
        Set<String> strings = recordDao.keyList();
        for(String s:strings){
            System.out.println("key:"+s);
        }
        return strings.size();
    }
}
