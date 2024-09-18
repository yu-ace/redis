package com.example.rediswrite.server;

import com.example.rediswrite.model.Command;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CommandServer {
    private final Memory memory = Memory.getInstance();
    private static final CommandServer commandServer = new CommandServer();
    private CommandServer(){
    }
    public static CommandServer getInstance(){
        return commandServer;
    }
    private final Map<String, Runnable> runnableMap = new HashMap<>();

    public Map<String, Runnable> getRunnableMap(Command command){
        runnableMap.put("set",()->setKey(command.getKey(), command.getValue()));
        runnableMap.put("get",()->getValue(command.getKey()));
        runnableMap.put("stat", this::stat);
        runnableMap.put("delete",()->deleteKey(command.getKey()));
        return runnableMap;
    }

    public void setKey(String key,Object value){
        memory.set(key, value);
    }

    public String getValue(String key){
        return memory.get(key);
    }
    public Integer stat(){
        int size = 0;
        Set<String> strings = memory.keyList();
        for(String s:strings){
            String value = memory.get(s);
            System.out.println("value:"+value);
            size++;
        }
        return size;
    }

    public String deleteKey(String key){
        return memory.delete(key);
    }

    public Integer list() {
        Set<String> strings = memory.keyList();
        for(String s:strings){
            System.out.println("key:"+s);
        }
        return strings.size();
    }
}
