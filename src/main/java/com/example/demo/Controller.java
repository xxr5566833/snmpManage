package com.example.demo;


import com.example.demo.Graph.Graph;
import com.example.demo.Graph.GraphCreator;
import com.example.demo.Graph.GraphData;
import com.example.demo.snmpServer.Data.*;
import com.example.demo.snmpServer.SnmpServer;
import com.example.demo.snmpServer.SnmpServerCreater;
import com.example.demo.snmpServer.TrapManager;
import com.sun.javafx.collections.MappingChange;
import jdk.nashorn.internal.ir.IfNode;
import org.apache.tomcat.util.bcel.Const;
import org.snmp4j.Snmp;
import org.snmp4j.smi.*;
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
    public int test (){
        SnmpServer t = creater.getServer("127.0.0.1", "public","private");
        return t.collectCPU();
    }

    @RequestMapping("/getNetGraph")
    public GraphData getNetGraph(){
        Graph g = GraphCreator.createGraph("127.0.0.1");
        return g.toData();
    }

    @RequestMapping("/updateStatus")
    public String[] getInterfaceStatus(@RequestBody Map datamap){
        String ip = (String)datamap.get("ip");
        String readcommunity = (String)datamap.get("readcommunity");
        String writecommunity = (String)datamap.get("writecommunity");
        SnmpServer t = creater.getServer(ip, readcommunity,writecommunity);
        return t.getInterfacesStatus();

    }

    @RequestMapping("/getDeviceType")
    public int getDeviceType(@RequestBody Map datamap){
        String ip = datamap.get("ip").toString();
        String readcommunity = (String)datamap.get("readcommunity");
        String writecommunity = (String)datamap.get("writecommunity");
        SnmpServer t = creater.getServer(ip, readcommunity,writecommunity);
        return t.getDeviceType().getType();
    }
    @RequestMapping("/getInfo")
    public SysInfo getSysInfo(@RequestBody Map datamap){
        //除了 7 以外其他的都获取
        String ip = datamap.get("ip").toString();
        String readcommunity = (String)datamap.get("readcommunity");
        String writecommunity = (String)datamap.get("writecommunity");
        SnmpServer t = creater.getServer(ip, readcommunity,writecommunity);
        return t.getSysInfo();
    }
    @RequestMapping("/getVlan")
    public InterFace[] getVlans(@RequestBody Map datamap){
        String ip = datamap.get("ip").toString();
        String readcommunity = (String)datamap.get("readcommunity");
        String writecommunity = (String)datamap.get("writecommunity");
        SnmpServer t = creater.getServer(ip, readcommunity,writecommunity);
        int vlanbeginindex = t.getVlanBegin();
        Vector<VariableBinding> ifdescrvbs = null;
        Vector<VariableBinding> ifindexvbs = null;
        Vector<VariableBinding> iftypevbs = null;
        Vector<VariableBinding> ifmtuvbs = null;
        Vector<VariableBinding> ifspeedvbs = null;
        Vector<VariableBinding> ifphysaddressvbs = null;
        Vector<VariableBinding> ifadminstatusvbs = null;
        Vector<VariableBinding> iflastchangevbs = null;
        Vector<VariableBinding> ifopestatusvbs = null;
        Vector<VariableBinding> ifinboundvbs = null;
        Vector<VariableBinding> ifoutboundvbs = null;
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
            inter.setIndex(ifindexvbs.elementAt(i).getVariable().toInt());
            inter.setIfAdminStatus(Status.values()[ifadminstatusvbs.elementAt(i).getVariable().toInt()]);
            // 对于ifdescr 需要特别区分是否以Octet的String形式给出
            String descr = ifdescrvbs.elementAt(i).getVariable().toString();
            // TODO: 目前给出的判断方法是看index为2的位置是否是: 如果有更充要的条件，则改之
            if(descr.charAt(2) == ':')
                inter.setIfDescr(SnmpServer.octetStr2Readable(descr));
            else{
                inter.setIfDescr(descr);
            }
            // TODO: 这里应该用时间戳类而不是String
            inter.setIfLastChange(iflastchangevbs.elementAt(i).getVariable().toString());
            inter.setIfMtu(ifmtuvbs.elementAt(i).getVariable().toInt());
            inter.setIfOperStatus(Status.values()[ifopestatusvbs.elementAt(i).getVariable().toInt()]);
            inter.setIfPhysAddress(ifphysaddressvbs.elementAt(i).getVariable().toString());
            inter.setIfSpeed(ifspeedvbs.elementAt(i).getVariable().toInt());
            inter.setIfType(IFType.int2Type(iftypevbs.elementAt(i).getVariable().toInt()));
            inter.setInBound(ifinboundvbs.elementAt(i).getVariable().toLong());
            inter.setOutBound(ifoutboundvbs.elementAt(i).getVariable().toLong());
            vlans[i] = inter;
        }
        return vlans;
    }
    //完成，不过速度有点慢，之后调用getBulk看能不能改善这个情况
    @RequestMapping("/getInterface")
    public InterFace[] getInterfaces(@RequestBody Map datamap){
        //获取设备的接口情况,获取oid为9以前的情况
        String ip = datamap.get("ip").toString();
        String readcommunity = (String)datamap.get("readcommunity");
        String writecommunity = (String)datamap.get("writecommunity");
        SnmpServer t = creater.getServer(ip, readcommunity,writecommunity);
        int interfacenum = t.getInterfaceNum();
        InterFace[] interFaces = new InterFace[interfacenum];
        Vector<VariableBinding> ifdescrvbs = null;
        Vector<VariableBinding> ifindexvbs = null;
        Vector<VariableBinding> iftypevbs = null;
        Vector<VariableBinding> ifmtuvbs = null;
        Vector<VariableBinding> ifspeedvbs = null;
        Vector<VariableBinding> ifphysaddressvbs = null;
        Vector<VariableBinding> ifadminstatusvbs = null;
        Vector<VariableBinding> iflastchangevbs = null;
        Vector<VariableBinding> ifopestatusvbs = null;
        Vector<VariableBinding> ifinboundvbs = null;
        Vector<VariableBinding> ifoutboundvbs = null;
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
            inter.setIndex(ifindexvbs.elementAt(i).getVariable().toInt());
            inter.setIfAdminStatus(Status.values()[ifadminstatusvbs.elementAt(i).getVariable().toInt()]);
            // 对于ifdescr 需要特别区分是否以Octet的String形式给出
            String descr = ifdescrvbs.elementAt(i).getVariable().toString();
            // TODO: 目前给出的判断方法是看index为2的位置是否是: 如果有更充要的条件，则改之
            if(descr.charAt(2) == ':')
                inter.setIfDescr(SnmpServer.octetStr2Readable(descr));
            else{
                inter.setIfDescr(descr);
            }
            // TODO: 这里应该用时间戳类而不是String
            inter.setIfLastChange(iflastchangevbs.elementAt(i).getVariable().toString());
            inter.setIfMtu(ifmtuvbs.elementAt(i).getVariable().toInt());
            inter.setIfOperStatus(Status.values()[ifopestatusvbs.elementAt(i).getVariable().toInt()]);
            inter.setIfPhysAddress(ifphysaddressvbs.elementAt(i).getVariable().toString());
            inter.setIfSpeed(ifspeedvbs.elementAt(i).getVariable().toInt());
            inter.setIfType(IFType.int2Type(iftypevbs.elementAt(i).getVariable().toInt()));
            // 有的接口没有实现inbound和outbound
            if(inter.getIfDescr().length() >= 8 && inter.getIfDescr().substring(0, 8).equals("Cellular")){
                ifinboundvbs.add(new VariableBinding(iftypevbs.elementAt(i).getOid(), new Integer32(0)));
                ifoutboundvbs.add(new VariableBinding(iftypevbs.elementAt(i).getOid(), new Integer32(0)));
            }
            inter.setInBound(ifinboundvbs.elementAt(i).getVariable().toLong());
            inter.setOutBound(ifoutboundvbs.elementAt(i).getVariable().toLong());
            interFaces[i] = inter;
        }
        // 有的接口没有inbound 比如Cellular0/0，此时就单独赋值
        return interFaces;
    }


    @RequestMapping("/getRoutingTable")
    public IPRoute[] getIpRoute(@RequestBody Map datamap) {
        String ip = datamap.get("ip").toString();
        String readcommunity = (String) datamap.get("readcommunity");
        String writecommunity = (String) datamap.get("writecommunity");
        SnmpServer t = creater.getServer(ip, readcommunity, writecommunity);
        return t.getIpRoute();
    }
    @RequestMapping("/setAdminStatus")
    public boolean setAdminStatus(@RequestBody Map datamap){
        boolean result = false;
        System.out.println("changing interface");
        // Map datamap = new HashMap();
        // datamap.put("ip", "127.0.0.1");
        // datamap.put("community", "public");
        // datamap.put("index", 25);
        // datamap.put("status", 0);
        String ip = datamap.get("ip").toString();
        String readcommunity = (String)datamap.get("readcommunity");
        String writecommunity = (String)datamap.get("writecommunity");
        SnmpServer t = creater.getServer(ip, readcommunity,writecommunity);
        result = t.setStatus((int)datamap.get("index"), (int)datamap.get("status"));
        return result;
    }

    @RequestMapping("/getTraps")
    public String getTraps(){
        return TrapManager.trapCache.elementAt(0).toString();
    }



}
