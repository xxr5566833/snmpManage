package com.example.demo.Graph;

import com.example.demo.snmpServer.Data.IP;

import java.util.Vector;

public class Node {
    public Vector<IP> ips;
    public NodeType type;
    public String mask;
    public NodeStatus status;
    public int index;
    public Node(Vector<IP> ips, String mask, NodeType type, int index){
        this.ips = ips;
        this.mask = mask;
        this.type = type;
        this.status = NodeStatus.undiscovered;
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

    public String getMask() {
        return mask;
    }

    public void setMask(String mask) {
        this.mask = mask;
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



}
