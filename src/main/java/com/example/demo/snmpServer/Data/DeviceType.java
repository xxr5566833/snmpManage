package com.example.demo.snmpServer.Data;

public enum DeviceType {
    // 根据测试结果router也包含了三层交换机
    none(-1), host(0), exchange(1), router(2);
    private int type = -1;
    private DeviceType(int type){
        this.type = type;
    }
    public int getType(){
        return this.type;
    }
}
