package com.example.demo.snmpServer.Data;

import java.util.Vector;

//被管理的设备，可能之后要根据服务器和交换机，路由器分子类
public class Device {
    private SysInfo sysinfo;
    //因为不同设备的接口数量不同，这里我们使用变长Vector
    private Vector<InterFace> interfaces;
    //设备数量
    private int interfaceNum;
    //与本系统关联的IP地址？？不知道这个“与本系统关联”是指？
    private DeviceType type;


    public Device(){
        this.type = DeviceType.none;
    }

    public void setType(DeviceType type) {
        this.type = type;
    }

    public DeviceType getType() {
        return type;
    }

    public SysInfo getSysinfo() {
        return sysinfo;
    }

    public void setSysinfo(SysInfo sysinfo) {
        this.sysinfo = sysinfo;
    }

    public void setInterfaceNum(int interfaceNum) {
        this.interfaceNum = interfaceNum;
    }

    public int getInterfaceNum() {
        return interfaceNum;
    }

    public Vector<InterFace> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(Vector<InterFace> interfaces) {
        this.interfaces = interfaces;
    }
}
