package com.example.demo.snmpServer;

import java.util.HashMap;
import java.util.Map;

public class SnmpServerCreater {
    private Map Servers = new HashMap();
    public synchronized SnmpServer getServer(String ip, String readcommunity, String writecommunity){
        SnmpServer t = null;

        if(this.Servers.containsKey(ip)){
            t = (SnmpServer) this.Servers.get(ip);
        }
        else {
            t = new SnmpServer(ip, 161, readcommunity,writecommunity);
            // Thread thread = new Thread(t);
            // thread.start();
            this.Servers.put(ip, t);
        }
        return t;
    }
    //取消管理
    public void free(String ip){
        if(this.Servers.containsKey(ip)){
            this.Servers.remove(ip);
        }
    }
}
