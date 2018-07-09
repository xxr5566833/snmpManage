package com.example.demo;


import com.example.demo.snmpServer.Data.*;
import com.example.demo.snmpServer.SnmpServer;
import com.example.demo.snmpServer.SnmpServerCreater;
import com.example.demo.snmpServer.TrapManager;
import com.sun.javafx.collections.MappingChange;
import jdk.nashorn.internal.ir.IfNode;
import org.apache.tomcat.util.bcel.Const;
import org.snmp4j.Snmp;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.InterfaceAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;

import static com.sun.jmx.snmp.SnmpStatusException.noSuchObject;

@RestController
public class Controller {
    private static SnmpServerCreater creater = new SnmpServerCreater();

    @RequestMapping("/test")
    public InterFace[] test (){
        SnmpServer t = creater.getServer("192.168.2.2", "public");
        int vlanbeginindex = t.getVlanBegin();
        Vector<Variable> ifdescrvbs = null;
        Vector<Variable> ifindexvbs = null;
        Vector<Variable> iftypevbs = null;
        Vector<Variable> ifmtuvbs = null;
        Vector<Variable> ifspeedvbs = null;
        Vector<Variable> ifphysaddressvbs = null;
        Vector<Variable> ifadminstatusvbs = null;
        Vector<Variable> iflastchangevbs = null;
        Vector<Variable> ifopestatusvbs = null;
        Vector<Variable> ifinboundvbs = null;
        Vector<Variable> ifoutboundvbs = null;
        // 得到接口数量后，开始获取每一组信息
        try {
            OID descroid = new OID(Constant.IfDescr).append(vlanbeginindex);
            int leftlength = descroid.size() - 1;
            ifdescrvbs = t.getSubTree(descroid, leftlength);
            OID indexoid = new OID(Constant.IfIndex).append(vlanbeginindex);
            ifindexvbs = t.getSubTree(indexoid, leftlength);
            OID typeoid = new OID(Constant.IfType).append(vlanbeginindex);
            iftypevbs = t.getSubTree(typeoid, leftlength);
            OID mtuoid = new OID(Constant.IfMtu).append(vlanbeginindex);
            ifmtuvbs = t.getSubTree(mtuoid, leftlength);
            OID speedoid = new OID(Constant.IfSpeed).append(vlanbeginindex);
            ifspeedvbs = t.getSubTree(speedoid, leftlength);
            OID phyoid = new OID(Constant.IfPhysAddress).append(vlanbeginindex);
            ifphysaddressvbs = t.getSubTree(phyoid, leftlength);
            OID adminoid = new OID(Constant.IfAdminStatus).append(vlanbeginindex);
            ifadminstatusvbs = t.getSubTree(adminoid, leftlength);
            OID operoid = new OID(Constant.IfOperStatus).append(vlanbeginindex);
            ifopestatusvbs = t.getSubTree(operoid, leftlength);
            OID lastchangeoid = new OID(Constant.IfLastChange).append(vlanbeginindex);
            iflastchangevbs = t.getSubTree(lastchangeoid, leftlength);
            OID inboundoid = new OID(Constant.IfInBound).append(vlanbeginindex);
            ifinboundvbs = t.getSubTree(inboundoid, leftlength);
            OID outboundoid = new OID(Constant.IfOutBound).append(vlanbeginindex);
            ifoutboundvbs = t.getSubTree(outboundoid, leftlength);
        }catch(IOException e){
            e.printStackTrace();
        }
        InterFace[] vlans = new Vlan[ifdescrvbs.size()];
        for(int i = 0 ; i < ifdescrvbs.size() ; i++){
            InterFace inter = new Vlan();
            inter.setIndex(ifindexvbs.elementAt(i).toInt());
            inter.setIfAdminStatus(Status.values()[ifadminstatusvbs.elementAt(i).toInt()]);
            // 对于ifdescr 需要特别区分是否以Octet的String形式给出
            String descr = ifdescrvbs.elementAt(i).toString();
            // TODO: 目前给出的判断方法是看index为2的位置是否是: 如果有更充要的条件，则改之
            if(descr.charAt(2) == ':')
                inter.setIfDescr(SnmpServer.octetStr2Readable(descr));
            else{
                inter.setIfDescr(descr);
            }
            // TODO: 这里应该用时间戳类而不是String
            inter.setIfLastChange(iflastchangevbs.elementAt(i).toString());
            inter.setIfMtu(ifmtuvbs.elementAt(i).toInt());
            inter.setIfOperStatus(Status.values()[ifopestatusvbs.elementAt(i).toInt()]);
            inter.setIfPhysAddress(ifphysaddressvbs.elementAt(i).toString());
            inter.setIfSpeed(ifspeedvbs.elementAt(i).toInt());
            inter.setIfType(IFType.int2Type(iftypevbs.elementAt(i).toInt()));
            inter.setInBound(ifinboundvbs.elementAt(i).toLong());
            inter.setOutBound(ifoutboundvbs.elementAt(i).toLong());
            vlans[i] = inter;
        }
        return vlans;



    }

