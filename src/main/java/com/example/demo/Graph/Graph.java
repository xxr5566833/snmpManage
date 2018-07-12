package com.example.demo.Graph;

import com.example.demo.snmpServer.Data.IP;
import com.sun.security.auth.X500Principal;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class Graph {
    private Vector<Node> nodes;
    private Map ip2Node ;
    private Vector<Node> exchanges = new Vector<>();
    private Vector<Node> routers = new Vector<>();
    private Vector<Node> hosts = new Vector<>();
    public Graph(){
        nodes = new Vector<Node>();
        ip2Node = new HashMap();
    }

    public GraphData toData(){
        NodeData[] ns = new NodeData[this.nodes.size()];
        Vector<LinkData> links = new Vector<>();
        for(int i = 0 ; i < this.nodes.size() ; i++){
            Node n = this.nodes.elementAt(i);
            ns[i] = new NodeData();
            ns[i].setName(n.getName());
            ns[i].setCategory(n.getType());
            Vector<Edge> edges = n.getEdges();
            for(int j = 0 ; j < edges.size() ; j++){
                Node dest = edges.elementAt(j).getDest();
                // 检查是否重复
                boolean flag = false;
                for(int k = 0 ; k < links.size() ; k++){
                    LinkData link = links.elementAt(k);
                    if((dest.getName().equals(link.getSource()) && n.getName().equals(link.getTarget()))
                            || (dest.getName().equals(link.getTarget()) && n.getName().equals(link.getSource()))){
                        flag = true;
                        break;
                    }
                }
                if(!flag){
                    // 说明没有重复，那么吧这个边加入到links中
                    LinkData link = new LinkData();
                    link.setSource(n.getName());
                    link.setTarget(dest.getName());
                    links.add(link);
                }
            }
        }
        GraphData data = new GraphData();
        data.setData(ns);
        LinkData[] linkdatas  = new LinkData[links.size()];
        data.setLink(links.toArray(linkdatas));
        return data;

    }

    public void setTypeNode(){
        for(Node n : this.nodes) {
            switch (n.getType()) {
                case host:
                    this.hosts.add(n);
                    break;
                case gateway:
                    this.routers.add(n);
                    break;
                case exchange:
                    this.exchanges.add(n);
                    break;
            }
        }
    }

    public int getNodeNum(){
        return this.nodes.size();
    }

    public Vector<Node> getNodes(){
        return this.nodes;
    }

    public void setNodes(Vector<Node> nodes){
        this.nodes = nodes;
    }

    public void addNode(Node n){
        nodes.add(n);
        if(n.getType() != NodeType.host){
            for(IP ip : n.getIps()){
                ip2Node.put(ip.getIpAddress(), n);
            }
        }
        else{
            ip2Node.put(n.getMainIp(), n);
        }
    }

    public void setIps(Node n, Vector<IP> ips){
        n.setIps(ips);
        for(IP ip : ips){
            this.ip2Node.put(ip.getIpAddress(), n);
        }
    }



    public Node ipToNode(String ip){
        return (Node)this.ip2Node.get(ip);
    }

    public boolean isConnected(int i, int j){
        Vector<Edge> edges = this.nodes.elementAt(i).getEdges();
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
        this.nodes.elementAt(i).addEdge(this.nodes.elementAt(j));
        this.nodes.elementAt(j).addEdge(this.nodes.elementAt(i));
    }

    public Vector<Edge> getNbrs(int i){
        return this.nodes.elementAt(i).getEdges();
    }

    public Node getNode(int index){
        return this.nodes.elementAt(index);
    }

}
