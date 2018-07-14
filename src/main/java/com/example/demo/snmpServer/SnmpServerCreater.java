package com.example.demo.snmpServer;

import java.util.HashMap;
import java.util.Map;

public class SnmpServerCreater {
    private static Map Servers = new HashMap();
    public static synchronized SnmpServer getServer(String ip) {
        return getServer(ip, "public", "private");
    }

    // 如果只给定一个community，那么默认这个community是read的，write默认是private
    public static synchronized SnmpServer getServer(String ip, String community){
        return getServer(ip, community, "private");
    }
    public static synchronized SnmpServer getServer(String ip, String readcommunity, String writecommunity){
        SnmpServer t = null;

        if(Servers.containsKey(ip)){
            t = (SnmpServer) Servers.get(ip);
        }
        else {
            t = new SnmpServer(ip, 161, readcommunity,writecommunity);
            // Thread thread = new Thread(t);
            // thread.start();
            Servers.put(ip, t);
        }
        return t;
    }
    //取消管理
    public void free(String ip){
        if(Servers.containsKey(ip)){
            Servers.remove(ip);
        }
    }

}
