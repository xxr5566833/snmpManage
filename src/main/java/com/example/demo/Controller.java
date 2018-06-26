package com.example.demo;


import com.example.demo.snmpServer.Data.*;
import com.example.demo.snmpServer.SnmpServer;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.VariableBinding;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.bind.DatatypeConverter;
import java.net.InterfaceAddress;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class Controller {
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping("/test")
    public String[] test (@RequestParam(value = "name", defaultValue = "World")  String name){
        SnmpServer t = new SnmpServer("127.0.0.1", 161);
        int[] oid = {1, 3, 6, 1, 2, 1, 4, 21, 1, 2};
        Vector<? extends VariableBinding> vbs = t.walkVB(oid, false);

        String[] v = t.walkInfo(oid, false);
        for(int i = 0 ; i < v.length ; i++)
            System.out.println(v[i]);
        return v;
    }

    @RequestMapping("/sys")
    public SysInfo getSysInfo(){
        //除了 7 以外其他的都获取
        SnmpServer t = new SnmpServer("127.0.0.1", 161);
        int[] oid = {1, 3, 6, 1, 2, 1, 1, 1, 0};
        SysInfo sys = new SysInfo();
        String[] s = t.getInfo(oid, false);
        sys.setSysDescr(s[0]);
        oid[7] += 1;
        s = t.getInfo(oid, false);
        sys.setSysObjectId(s[0]);
        oid[7] += 1;
        s = t.getInfo(oid, false);
        sys.setSysUpTime(s[0]);
        oid[7] += 1;
        s = t.getInfo(oid, false);
        sys.setSysContact(s[0]);
        oid[7] += 1;
        s = t.getInfo(oid, false);
        sys.setSysName(s[0]);
        oid[7] += 1;
        s = t.getInfo(oid, false);
        sys.setSysLocation(s[0]);
        return sys;
    }
    //完成，不过速度有点慢，之后调用getBulk看能不能改善这个情况
    @RequestMapping("/interface")
    public InterFace[] getInterfaces(){
        //获取设备的接口情况,获取oid为9以前的情况
        Vector<InterFace> interFaces = new Vector<InterFace>();
        int interfacenum = 0;
        int[] oid = {1, 3, 6, 1, 2, 1, 2, 1, 0};
        SnmpServer t = new SnmpServer("127.0.0.1", 161);
        String[] s = t.getInfo(oid, false);
        interfacenum = Integer.parseInt(s[0]);
        int[] newoid = {1, 3, 6, 1, 2, 1, 2, 2, 1, 1, 0};
        for(int i = 0 ; i < interfacenum ; i++){
            //每个接口每个属性取信息
            newoid[10] += 1;
            newoid[9] = 1;
            InterFace inter = new InterFace();
            //ifDescr
            newoid[9] += 1;
            s = t.getInfo(newoid, true);
            inter.setIfDescr(s[0]);
            //ifType
            newoid[9] += 1;
            s = t.getInfo(newoid, false);
            inter.setIfType(IFType.int2Type(Integer.parseInt(s[0])));
            //ifMtu
            newoid[9] += 1;
            s = t.getInfo(newoid, false);
            inter.setIfMtu(Integer.parseInt(s[0]));
            //ifSpeed
            newoid[9] += 1;
            s = t.getInfo(newoid, false);
            inter.setIfSpeed(Integer.parseInt(s[0]));
            //ifPhysAddress
            newoid[9] += 1;
            s = t.getInfo(newoid, false);
            System.out.println(s[0]);
            inter.setIfPhysAddress(s[0]);
            //ifAdminStatus
            newoid[9] += 1;
            s = t.getInfo(newoid, false);
            inter.setIfAdminStatus(Status.values()[Integer.parseInt(s[0])]);
            //ifOperStatus
            newoid[9] += 1;
            s = t.getInfo(newoid, false);
            inter.setIfOperStatus(Status.values()[Integer.parseInt(s[0])]);
            //ifLastChange
            newoid[9] += 1;
            s = t.getInfo(newoid, false);
            inter.setIfLastChange(s[0]);
            interFaces.add(inter);
        }

        InterFace[] inters = interFaces.toArray(new InterFace[interFaces.size()]);
        return inters;
    }

    @RequestMapping("/relatedip")
    public IP[] getRelatedIp(){
        IP[] ips = new IP[4];
        int[] oid = {1, 3, 6, 1, 2, 1, 4, 20, 1, 1};
        SnmpServer t = new SnmpServer("127.0.0.1", 161);
        String[] s = t.walkInfo(oid, false);
        for(int i = 0 ; i < 16 ; i++){
            //这里暂定只有四个ip，mib也没有节点指示有几个related ip
            IP ip = new IP();
            ip.setIpAddress(s[4 * (i / 4)]);
            ip.setIpIfIndex(s[4 * (i / 4) + 1]);
            ip.setIpNetMask(s[4 * (i / 4) + 2]);
            ip.setIpMaxSize(s[4 * (i / 4) + 3]);
            ips[i / 4] = ip;
            i = i + 4;
        }
        return ips;
    }
    //终于完成了，但是感觉代码重复太多了，需要重构
    @RequestMapping("/routingtable")
    public IPRoute[] getIpRoute(){
        SnmpServer t = new SnmpServer("127.0.0.1", 161);

        int count = 0;
        int oid[] = {1, 3, 6, 1, 2, 1, 4, 21, 1, 1};
        Vector<? extends VariableBinding> vips = t.walkVB(oid, false);
        String first = new String();
        for(int i = 0 ; i < vips.size() ; i++){
            OID tempoid = vips.elementAt(i).getOid();
            if(i == 0)
                first = tempoid.toString();
            if(i != 0 ){
                tempoid.set(9, 1);
                if(first.equals(tempoid.toString())) {
                    count = i;
                    break;
                }
            }
        }


        //上面获得了路由表的长度，接下来根据长度设置路由表项
        //首先初始化每个项
        IPRoute[] routes = new IPRoute[count];
        for(int i = 0 ; i < count ; i++){
            routes[i] = new IPRoute();
        }
        String[] v = t.walkInfo(oid, false);
        for(int i = 0 ; i < count ; i++){
            routes[i].setIpRouteDest(v[i]);
        }
        //然后是ifindex
        oid[9] += 1;
        v = t.walkInfo(oid, false);
        for(int i = 0 ; i < count ; i++){
            routes[i].setIpRouteIfIndex(v[i]);
        }
        //Metric1
        oid[9] += 1;
        v = t.walkInfo(oid, false);
        for(int i = 0 ; i < count ; i++){
            routes[i].setIpRouteMetric1(Integer.parseInt(v[i]));
        }
        //Metric2
        oid[9] += 1;
        v = t.walkInfo(oid, false);
        for(int i = 0 ; i < count ; i++){
            routes[i].setIpRouteMetric2(Integer.parseInt(v[i]));
        }
        //Metric3
        oid[9] += 1;
        v = t.walkInfo(oid, false);
        for(int i = 0 ; i < count ; i++){
            routes[i].setIpRouteMetric3(Integer.parseInt(v[i]));
        }
        //Metric4
        oid[9] += 1;
        v = t.walkInfo(oid, false);
        for(int i = 0 ; i < count ; i++){
            routes[i].setIpRouteMetric4(Integer.parseInt(v[i]));
        }
        //NextHop
        oid[9] += 1;
        v = t.walkInfo(oid, false);
        for(int i = 0 ; i < count ; i++){
            routes[i].setIpRouteNextHop(v[i]);
        }
        //RouteType
        oid[9] += 1;
        v = t.walkInfo(oid, false);
        for(int i = 0 ; i < count ; i++){
            routes[i].setIpRouteType(IPRouteType.int2Type(Integer.parseInt(v[i])));
        }

        //RouteProto
        oid[9] += 1;
        v = t.walkInfo(oid, false);
        for(int i = 0 ; i < count ; i++){
            routes[i].setIpRouteProto(IPRouteProto.int2type(Integer.parseInt(v[i])));
        }
        //RouteAge
        oid[9] += 1;
        v = t.walkInfo(oid, false);
        for(int i = 0 ; i < count ; i++){
            routes[i].setIpRouteAge(Integer.parseInt(v[i]));
        }
        //routeMask
        oid[9] += 1;
        v = t.walkInfo(oid, false);
        for(int i = 0 ; i < count ; i++){
            routes[i].setIpRouteMask(v[i]);
        }
        //RouteMatric5
        oid[9] += 1;
        v = t.walkInfo(oid, false);
        for(int i = 0 ; i < count ; i++){
            routes[i].setIpRouteMetric5(Integer.parseInt(v[i]));
        }
        return routes;
    }


}
