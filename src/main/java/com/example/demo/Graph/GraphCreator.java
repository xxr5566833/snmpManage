package com.example.demo.Graph;

import com.example.demo.snmpServer.Data.DeviceType;
import com.example.demo.snmpServer.Data.IP;
import com.example.demo.snmpServer.SnmpServer;
import com.example.demo.snmpServer.SnmpServerCreater;

import java.util.Vector;

public class GraphCreator {

    public static Graph createGraph(String ip){
        Graph graph = new Graph();

        dfs(ip, graph);

        return graph;
    }
    private static void dfs(String ip, Graph graph){
        // 通过ip创建对应的节点
        SnmpServer t = SnmpServerCreater.getServer(ip);
        // 现在需要通过snmp服务器获得一些信息
        // 首先是自己的ip信息
        Vector<IP> ips = t.getOwnIp();
        // 类型
        DeviceType type = t.getDeviceType();
        // 获得路由表
    }
}
