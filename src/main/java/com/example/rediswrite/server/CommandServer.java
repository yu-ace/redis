package com.example.rediswrite.server;

import com.example.rediswrite.model.Command;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public class CommandServer {
    private static final CommandServer commandServer = new CommandServer();
    private CommandServer(){
        initializeCommands();
    }
    public static CommandServer getInstance(){
        return commandServer;
    }


    private final Map<String, Function<Command, String>> commandMap = new HashMap<>();
    private final Memory memory = Memory.getInstance();
    public void initializeCommands() {
        commandMap.put("set", this::setKey);
        commandMap.put("get", this::getValue);
        commandMap.put("setNX", this::setNX);
        commandMap.put("exists",this::exists);
        commandMap.put("stat", command -> stat());
        commandMap.put("delete", this::deleteKey);
        commandMap.put("list", command -> list());
    }

    public String executeCommand(Command command) {
        String result;
        Function<Command, String> action = commandMap.get(command.getName());
        if (action != null) {
            result = action.apply(command);
        } else {
            result = "Unknown command: " + command.getName();
        }
        return result;
    }

    public String exists(Command command){
        String s = memory.get(command.getKey());
        if(!(s == null)){
            return "key 存在";
        }
        return "key 不存在";
    }
    public String setNX(Command command){
        String s = memory.get(command.getKey());
        if(!(Objects.equals(s, " ")) && !(Objects.equals(s, "null"))){
            return "key 存在";
        }
        memory.set(command.getKey(), command.getValue());
        return "key 添加成功";

    }

    public String setKey(Command command){
        memory.set(command.getKey(), command.getValue());
        return "set ok";
    }

    public String getValue(Command command){
        return memory.get(command.getKey());
    }
    public String stat(){
        StringBuilder stringBuilder = new StringBuilder();
        Set<String> keys = memory.keyList();
        for(String key:keys){
            String value = memory.get(key);
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
}
