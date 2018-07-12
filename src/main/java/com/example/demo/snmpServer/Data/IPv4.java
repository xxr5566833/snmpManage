package com.example.demo.snmpServer.Data;

public class IPv4 {
    private int ip;
    private int mask;
    private int subnet;
    public IPv4(){
        this.ip = 0;
        this.mask = 0;
        this.subnet = 0;
    }

    public IPv4(String ip, String mask){
        this(ip);
        this.setMask(mask);
        this.subnet = this.ip & this.mask;
    }

    public IPv4(int ip){
        this.ip = ip;
        this.mask = (255 << 24) + (255 << 16) + (255 << 8) + 255;
        this.subnet = ip;
    }

    public IPv4(String ip){
        // TODO 检查ip的合法性
        this.ip = 0;
        String[] splitip = ip.split("\\.");
        for(int i = 0 ; i < 4 ; i ++){
            this.ip = this.ip + (Integer.parseInt(splitip[i]) << (8 * (3 - i)));
        }
        this.mask = (255 << 24) + (255 << 16) + (255 << 8) + 255;
        this.subnet = this.ip;
    }

    public IPv4(int[] ips){
        int ip = 0;
        for(int i = 0 ; i < 4 ; i ++){
            ip = ip + (ips[i] << (8 * (3 - i)));
        }
        this.mask = (255 << 24) + (255 << 16) + (255 << 8) + 255;
        this.subnet = this.ip;
    }

    public void setMask(String mask){
        String[] masks = mask.split("\\.");
        this.mask = 0;
        for(int i = 0 ; i < 4 ; i++){
            int value = Integer.parseInt(masks[i]);
            this.mask = this.mask + (value << (8 * (3 - i)));
        }
    }

    public void setMask(int mask){
        this.mask = mask;
    }

    public void setMaskFromBitNum(int maskNum){
        this.mask = 1 << maskNum;
    }

    public String getMaskString(){
        return int2String(this.getMaskInt());
    }

    public static String int2String(int mask){
        String[] masks = new String[4];
        int result1 = mask >> 3;
        int result2 = mask % 8;
        for(int i = 0 ; i < result1 ; i++){
            masks[i] = "255";
        }
        if(result1 < 4){
            int value = 1 << result2;
            masks[result1++] = String.format("%d", value);
        }
        for(int i = result1 ; i < 4 ; i ++){
            masks[i] = "0";
        }
        return String.join(".", masks);
    }
    public String getSubnet(){
        return int2String(this.subnet);
    }

    public int getMaskInt(){
        if(this.mask == 0)
            return 0;
        for(int i = 1 ; i <= 32 ; i++){
            if((this.mask >> i) == 0)
                break;
        }
        // TODO 不严谨
        return 32;
    }

    public String getIP(){
        String[] s = new String[4];
        for(int i = 0 ; i < 4 ; i++){
            s[i] = String.format("%d", (this.ip >> ((3 - i) * 8)) & 0xFF);
        }

        return String.join(".", s);
    }

    public static boolean isSameSubnet(String ip1, String ip2, String mask){
        String[] ips = ip1.split("\\.");
        String[] masks = mask.split("\\.");
        String[] newips = new String[4];
        String[] subnets = ip2.split("\\.");
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

    public void addOne(){
        this.ip ++;
    }

    public boolean isValid(){
        // 网络号不能为全1或者是全0
        // 必须仍然属于mask所规定的子网范围
        int result = this.ip & (~this.mask);
        IPv4 ip = new IPv4(this.ip & this.mask);
        System.out.println(ip.getIP());
        if(result == 0 || result == ~this.mask || this.subnet != (this.ip & this.mask))
            return false;
        return true;

    }

}
