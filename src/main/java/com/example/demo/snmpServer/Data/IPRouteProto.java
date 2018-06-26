package com.example.demo.snmpServer.Data;

public enum IPRouteProto {
    other(1),
    local(2),
    netmgmt(3),
    icmp(4),
    egp(5),
    ggp(6),
    hello(7),
    rip(8),
    is_is(9),
    es_is(10),
    ciscoIgrp(11),
    bbnSpfIgp(12),
    ospf(13),
    bgp(14);
    private int type = 0;
    IPRouteProto(int type){
        this.type = type;
    }
    public static IPRouteProto int2type(int type){
        return IPRouteProto.values()[type - 1];
    }
}
