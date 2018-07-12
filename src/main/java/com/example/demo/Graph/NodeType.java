package com.example.demo.Graph;

public enum NodeType {
    gateway(0), exchange(1), host(2), other(3);
    private int type = 3;
    private NodeType(int type){
        this.type = type;
    }
    public int getType(){
        return this.type;
    }
}
