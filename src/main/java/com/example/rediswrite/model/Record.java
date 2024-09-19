package com.example.rediswrite.model;

import java.io.Serializable;

public class Record implements Serializable {
    Integer position;
    Integer length;

    public Record() {
    }

    public Record(Integer position, Integer length) {
        this.position = position;
        this.length = length;
    }


    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }
}
