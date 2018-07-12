package com.example.demo.Graph;

import com.example.demo.snmpServer.Data.Constant;
import com.example.demo.snmpServer.Data.IP;
import com.example.demo.snmpServer.Data.IPv4;
import com.example.demo.snmpServer.SnmpServer;
import com.example.demo.snmpServer.SnmpServerCreater;
import org.snmp4j.smi.VariableBinding;

import java.io.IOException;
import java.util.Vector;

public class Node {
    public Vector<IP> ips;
    public String mainIp;
    public NodeType type;
    public NodeStatus status;
    private Vector<Edge> edges;
    private String name;
    private int index;

    public Node(String mainIp, NodeType type){
        this.mainIp = mainIp;
        this.status = NodeStatus.undiscovered;
        this.ips = new Vector<>();
        this.type = type;
        this.edges = new Vector<Edge>();
        SnmpServer t = SnmpServerCreater.getServer(mainIp);
        VariableBinding vb = null;
        try {
            vb = t.getTreeNode(Constant.SysName);
        }catch(IOException e){
            e.printStackTrace();
        }
        this.name = vb.getVariable().toString();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Node(String mainip, NodeType type, int index){
        this(mainip, type);
        this.index = index;
    }

    // 增加到n的连接边
    public void addEdge(Node n){
        Edge e = new Edge(n);
        this.edges.add(e);
    }

    public void deleteEdge(Node n){
        for(int i = 0 ; i < this.edges.size() ; i++){
            Edge e = this.edges.elementAt(i);
            if(e.getDest().getMainIp() == n.getMainIp())
                this.edges.remove(i);
            return ;
        }
    }

    public Vector<Edge> getEdges(){
        return this.edges;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public NodeType getType() {
        return type;
    }

    public void setType(NodeType type) {
        this.type = type;
    }


    public void setStatus(NodeStatus status) {
        this.status = status;
    }

    public NodeStatus getStatus() {
        return status;
    }

    public void setIps(Vector<IP> ips){
        this.ips = ips;
    }

    public Vector<IP> getIps() {
        return ips;
    }

    public String getMainIp() {
        return mainIp;
    }

    public void setMainIp(String mainIp) {
        this.mainIp = mainIp;
    }

    public boolean isSameSubnet(String ip){
        for(int i = 0 ; i < this.ips.size() ; i++){
            IP oneip = this.ips.elementAt(i);
            if (IPv4.isSameSubnet(ip, oneip.getIpAddress(), oneip.getIpNetMask()))
                return true;
        }
        return false;
    }

    public boolean isRepeated(String ip){
        if(this.type == NodeType.host){
            return this.mainIp.equals(ip);
        }
        else{
            for(IP oneip :this.ips){
                if(oneip.getIpAddress().equals(ip)){
                    return true;
                }
            }
        }
        return false;
    }

}
