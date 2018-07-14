package com.example.demo.snmpServer.Data;
// 存储一些常量
public class Constant {
    public static final int[]
            IpForwarding = {1, 3, 6, 1, 2, 1, 4, 1, 0},
            OnlyRouterOid = {1, 3, 6, 1, 2, 1, 17, 1, 3, 0},
            SysDescr = {1, 3, 6, 1, 2, 1, 1, 1, 0},
            SysObjectId = {1, 3, 6, 1, 2, 1, 1, 2, 0},
            SysUpTime = {1, 3, 6, 1, 2, 1, 1, 3, 0},
            SysContact = {1, 3, 6, 1, 2, 1, 1, 4, 0},
            SysName = {1, 3, 6, 1, 2, 1, 1, 5, 0},
            SysLocation = {1, 3, 6, 1, 2, 1, 1, 6, 0},

            IfNum = {1, 3, 6, 1, 2, 1, 2, 1, 0},
            IfOperStatus = {1, 3, 6, 1, 2, 1, 2, 2, 1, 8},
            IfDescr = {1, 3, 6, 1, 2, 1, 2, 2, 1, 2},
            IfIndex = {1, 3, 6, 1, 2, 1, 2, 2, 1, 1},
            IfType = {1, 3, 6, 1, 2, 1, 2, 2, 1, 3},
            IfMtu = {1, 3, 6, 1, 2, 1, 2, 2, 1, 4},
            IfSpeed = {1, 3, 6, 1, 2, 1, 2, 2, 1, 5},
            IfPhysAddress = {1, 3, 6, 1, 2, 1, 2, 2, 1, 6},
            IfAdminStatus = {1, 3, 6, 1, 2, 1, 2, 2, 1, 7},
            IfLastChange = {1, 3, 6, 1, 2, 1, 2, 2, 1, 9},
            IfInBound = {1, 3, 6, 1, 2, 1, 2, 2, 1, 10},
            IfOutBound = {1, 3, 6, 1, 2, 1, 2, 2, 1, 16},

            IpRouteDest = {1, 3, 6, 1, 2, 1, 4, 21, 1, 1},
            IpRouteIfIndex = {1, 3, 6, 1, 2, 1, 4, 21, 1, 2},
            IpRouteMetric1 = {1, 3, 6, 1, 2, 1, 4, 21, 1, 3},
            IpRouteMetric2 = {1, 3, 6, 1, 2, 1, 4, 21, 1, 4},
            IpRouteMetric3 = {1, 3, 6, 1, 2, 1, 4, 21, 1, 5},
            IpRouteMetric4 = {1, 3, 6, 1, 2, 1, 4, 21, 1, 6},
            IpRouteNextHop = {1, 3, 6, 1, 2, 1, 4, 21, 1, 7},
            IpRouteType = {1, 3, 6, 1, 2, 1, 4, 21, 1, 8},
            IpRouteProto = {1, 3, 6, 1, 2, 1, 4, 21, 1, 9},
            IpRouteAge = {1, 3, 6, 1, 2, 1, 4, 21, 1, 10},
            IpRouteMask = {1, 3, 6, 1, 2, 1, 4, 21, 1, 11},
            IpRouteMetric5 = {1, 3, 6, 1, 2, 1, 4, 21, 1, 12},

            // 获得自身所有ip
            IpAdEntAddr = {1, 3, 6, 1, 2, 1, 4, 20, 1, 1},
            IpAdEntIfAddr = {1, 3, 6, 1, 2, 1, 4, 20, 1, 2},
            IpAdEntNetmask = {1, 3, 6, 1, 2, 1, 4, 20, 1, 3},
            IpAdEntReasmMaxSize = {1, 3, 6, 1, 2, 1, 4, 20, 1, 5},

    //TCP连接远程地址
            TCPConnectionRemoteAddress = {1, 3, 6, 1, 2, 1, 6, 13, 1, 4},

    //相连设备的ip
            atNetAddress = {1, 3, 6, 1, 2, 1, 3, 1, 1, 3},

    // 获取CPU信息
            hrProcessorLoad = {1, 3, 6, 1, 2, 1, 25, 3, 3, 1, 2},
    // 获取硬盘信息
            hrStorageIndex = {1, 3, 6, 1, 2, 1, 25, 2, 3, 1, 1},
            hrStorageDescr = {1, 3, 6, 1, 2, 1, 25, 2, 3, 1, 3},
            hrStorageUnit = {1, 3, 6, 1, 2, 1, 25, 2, 3, 1, 4},
            hrStorageSize = {1, 3, 6, 1, 2, 1, 25, 2, 3, 1, 5},
            hrStorageUsed = {1, 3, 6, 1, 2, 1, 25, 2, 3, 1, 6},
    // 进程信息
            hrSWRunIndex = {1, 3, 6, 1, 2, 1, 25, 4, 2, 1, 1},
            hrSWRunName = {1, 3, 6, 1, 2, 1, 25, 4, 2, 1, 2},
            hrSWRunType = {1, 3, 6, 1, 2, 1, 25, 4, 2, 1, 6},
            hrSWRunStatus = {1, 3, 6, 1, 2, 1, 25, 4, 2, 1, 7},
            hrSWRunMemory = {1, 3, 6, 1, 2, 1, 25, 5, 1, 1, 2},
    //地址转换表
            ipNetToMediaNet = {1, 3, 6, 1, 2, 1, 4, 22, 1, 3};




}
