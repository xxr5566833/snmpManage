package com.example.demo.snmpServer.Data;

public class IP {
    private String ipAddress;
    private int ipIfIndex;
    private String ipNetMask;
    private int ipMaxSize;

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getIpIfIndex() {
        return ipIfIndex;
    }

    public void setIpIfIndex(int ipIfIndex) {
        this.ipIfIndex = ipIfIndex;
    }

    public int getIpMaxSize() {
        return ipMaxSize;
    }

    public void setIpMaxSize(int ipMaxSize) {
        this.ipMaxSize = ipMaxSize;
    }

    public String getIpNetMask() {
        return ipNetMask;
    }

    public void setIpNetMask(String ipNetMask) {
        this.ipNetMask = ipNetMask;
    }
}