    @RequestMapping("/updateStatus")
    public String[] getInterfaceStatus(@RequestBody Map datamap){
        String ip = (String)datamap.get("ip");
        String community = (String)datamap.get("community");
        SnmpServer t = creater.getServer(ip, community);
        return t.getInterfacesStatus();

    }

    @RequestMapping("/getDeviceType")
    public int getDeviceType(@RequestBody Map datamap){
        DeviceType type;
        String ip = datamap.get("ip").toString();
        String community = datamap.get("community").toString();
        SnmpServer t = creater.getServer(ip, community);
        //首先判断ipForwarding
        Variable ipforwarding = null;
        try {
            ipforwarding = t.getTreeNode(Constant.IpForwarding);
        }catch(IOException e){
            e.printStackTrace();
        }
        if(ipforwarding.toInt() == 2){
            type = DeviceType.host;
        }
        else{
            // 交换机和路由器
            Variable v = null;
            try{
                v = t.getTreeNode(Constant.OnlyRouterOid);
            }catch(IOException e){
                e.printStackTrace();
            }
            // 128是NoSuchObject的对应syntax的编号
            if(v.getSyntax() == 128)
            {
                type = DeviceType.exchange;
            }
            else {
                // 路由器或者是三层交换机
                type = DeviceType.router;
            }
        }
        return type.getType();
    }
    @RequestMapping("/getInfo")
    public SysInfo getSysInfo(@RequestBody Map datamap){
        //除了 7 以外其他的都获取
        String ip = datamap.get("ip").toString();
        String community = datamap.get("community").toString();
        SnmpServer t = this.creater.getServer(ip, community);
        return t.getSysInfo();
    }
    @RequestMapping("/getVlan")
    public InterFace[] getVlans(@RequestBody Map datamap){
        SnmpServer t = creater.getServer((String)datamap.get("ip"), (String)datamap.get("community"));
        int vlanbeginindex = t.getVlanBegin();
        Vector<Variable> ifdescrvbs = null;
        Vector<Variable> ifindexvbs = null;
        Vector<Variable> iftypevbs = null;
        Vector<Variable> ifmtuvbs = null;
        Vector<Variable> ifspeedvbs = null;
        Vector<Variable> ifphysaddressvbs = null;
        Vector<Variable> ifadminstatusvbs = null;
        Vector<Variable> iflastchangevbs = null;
        Vector<Variable> ifopestatusvbs = null;
        Vector<Variable> ifinboundvbs = null;
        Vector<Variable> ifoutboundvbs = null;
        // 得到接口数量后，开始获取每一组信息
        try {
            OID descroid = new OID(Constant.IfDescr).append(vlanbeginindex);
            int leftlength = descroid.size() - 1;
            ifdescrvbs = t.getSubTree(descroid, leftlength);
            OID indexoid = new OID(Constant.IfIndex).append(vlanbeginindex);
            ifindexvbs = t.getSubTree(indexoid, leftlength);
            OID typeoid = new OID(Constant.IfType).append(vlanbeginindex);
            iftypevbs = t.getSubTree(typeoid, leftlength);
            OID mtuoid = new OID(Constant.IfMtu).append(vlanbeginindex);
            ifmtuvbs = t.getSubTree(mtuoid, leftlength);
            OID speedoid = new OID(Constant.IfSpeed).append(vlanbeginindex);
            ifspeedvbs = t.getSubTree(speedoid, leftlength);
            OID phyoid = new OID(Constant.IfPhysAddress).append(vlanbeginindex);
            ifphysaddressvbs = t.getSubTree(phyoid, leftlength);
            OID adminoid = new OID(Constant.IfAdminStatus).append(vlanbeginindex);
            ifadminstatusvbs = t.getSubTree(adminoid, leftlength);
            OID operoid = new OID(Constant.IfOperStatus).append(vlanbeginindex);
            ifopestatusvbs = t.getSubTree(operoid, leftlength);
            OID lastchangeoid = new OID(Constant.IfLastChange).append(vlanbeginindex);
            iflastchangevbs = t.getSubTree(lastchangeoid, leftlength);
            OID inboundoid = new OID(Constant.IfInBound).append(vlanbeginindex);
            ifinboundvbs = t.getSubTree(inboundoid, leftlength);
            OID outboundoid = new OID(Constant.IfOutBound).append(vlanbeginindex);
            ifoutboundvbs = t.getSubTree(outboundoid, leftlength);
        }catch(IOException e){
            e.printStackTrace();
        }
        InterFace[] vlans = new Vlan[ifdescrvbs.size()];
        for(int i = 0 ; i < ifdescrvbs.size() ; i++){
            InterFace inter = new Vlan();
            inter.setIndex(ifindexvbs.elementAt(i).toInt());
            inter.setIfAdminStatus(Status.values()[ifadminstatusvbs.elementAt(i).toInt()]);
            // 对于ifdescr 需要特别区分是否以Octet的String形式给出
            String descr = ifdescrvbs.elementAt(i).toString();
            // TODO: 目前给出的判断方法是看index为2的位置是否是: 如果有更充要的条件，则改之
            if(descr.charAt(2) == ':')
                inter.setIfDescr(SnmpServer.octetStr2Readable(descr));
            else{
                inter.setIfDescr(descr);
            }
            // TODO: 这里应该用时间戳类而不是String
            inter.setIfLastChange(iflastchangevbs.elementAt(i).toString());
            inter.setIfMtu(ifmtuvbs.elementAt(i).toInt());
            inter.setIfOperStatus(Status.values()[ifopestatusvbs.elementAt(i).toInt()]);
            inter.setIfPhysAddress(ifphysaddressvbs.elementAt(i).toString());
            inter.setIfSpeed(ifspeedvbs.elementAt(i).toInt());
            inter.setIfType(IFType.int2Type(iftypevbs.elementAt(i).toInt()));
            inter.setInBound(ifinboundvbs.elementAt(i).toLong());
            inter.setOutBound(ifoutboundvbs.elementAt(i).toLong());
            vlans[i] = inter;
        }
        return vlans;
    }
    //完成，不过速度有点慢，之后调用getBulk看能不能改善这个情况
    @RequestMapping("/getInterface")
    public InterFace[] getInterfaces(@RequestBody Map datamap){
        //获取设备的接口情况,获取oid为9以前的情况
        SnmpServer t = creater.getServer((String)datamap.get("ip"), (String)datamap.get("community"));
        int interfacenum = t.getInterfaceNum();
        InterFace[] interFaces = new InterFace[interfacenum];
        Vector<Variable> ifdescrvbs = null;
        Vector<Variable> ifindexvbs = null;
        Vector<Variable> iftypevbs = null;
        Vector<Variable> ifmtuvbs = null;
        Vector<Variable> ifspeedvbs = null;
        Vector<Variable> ifphysaddressvbs = null;
        Vector<Variable> ifadminstatusvbs = null;
        Vector<Variable> iflastchangevbs = null;
        Vector<Variable> ifopestatusvbs = null;
        Vector<Variable> ifinboundvbs = null;
        Vector<Variable> ifoutboundvbs = null;
        // 得到接口数量后，开始获取每一组信息
        try {
            ifdescrvbs = t.getSubTree(Constant.IfDescr);
            ifindexvbs = t.getSubTree(Constant.IfIndex);
            iftypevbs = t.getSubTree(Constant.IfType);
            ifmtuvbs = t.getSubTree(Constant.IfMtu);
            ifspeedvbs = t.getSubTree(Constant.IfSpeed);
            ifphysaddressvbs = t.getSubTree(Constant.IfPhysAddress);
            ifadminstatusvbs = t.getSubTree(Constant.IfAdminStatus);
            ifopestatusvbs = t.getSubTree(Constant.IfOperStatus);
            iflastchangevbs = t.getSubTree(Constant.IfLastChange);
            ifinboundvbs = t.getSubTree(Constant.IfInBound);
            ifoutboundvbs = t.getSubTree(Constant.IfOutBound);
        }catch(IOException e){
            e.printStackTrace();
        }
        for(int i = 0 ; i < interfacenum ; i++){
            InterFace inter = new InterFace();
            inter.setIndex(ifindexvbs.elementAt(i).toInt());
            inter.setIfAdminStatus(Status.values()[ifadminstatusvbs.elementAt(i).toInt()]);
            // 对于ifdescr 需要特别区分是否以Octet的String形式给出
            String descr = ifdescrvbs.elementAt(i).toString();
            // TODO: 目前给出的判断方法是看index为2的位置是否是: 如果有更充要的条件，则改之
            if(descr.charAt(2) == ':')
                inter.setIfDescr(SnmpServer.octetStr2Readable(descr));
            else{
                inter.setIfDescr(descr);
            }
            // TODO: 这里应该用时间戳类而不是String
            inter.setIfLastChange(iflastchangevbs.elementAt(i).toString());
            inter.setIfMtu(ifmtuvbs.elementAt(i).toInt());
            inter.setIfOperStatus(Status.values()[ifopestatusvbs.elementAt(i).toInt()]);
            inter.setIfPhysAddress(ifphysaddressvbs.elementAt(i).toString());
            inter.setIfSpeed(ifspeedvbs.elementAt(i).toInt());
            inter.setIfType(IFType.int2Type(iftypevbs.elementAt(i).toInt()));
            inter.setInBound(ifinboundvbs.elementAt(i).toLong());
            inter.setOutBound(ifoutboundvbs.elementAt(i).toLong());
            interFaces[i] = inter;
        }
        return interFaces;
    }

