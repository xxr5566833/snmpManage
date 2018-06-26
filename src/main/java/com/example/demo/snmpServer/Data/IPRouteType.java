package com.example.demo.snmpServer.Data;

public enum IPRouteType {
    other(1),
    invalid(2),
    direct(3),
    indirect(4);
    private int type;
    IPRouteType(int type){
        this.type = type;
    }
    public static IPRouteType int2Type(int type){
        return IPRouteType.values()[type - 1];
    }
}
