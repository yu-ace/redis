package com.example.rediswrite;

import java.io.Serializable;
import java.util.ArrayList;

public class Entry implements Serializable {
    String key;
    ArrayList<Integer> valueList;

    public Entry() {
    }

    public Entry(String key, ArrayList<Integer> valueList) {
        this.key = key;
        this.valueList = valueList;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public ArrayList<Integer> getValueList() {
        return valueList;
    }

    public void setValueList(ArrayList<Integer> valueList) {
        this.valueList = valueList;
    }
}
