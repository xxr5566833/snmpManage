package com.example.demo.snmpServer.Data;

import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.Variable;

public class SysInfo {
    private String sysDescr;
    private String sysObjectId;
    private Variable sysUpTime;
    private String sysContact;
    private String sysName;
    private String sysLocation;
    private int sysCpuUsedRate;

    public int getSysCpuUsedRate() {
        return sysCpuUsedRate;
    }

    public void setSysCpuUsedRate(int sysCpuUsedRate) {
        this.sysCpuUsedRate = sysCpuUsedRate;
    }

    public String getSysDescr(){
        return this.sysDescr;
    }
    public void setSysDescr(String des){
        this.sysDescr = des;
    }
    public String getSysObjectId(){
        return this.sysObjectId;
    }
    public void setSysObjectId(String id){
        this.sysObjectId = id;
    }

    public Variable getSysUpTime(){
        return this.sysUpTime;
    }
    public void setSysUpTime(Variable time){
        //对时间处理一下，表达更清晰
        /*String t = new String();
        String[] timelist = time.split(":");
        //鉴于时间是固定的 xx时xx分xx.xx秒
        String[] unit = {"小时", "分钟", "秒"};
        for(int i = 0 ; i < timelist.length ; i++){
            t = t + timelist[i] + unit[i];
        }*/
        //学长写了处理函数，这里直接传入吧
        this.sysUpTime = time;
    }
    public String getSysContact(){
        return this.sysContact;
    }
    public void setSysContact(String contact){
        this.sysContact = contact;
    }

    public String getSysName(){
        return this.sysName;
    }
    public void setSysName(String name){
        this.sysName = name;
    }

    public String getSysLocation(){
        return this.sysLocation;
    }
    public void setSysLocation(String location){
        this.sysLocation = location;
    }
}
