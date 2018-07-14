package com.example.demo.snmpServer.Data;

public class PCdata {
    public String name;
    public int[] connection;

    public void setName(String name) {
        this.name = name;
    }

    public void setConnection(Integer[] connections) {
        this.connection=new int[connections.length];
        for(int i=0;i<connections.length;i++){
            connection[i]=connections[i].intValue();
        }
    }
}
