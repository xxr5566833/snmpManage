package com.example.demo.Graph;

public class DevData {
    private String name;
    private String ip;
    private String readcommunity;
    private String writecommunity;
    private NodeType deviceType;

    public void setDeviceType(NodeType type) {
        this.deviceType = type;
    }

    public NodeType getDeviceType() {
        return deviceType;
    }
    public DevData(String name, String ip, NodeType type){
        this.name = name;
        this.ip = ip;
        this.deviceType = type;
        this.readcommunity = "public";
        this.writecommunity = "private";
    }
    public DevData(String name, String ip, NodeType type, String readcommunity){
        this(name, ip, type);
        this.readcommunity = readcommunity;
    }
    public DevData(String name, String ip, NodeType type, String readcommunity, String writecommunity){
        this(name, ip, type, readcommunity);
        this.writecommunity = writecommunity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public String getReadcommunity() {
        return readcommunity;
    }

    public String getWritecommunity() {
        return writecommunity;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setReadcommunity(String readcommunity) {
        this.readcommunity = readcommunity;
    }

    public void setWritecommunity(String writecommunity) {
        this.writecommunity = writecommunity;
    }
}



