package com.example.demo.snmpServer.Data;

public class IP {
    private String ipAddress;
    private int ipIfIndex;
    private String ipNetMask;
    private int ipMaxSize;

    public IP(){}

    public IP(String ipaddress, String ipnetmask){
        this.ipAddress = ipaddress;
        this.ipNetMask = ipnetmask;
        this.ipIfIndex = -1;
        this.ipMaxSize = -1;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public static int compare(String mask1, String mask2){
        IPv4 ip1 = new IPv4();
        ip1.setMask(mask1);
        IPv4 ip2 = new IPv4();
        ip2.setMask(mask2);
        if(ip1.getMaskInt() > ip2.getMaskInt()){
            return 1;
        }
        else if(ip1.getMaskInt() < ip2.getMaskInt()){
            return -1;
        }
        else
            return 0;
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
