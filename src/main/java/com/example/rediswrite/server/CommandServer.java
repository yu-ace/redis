package com.example.rediswrite.server;

import com.example.rediswrite.model.Command;
import com.example.rediswrite.model.Record;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Function;

public class CommandServer {
    private static final CommandServer commandServer = new CommandServer();
    private CommandServer(){
        initializeCommands();
    }
    public static CommandServer getInstance(){
        return commandServer;
    }


    private final Map<String, Function<Command, Object>> commandMap = new HashMap<>();
    private final Memory memory = Memory.getInstance();
    public void initializeCommands() {
        commandMap.put("set", this::setKey);
        commandMap.put("get", this::getValue);
        commandMap.put("setNX", this::setNX);
        commandMap.put("exists",this::exists);
        commandMap.put("stat", command -> stat());
        commandMap.put("delete", this::deleteKey);
        commandMap.put("list", command -> list());
        commandMap.put("decr",this::decr);
        commandMap.put("incr",this::incr);
    }

    public Object executeCommand(Command command) {
        Object result;
        Function<Command, Object> action = commandMap.get(command.getName());
        if (action != null) {
            result = action.apply(command);
        } else {
            result = "Unknown command: " + command.getName();
        }
        return result;
    }

    public void init() throws Exception{
        memory.initMap();
        memory.init();
    }

    public void shutDown() throws Exception{
        memory.shutDown();
        memory.saveMap();
    }

    public String exists(Command command){
        Object s = memory.get(command.getKey());
        if(!(s == null)){
            return "key 存在";
        }
        return "key 不存在";
    }
    public String setNX(Command command){
        Object s = memory.get(command.getKey());
        if(!(Objects.equals(s, " ")) && !(Objects.equals(s, "null"))){
            return "key 存在";
        }
        memory.set(memory.buffer,memory.map,command.getKey(), command.getValue());
        return "key 添加成功";

    }

    public Object incr(Command command){
        return memory.incr(command.getKey());
    }

    public Object decr(Command command){
        return memory.decr(command.getKey());
    }

    public String setKey(Command command){
        memory.set(memory.buffer,memory.map,command.getKey(), command.getValue());
        return "set ok";
    }

    public Object getValue(Command command){
        return memory.get(command.getKey());
    }
    public Object stat(){
        StringBuilder stringBuilder = new StringBuilder();
        Set<String> keys = memory.keyList();
        for(String key:keys){
            Object value = memory.get(key);
            stringBuilder.append(value).append(" ");
        }
        return stringBuilder.toString();
    }

    public String deleteKey(Command command){
        return memory.delete(command.getKey());
    }

    public String list() {
        StringBuilder stringBuilder = new StringBuilder();
        Set<String> keys = memory.keyList();
        for(String key:keys){
            stringBuilder.append(key).append(" ");
        }
        return stringBuilder.toString();
    }

    public void clean() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 1024);
        Map<String, Record> map = new HashMap<>();
        Set<String> strings = memory.keyList();
        for(String s:strings){
            memory.set(byteBuffer,map,s,memory.get(s));
        }
    }

    public void cleanTime(){
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                clean();
            }
        };
        timer.scheduleAtFixedRate(timerTask,0,30*60*1000);
    }
}
