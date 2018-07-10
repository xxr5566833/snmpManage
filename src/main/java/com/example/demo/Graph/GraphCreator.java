package com.example.demo.Graph;

import com.example.demo.snmpServer.Data.DeviceType;
import com.example.demo.snmpServer.Data.IP;
import com.example.demo.snmpServer.Data.IPRoute;
import com.example.demo.snmpServer.Data.IPRouteType;
import com.example.demo.snmpServer.SnmpServer;
import com.example.demo.snmpServer.SnmpServerCreater;

import java.util.Vector;

public class GraphCreator {

    public static Graph createGraph(String ip){
        Graph graph = new Graph();
        // 通过ip创建对应的节点

        Node n = new Node(ip, 0);
        n.setType(NodeType.gateway);
        graph.addNode(n);
        dfs(n, graph);

        return graph;
    }

    private static void dfs(Node n, Graph graph){

        // 现在需要通过snmp服务器获得一些信息
        // 类型
        if(n.getType() != NodeType.gateway)
            // 非网关不需要继续深度遍历
            return ;
        SnmpServer t = SnmpServerCreater.getServer(n.getMainIp());
        DeviceType type = t.getDeviceType();
        if(n.getType() == NodeType.other)
            n.setType(type == DeviceType.router ? NodeType.gateway : NodeType.subnet);

        // 首先是自己的ip信息
        Vector<IP> ips = t.getOwnIp();
        n.setIps(ips);
        // 获得路由表，通过路由表获得它所有相连节点
        IPRoute[] iproutes = t.getIpRoute();
        // 获得路由表后，需要区分直接路由和间接路由，直接对应了子网，间接路由对应了三层交换机或者路由器
        for(int i = 0 ; i < iproutes.length ; i++){
            IPRoute ipr = iproutes[i];
            if(ipr.getIpRouteType() == IPRouteType.direct) {
                // 直接相连还要分情况
                String nexthop = ipr.getIpRouteNextHop();
                if(nexthop.equals("127.0.0.1") || nexthop.equals("0.0.0.0"))
                    continue;
                if(!graph.isRepeated(ipr.getIpRouteDest())){
                    if(ipr.getIpRouteMask().equals("255.255.255.255") ){
                        // 说明可能有直连路由器，那么此时需要检验dest的合法性
                        if(!IP.isAllOne(ipr.getIpRouteDest(), ipr.getIpRouteMask())) {
                            Node newn = new Node(ipr.getIpRouteDest(), NodeType.gateway, graph.getNodeNum());
                            graph.addNode(newn);
                            graph.addEdge(n.getIndex(), newn.getIndex());
                            graph.addEdge(newn.getIndex(), n.getIndex());
                        }
                    }
                    else {
                        // 说明是子网，因为子网号不可能为全1
                        Node newn = new Node(ipr.getIpRouteDest(), NodeType.subnet, graph.getNodeNum());
                        graph.addNode(newn);
                        graph.addEdge(n.getIndex(), newn.getIndex());
                        graph.addEdge(newn.getIndex(), n.getIndex());
                    }
                }
            }
            else{
                String nexthop = ipr.getIpRouteNextHop();
                if (!graph.isRepeated(nexthop)) {
                    // IP地址不重复，那么相当于发现新设备
                    Node newn = new Node(nexthop, NodeType.gateway graph.getNodeNum());
                    graph.addNode(newn);
                    graph.addEdge(n.getIndex(), newn.getIndex());
                    graph.addEdge(newn.getIndex(), n.getIndex());
                }
            }
        }
        // 获取到了连线后，开始深度优先
        Vector<Edge> edges = graph.getNbrs(n.getIndex());
        for(int i = 0 ; i < edges.size() ; i ++){
            Edge e = edges.elementAt(i);
            if(e.getStatus() == 0){
                // 反过来也要设置
                e.setStatus(1);
                graph.getNbrs(e.getDestIndex()).elementAt(n.getIndex()).setStatus(1);
                dfs(graph.getNode(e.getDestIndex()), graph);
            }
        }
    }
}
