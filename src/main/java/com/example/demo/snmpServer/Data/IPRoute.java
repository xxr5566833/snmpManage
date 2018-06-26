package com.example.demo.snmpServer.Data;

public class IPRoute {
    private String ipRouteDest;
    private String ipRouteIfIndex;
    private int ipRouteMetric1;
    private int ipRouteMetric2;
    private int ipRouteMetric3;
    private int ipRouteMetric4;
    private int ipRouteMetric5;
    private String ipRouteNextHop;
    private IPRouteType ipRouteType;
    private IPRouteProto ipRouteProto;
    private int ipRouteAge;
    private String ipRouteMask;

    public void setIpRouteDest(String ipRouteDest) {
        this.ipRouteDest = ipRouteDest;
    }

    public String getIpRouteDest() {
        return ipRouteDest;
    }

    public void setIpRouteAge(int ipRouteAge) {
        this.ipRouteAge = ipRouteAge;
    }

    public int getIpRouteAge() {
        return ipRouteAge;
    }

    public void setIpRouteIfIndex(String ipRouteIfIndex) {
        this.ipRouteIfIndex = ipRouteIfIndex;
    }

    public String getIpRouteIfIndex() {
        return ipRouteIfIndex;
    }

    public void setIpRouteMetric1(int ipRouteMetric1) {
        this.ipRouteMetric1 = ipRouteMetric1;
    }

    public int getIpRouteMetric1() {
        return ipRouteMetric1;
    }

    public void setIpRouteMetric2(int ipRouteMetric2) {
        this.ipRouteMetric2 = ipRouteMetric2;
    }

    public int getIpRouteMetric2() {
        return ipRouteMetric2;
    }

    public void setIpRouteMetric3(int ipRouteMetric3) {
        this.ipRouteMetric3 = ipRouteMetric3;
    }

    public int getIpRouteMetric3() {
        return ipRouteMetric3;
    }

    public void setIpRouteMetric4(int ipRouteMetric4) {
        this.ipRouteMetric4 = ipRouteMetric4;
    }

    public int getIpRouteMetric4() {
        return ipRouteMetric4;
    }

    public void setIpRouteMetric5(int ipRouteMetric5) {
        this.ipRouteMetric5 = ipRouteMetric5;
    }

    public int getIpRouteMetric5() {
        return ipRouteMetric5;
    }

    public void setIpRouteNextHop(String ipRouteNextHop) {
        this.ipRouteNextHop = ipRouteNextHop;
    }

    public String getIpRouteNextHop() {
        return ipRouteNextHop;
    }

    public void setIpRouteProto(IPRouteProto ipRouteProto) {
        this.ipRouteProto = ipRouteProto;
    }

    public IPRouteProto getIpRouteProto() {
        return ipRouteProto;
    }

    public void setIpRouteType(IPRouteType ipRouteType) {
        this.ipRouteType = ipRouteType;
    }

    public IPRouteType getIpRouteType() {
        return ipRouteType;
    }

    public void setIpRouteMask(String ipRouteMask) {
        this.ipRouteMask = ipRouteMask;
    }

    public String getIpRouteMask() {
        return ipRouteMask;
    }
}
