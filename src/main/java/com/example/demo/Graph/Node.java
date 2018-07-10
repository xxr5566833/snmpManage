package com.example.demo.Graph;

import com.example.demo.snmpServer.Data.IP;

import java.util.Vector;

public class Node {
    public Vector<IP> ips;
    public String mainIp;
    public NodeType type;
    public NodeStatus status;
    public int index;
    public Node(String mainip, NodeType type, int index){
        this.mainIp = mainip;
        this.status = NodeStatus.undiscovered;
        this.ips = new Vector<IP>();
        this.type = type;
        this.index = index;
    }

    public int getIndex() {
        return index;
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
}
