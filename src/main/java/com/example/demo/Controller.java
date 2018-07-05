package com.example.demo;


import com.example.demo.snmpServer.Data.*;
import com.example.demo.snmpServer.SnmpServer;
import com.example.demo.snmpServer.SnmpServerCreater;
import com.example.demo.snmpServer.TrapManager;
import com.sun.javafx.collections.MappingChange;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.VariableBinding;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;
import java.net.InterfaceAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class Controller {
    private final AtomicLong counter = new AtomicLong();
    private static SnmpServerCreater creater = new SnmpServerCreater();

    @RequestMapping("/test")
    public String[] test (){
        SnmpServer t = creater.getServer("127.0.0.1", "public");
        int[] oid = {1, 3, 6, 1, 2, 1, 1, 1, 0};
        // Vector<? extends VariableBinding> vbs = t.walkVB(oid, false);

        String[] v = t.getInfo(oid, false);
        for(int i = 0 ; i < v.length ; i++)
            System.out.println(v[i]);

        /*int size = TrapManager.trapCache.size();
        String v[] = new String[TrapManager.trapCache.size()];
        for(int i = 0 ; i < size ; i++){
            v[i] = TrapManager.trapCache.elementAt(i).toString();
        }*/

        return v;
    }

    @RequestMapping("/updateStatus")
    public String[] getInterfaceStatus(@RequestBody Map datamap){

        int interfacenum = 0;
        int[] oid = {1, 3, 6, 1, 2, 1, 2, 1, 0};
        String ip = (String)datamap.get("ip");
        String community = (String)datamap.get("community");
        SnmpServer t = creater.getServer(ip, community);

        String[] s = t.getInfo(oid, false);
        interfacenum = Integer.parseInt(s[0]);
        int[] newoid = {1, 3, 6, 1, 2, 1, 2, 2, 2, 1, 8};
        s = t.walkInfo(newoid, false);
        String[] result = new String[interfacenum];
        for(int i = 0 ; i < interfacenum ; i++){
            int type = Integer.parseInt(s[i]);
            result[i] = (type == 1 ? "UP" : "DOWN");
        }

        return result;

    }

    @RequestMapping("/getDeviceType")
    public int getDeviceType(@RequestBody Map datamap){
        DeviceType type;
        String ip = datamap.get("ip").toString();
        String community = datamap.get("community").toString();
        SnmpServer t = creater.getServer(ip, community);
        //首先判断ipForwarding
        int oid1[] = {1, 3, 6, 1, 2, 1, 4, 1, 0};
        String[] s = t.getInfo(oid1, false);
        if(Integer.parseInt(s[0]) == 2){
            type = DeviceType.host;
        }
        else{
            // 交换机和路由器
            int oid2[] = {1, 3, 6, 1, 2, 1, 17, 1, 3, 0};
            s = t.getInfo(oid2, false);
            if(s != null)
            {
                type = DeviceType.exchange;
            }
            else
                type = DeviceType.router;
        }
        System.out.println(type.getType());
        return type.getType();
    }
    //TODO：直接把类型都设成Variable就完事了，以后做
    @RequestMapping("/getInfo")
    public SysInfo getSysInfo(@RequestBody Map datamap){
        //除了 7 以外其他的都获取
        String ip = datamap.get("ip").toString();
        String community = datamap.get("community").toString();
        System.out.println(ip);
        System.out.println(community);
        SnmpServer t = this.creater.getServer(ip, community);
        int[] oid = {1, 3, 6, 1, 2, 1, 1, 1, 0};
        SysInfo sys = new SysInfo();
        String[] s = t.getInfo(oid, false);
        sys.setSysDescr(s[0]);
        oid[7] += 1;
        s = t.getInfo(oid, false);
        sys.setSysObjectId(s[0]);
        oid[7] += 1;
        // 这里不能得到String，因为不能从STring转到TimeTicks
        VariableBinding tmp = t.getVB(oid, false);
        sys.setSysUpTime(tmp.getVariable());
        System.out.println(tmp);
        oid[7] += 1;
        s = t.getInfo(oid, false);
        sys.setSysContact(s[0]);
        oid[7] += 1;
        s = t.getInfo(oid, false);
        sys.setSysName(s[0]);
        oid[7] += 1;
        s = t.getInfo(oid, false);
        sys.setSysLocation(s[0]);
        System.out.println(sys.getSysUpTime());
        return sys;
    }

    //完成，不过速度有点慢，之后调用getBulk看能不能改善这个情况
    @RequestMapping("/getInterface")
    public InterFace[] getInterfaces(@RequestBody Map datamap){
        //获取设备的接口情况,获取oid为9以前的情况

        Vector<InterFace> interFaces = new Vector<InterFace>();
        int interfacenum = 0;
        int[] oid = {1, 3, 6, 1, 2, 1, 2, 1, 0};
        SnmpServer t = this.creater.getServer((String)datamap.get("ip"), (String)datamap.get("community"));
        String[] s = t.getInfo(oid, false);
        interfacenum = Integer.parseInt(s[0]);
        int[] newoid = {1, 3, 6, 1, 2, 1, 2, 2, 1, 1, 0};
        for(int i = 0 ; i < interfacenum ; i++){
            //每个接口每个属性取信息
            newoid[10] += 1;
            newoid[9] = 1;
            InterFace inter = new InterFace();
            inter.setIndex(newoid[10]);
            //ifDescr
            newoid[9] += 1;
            s = t.getInfo(newoid, true);
            inter.setIfDescr(s[0]);
            //ifType
            newoid[9] += 1;
            s = t.getInfo(newoid, false);
            // 这里因为要传出去，所以传int更合适而不是枚举
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
            //inBound
            newoid[9] = 10;
            s = t.getInfo(newoid, false);
            inter.setInBound(Integer.parseInt(s[0]));
            //outBound
            newoid[9] = 16;
            s = t.getInfo(newoid, false);
            inter.setOutBound(Integer.parseInt(s[0]));
            interFaces.add(inter);
        }

        InterFace[] inters = interFaces.toArray(new InterFace[interFaces.size()]);
        return inters;
    }

    @RequestMapping("/getNetwork")
    public IP[] getRelatedIp(@RequestBody Map datamap){
        IP[] ips = new IP[4];
        int[] oid = {1, 3, 6, 1, 2, 1, 4, 20, 1, 1};
        SnmpServer t = this.creater.getServer((String)datamap.get("ip"), (String)datamap.get("community"));
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
    @RequestMapping("/getRoutingTable")
    public IPRoute[] getIpRoute(@RequestBody Map datamap){
        SnmpServer t = this.creater.getServer((String)datamap.get("ip"), (String)datamap.get("community"));

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

    @RequestMapping("/setAdminStatus")
    public boolean setAdminStatus(@RequestBody Map datamap){
        boolean result = false;
        /*Map datamap = new HashMap();
        datamap.put("ip", "127.0.0.1");
        datamap.put("community", "public");
        datamap.put("index", 25);
        datamap.put("status", 0);*/
        SnmpServer t = this.creater.getServer((String)datamap.get("ip"), (String)datamap.get("community"));
        int oid[] = {1, 3, 6, 1, 2, 1, 2, 2, 1, 7, (int)datamap.get("index")};
        result = t.setStatus(oid, (int)datamap.get("status"));
        return result;
    }

    @RequestMapping("/getTraps")
    public String getTraps(){
        return TrapManager.trapCache.elementAt(0).toString();
    }

}
