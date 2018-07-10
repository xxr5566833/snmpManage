package com.example.demo.Graph;

import com.example.demo.snmpServer.Data.IP;
import com.sun.security.auth.X500Principal;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class Graph {
    public Vector<Node> nodes;
    public Vector<Vector<Edge>> edges;
    public Map ip2Node ;

    public Graph(){
        nodes = new Vector<Node>();
        edges = new Vector<Vector<Edge>>();
        ip2Node = new HashMap();
    }

    public int getNodeNum(){
        return this.nodes.size();
    }

    public void addNode(Node n){
        nodes.add(n);
        if(n.getType() == NodeType.gateway){
            for(IP ip : n.getIps()){
                ip2Node.put(ip.getIpAddress(), n);
            }
        }
        else{
            ip2Node.put(n.getMainIp(), n);
        }
        edges.add(new Vector<Edge>());
    }

    public boolean isSameSubnet(String ip, String subnet, String mask){
        String[] ips = ip.split("\\.");
        String[] masks = mask.split("\\.");
        String[] newips = new String[4];
        String[] subnets = subnet.split("\\.");
        String[] newsubnets = new String[4];
        for(int i = 0 ; i < 4 ; i++){
            int sub = Integer.parseInt(ips[i]) & Integer.parseInt(masks[i]);
            newips[i] = String.format("%d", sub);
            int newsub = Integer.parseInt(subnets[i]) & Integer.parseInt(masks[i]);
            newsubnets[i] = String.format("%d", newsub);
        }
        String newip = String.join(".", newips);
        String newsubnet = String.join(".", newsubnets);
        return newsubnet.equals(newip);
    }


    public Node ipToNode(String ip){
        return (Node)this.ip2Node.get(ip);
    }

    public boolean isConnected(int i, int j){
        Vector<Edge> edges = this.edges.elementAt(i);
        for(Edge e : edges){
            if(e.getDestIndex() == j){
                return true;
            }
        }
        return false;
    }

    public boolean isRepeated(String ip){
        for(Node n : nodes){
            Vector<IP> ips = n.getIps();
            for(IP oneip : ips){
                if(oneip.getIpAddress().equals(ip)){
                    return true;
                }
            }
            if(n.getMainIp().equals(ip))
                return true;
        }
        return false;
    }

    public void addEdge(int i, int j){
        Vector<Edge> edges = this.edges.elementAt(i);
        // 首先确定不能重复加
        edges.add(new Edge(j));
    }

    public Vector<Edge> getNbrs(int i){
        return this.edges.elementAt(i);
    }

    public Node getNode(int index){
        return this.nodes.elementAt(index);
    }

}