package com.example.demo.snmpServer.Data;

public enum DeviceType {
    host(0), exchange(1), router(2);
    private int type = 0;
    private DeviceType(int type){
        this.type = type;
    }
    public int getType(){
        return this.type;
    }
}
