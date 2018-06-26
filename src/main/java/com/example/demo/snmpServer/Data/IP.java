package com.example.demo.snmpServer.Data;

public class IP {
    private String ipAddress;
    private String ipIfIndex;
    private String ipNetMask;
    private String ipMaxSize;

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getIpIfIndex() {
        return ipIfIndex;
    }

    public void setIpIfIndex(String ipIfIndex) {
        this.ipIfIndex = ipIfIndex;
    }

    public String getIpMaxSize() {
        return ipMaxSize;
    }

    public void setIpMaxSize(String ipMaxSize) {
        this.ipMaxSize = ipMaxSize;
    }

    public String getIpNetMask() {
        return ipNetMask;
    }

    public void setIpNetMask(String ipNetMask) {
        this.ipNetMask = ipNetMask;
    }
}