    @RequestMapping("/getNetwork")
    public IP[] getRelatedIp(@RequestBody Map datamap){
        IP[] ips = new IP[4];
        int[] oid = {1, 3, 6, 1, 2, 1, 4, 20, 1, 1};
        SnmpServer t = this.creater.getServer((String)datamap.get("ip"), (String)datamap.get("community"));
        // TODO: 目前还是不太清楚这个oid下的四个ip地址到底具体含义是什么
        Vector<Variable> s = t.getBulk(oid);
        for(int i = 0 ; i < 16 ; i++){
            //这里暂定只有四个ip，mib也没有节点指示有几个related ip
            IP ip = new IP();
            ip.setIpAddress(s.elementAt(4 * (i / 4)).toString());
            ip.setIpIfIndex(s.elementAt(4 * (i / 4) + 1).toString());
            ip.setIpNetMask(s.elementAt(4 * (i / 4) + 2).toString());
            ip.setIpMaxSize(s.elementAt(4 * (i / 4) + 3).toString());
            ips[i / 4] = ip;
            i = i + 4;
        }

        return ips;
    }

    @RequestMapping("/getRoutingTable")
    public IPRoute[] getIpRoute(@RequestBody Map datamap){
        SnmpServer t = this.creater.getServer((String)datamap.get("ip"), (String)datamap.get("community"));
        Vector<Variable> destvbs = null;
        Vector<Variable> ifindexvbs = null;
        Vector<Variable> metric1vbs = null;
        Vector<Variable> metric2vbs = null;
        Vector<Variable> metric3vbs = null;
        Vector<Variable> metric4vbs = null;
        Vector<Variable> nexthopvbs = null;
        Vector<Variable> typevbs = null;
        Vector<Variable> protovbs = null;
        Vector<Variable> agevbs = null;
        Vector<Variable> maskvbs = null;
        Vector<Variable> metric5vbs = null;
        try{
            destvbs = t.getSubTree(Constant.IpRouteDest);
            ifindexvbs = t.getSubTree(Constant.IpRouteIfIndex);
            metric1vbs = t.getSubTree(Constant.IpRouteMetric1);
            metric2vbs = t.getSubTree(Constant.IpRouteMetric2);
            metric3vbs = t.getSubTree(Constant.IpRouteMetric3);
            metric4vbs = t.getSubTree(Constant.IpRouteMetric4);
            nexthopvbs = t.getSubTree(Constant.IpRouteNextHop);
            typevbs = t.getSubTree(Constant.IpRouteType);
            protovbs = t.getSubTree(Constant.IpRouteProto);
            agevbs = t.getSubTree(Constant.IpRouteAge);
            maskvbs = t.getSubTree(Constant.IpRouteMask);
            metric5vbs = t.getSubTree(Constant.IpRouteMetric5);
        }catch(IOException e){
            e.printStackTrace();
        }
        IPRoute[] irs = new IPRoute[destvbs.size()];
        for(int i = 0 ; i < destvbs.size() ; i++){
            IPRoute ir = new IPRoute();
            ir.setIpRouteAge(agevbs.elementAt(i).toInt());
            ir.setIpRouteDest(destvbs.elementAt(i).toString());
            ir.setIpRouteIfIndex(ifindexvbs.elementAt(i).toInt());
            ir.setIpRouteMask(maskvbs.elementAt(i).toString());
            ir.setIpRouteMetric1(metric1vbs.elementAt(i).toInt());
            ir.setIpRouteMetric2(metric2vbs.elementAt(i).toInt());
            ir.setIpRouteMetric3(metric3vbs.elementAt(i).toInt());
            ir.setIpRouteMetric4(metric4vbs.elementAt(i).toInt());
            ir.setIpRouteMetric5(metric5vbs.elementAt(i).toInt());
            ir.setIpRouteNextHop(nexthopvbs.elementAt(i).toString());
            ir.setIpRouteProto(IPRouteProto.int2type(protovbs.elementAt(i).toInt()));
            ir.setIpRouteType(IPRouteType.int2Type(typevbs.elementAt(i).toInt()));
            irs[i] = ir;
        }

        return irs;
    }

    @RequestMapping("/setAdminStatus")
    public boolean setAdminStatus(@RequestBody Map datamap){
        boolean result = false;
        // Map datamap = new HashMap();
        // datamap.put("ip", "127.0.0.1");
        // datamap.put("community", "public");
        // datamap.put("index", 25);
        // datamap.put("status", 0);
        SnmpServer t = this.creater.getServer((String)datamap.get("ip"), (String)datamap.get("community"));
        result = t.setStatus((int)datamap.get("index"), (int)datamap.get("status"));
        return result;
    }

    @RequestMapping("/getTraps")
    public String getTraps(){
        return TrapManager.trapCache.elementAt(0).toString();
    }

}
