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

    public static boolean isAllOne(String ipAddress, String ipNetMask){
        // 给定一个IP和其子网号，这里需要判断这个IP是否是有效的
        String[] ipaddressarray = ipAddress.split("\\.");
        String[] ipnetmasks = ipNetMask.split("\\.");
        String[] newips = new String[4];
        for(int i = 0 ; i < 4 ; i++){
            int result = Integer.parseInt(ipaddressarray[i]) | Integer.parseInt(ipnetmasks[i]);
            newips[i] = String.format("%d", result);
        }
        return String.join(".").equals("255.255.255.255");
    }
}
