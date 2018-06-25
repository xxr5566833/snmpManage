package com.example.demo;

public class SysInfo {
    private String sysDescr;
    private String sysObjectId;
    private String sysUpTime;
    private String sysContact;
    private String sysName;
    private String sysLocation;

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

    public String getSysUpTime(){
        return this.sysUpTime;
    }
    public void setSysUpTime(String time){
        //对时间处理一下，表达更清晰
        String t = new String();
        String[] timelist = time.split(":");
        //鉴于时间是固定的 xx时xx分xx.xx秒
        String[] unit = {"小时", "分钟", "秒"};
        for(int i = 0 ; i < timelist.length ; i++){
            t = t + timelist[i] + unit[i];
        }
        this.sysUpTime = t;
    }


}
