package com.example.demo.snmpServer.Data;

import com.example.demo.Graph.Node;

import java.util.Vector;

public class PC {
    public String name;
    private String IP;
    public Vector<Integer> connections = new Vector<>();

    public PC(String name, String IP, Vector<Node> hosts,Vector<IP> IPs){
        int i;
        this.name=name;
        this.IP=IP;
        int length=hosts.size();
        for(i=0;i<length;i++){
            String aIP=hosts.elementAt(i).mainIp;
                int exist=0;
                for(int k=0;k<IPs.size();k++){
                    if(aIP.equals(IPs.elementAt(k).getIpAddress())){
                        exist=1;
                        break;
                    }
                }
                if(exist==1)connections.add(1);
                else connections.add(0);
        }
    }
    public Integer[] getConnection(){
        Integer[] a=new Integer[1];
        Integer[] connection = (Integer[])connections.toArray(a);
        return connection;
    }
}
