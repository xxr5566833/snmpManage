package com.example.demo;


import com.example.demo.Graph.Graph;
import com.example.demo.Graph.GraphCreator;
import com.example.demo.Graph.GraphData;
import com.example.demo.Graph.Node;
import com.example.demo.snmpServer.Data.*;
import com.example.demo.snmpServer.Data.Process;
import com.example.demo.snmpServer.Data.Type.DeviceType;
import com.example.demo.snmpServer.Data.Type.IFType;
import com.example.demo.snmpServer.Data.Type.Status;
import com.example.demo.snmpServer.SnmpServer;
import com.example.demo.snmpServer.SnmpServerCreater;
import com.example.demo.snmpServer.TrapManager;
import org.snmp4j.smi.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

@RestController
public class Controller {
    private static SnmpServerCreater creater = new SnmpServerCreater();

    @RequestMapping("/test")
    public InterFace[] test (){
        SnmpServer t = creater.getServer("192.168.2.2", "public","private");
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
            if(descr.substring(0, 4).equals("Vlan"))
                break;
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
        // 接下来分析每个接口所属的vlan
        if(t.getDeviceType() != DeviceType.host) {
            // 如果不是主机，那么尝试获取这个私有MIB库
            Vector<VariableBinding> vbs = new Vector<>();
            try {
                vbs = t.getSubTree(Constant.vlanPorts);
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (int i = 0; i < vbs.size(); i++) {
                Vlan vlan = new Vlan(i + 1, vbs.elementAt(i).getVariable().toString());
                Vector<Integer> ports = vlan.getPorts();
                for (int j = 0; j < ports.size(); j++) {
                    interFaces[ports.elementAt(j) - 1].setVlanIndex(i + 1);
                }
            }
        }
        return interFaces;

    }

    @RequestMapping("/getDisks")
    public Disk[] getDisks(@RequestBody Map datamap){
        String ip = (String)datamap.get("ip");
        String readcommunity = (String)datamap.get("readcommunity");
        String writecommunity = (String)datamap.get("writecommunity");
        SnmpServer t = creater.getServer(ip, readcommunity,writecommunity);
        return t.collectDisk();
    }

    @RequestMapping("/getTranslationTable")
    public AddressTranslation[] getTranslationTable(@RequestBody Map datamap){
        String ip = (String)datamap.get("ip");
        String readcommunity = (String)datamap.get("readcommunity");
        String writecommunity = (String)datamap.get("writecommunity");
        SnmpServer t = creater.getServer(ip, readcommunity,writecommunity);
        return t.getATtable();
    }

    @RequestMapping("/getProcesses")
    public Process[] getProcesses(@RequestBody Map datamap){
        String ip = (String)datamap.get("ip");
        String readcommunity = (String)datamap.get("readcommunity");
        String writecommunity = (String)datamap.get("writecommunity");
        SnmpServer t = creater.getServer(ip, readcommunity,writecommunity);
        return t.getProcesses();
    }

    @RequestMapping("/getNetGraph")
    public GraphData getNetGraph(@RequestBody String obj){
        Graph g = GraphCreator.createGraph(obj);
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
    public Vlan[] getVlans(@RequestBody Map datamap){
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
        Vector<VariableBinding> ports = null;
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
            ports = t.getSubTree(Constant.vlanPorts);
        }catch(IOException e){
            e.printStackTrace();
        }
        int vlansize = 0;
        for(int i = 0 ; i < ifdescrvbs.size() ; i++){
            if(!ifdescrvbs.elementAt(i).getVariable().toString().substring(0, 4).equals("Vlan")){
                break;
            }
            vlansize++;
        }
        Vlan[] vlans = new Vlan[vlansize];
        for(int i = 0 ; i < vlansize ; i++){
            Vlan inter = new Vlan(i + 1, ports.size() == 0 ? "null" : ports.elementAt(i).getVariable().toString());
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
        int interfacenum = ifdescrvbs.size();
        for(int i = 0 ; i < ifdescrvbs.size() ; i++){
            // 首先必须把vlan除掉
            String descr = ifdescrvbs.elementAt(i).getVariable().toString();
            if(descr.length() >= 4 && descr.substring(0, 4).equals("Vlan")){
                interfacenum = i;
                break;
            }
        }

        InterFace[] interFaces = new InterFace[interfacenum];
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
            if(descr.length() >=4 && descr.substring(0, 4).equals("Vlan"))
                break;
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
        // 接下来分析每个接口所属的vlan
        if(t.getDeviceType() != DeviceType.host) {
            // 如果不是主机，那么尝试获取这个私有MIB库
            Vector<VariableBinding> vbs = new Vector<>();
            try {
                vbs = t.getSubTree(Constant.vlanPorts);
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (int i = 0; i < vbs.size(); i++) {
                Vlan vlan = new Vlan(i + 1, vbs.elementAt(i).getVariable().toString());
                Vector<Integer> ports = vlan.getPorts();
                for (int j = 0; j < ports.size(); j++) {
                    interFaces[ports.elementAt(j) - 1].setVlanIndex(i + 1);
                }
            }
        }
        return interFaces;
    }


    @RequestMapping("/getRoutingTable")
    public IPRoute[] getIpRoute(@RequestBody Map datamap) {
        String ip = datamap.get("ip").toString();
        String readcommunity = (String) datamap.get("readcommunity");
        String writecommunity = (String) datamap.get("writecommunity");
        SnmpServer t = SnmpServerCreater.getServer(ip, readcommunity, writecommunity);
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
        SnmpServer t = SnmpServerCreater.getServer(ip, readcommunity,writecommunity);
        result = t.setStatus((int)datamap.get("index"), (int)datamap.get("status"));
        return result;
    }

    @RequestMapping("/getTraps")
    public String getTraps(){
        return TrapManager.trapCache.elementAt(0).toString();
    }

    @RequestMapping("/getTCP")
    public PCdata[] getTCP(){
        Graph g = GraphCreator.createGraph("127.0.0.1");
        Vector<Node> hosts=g.getHosts();
        int hostsLength=hosts.size();
        PCdata[] PCs=new PCdata[hostsLength];
        for(int j=0;j<hostsLength;j++){
            PCdata PCA=new PCdata();
            String name=hosts.elementAt(j).getName();
            String IP=hosts.elementAt(j).getMainIp();
            SnmpServer t = SnmpServerCreater.getServer(IP,"public","private");
            PC onePC=new PC(name,IP,hosts,t.getTCPconnection());
            PCA.setName(name);
            PCA.setConnection(onePC.getConnection());
            PCs[j]=PCA;
        }
        return PCs;
    }

    @RequestMapping("/getFlow")
    public HashMap getFlow(@RequestBody Map datamap){
        String ip = datamap.get("ip").toString();
        String readcommunity = (String) datamap.get("readcommunity");
        String writecommunity = (String) datamap.get("writecommunity");
        int index = (int)datamap.get("index");
        HashMap<String, Object> map = new HashMap<String, Object>();
        SnmpServer t = SnmpServerCreater.getServer(ip, readcommunity,writecommunity);
        if(index == -1){
            int insum = t.getInBound();
            int outsum = t.getOutBound();
            map.put("inBound", (double)insum / (1024.0 * 1024.0));
            map.put("outBound", (double)outsum / (1024.0 * 1024.0));
        }
        else{
            int in = t.getInBound(index);
            int out = t.getOutBound(index);
            map.put("inBound", (double)in / (1024.0 * 1024.0));
            map.put("outBound", (double)out / (1024.0 * 1024.0));
        }
        return map;
    }
    @RequestMapping("/login")
    public int login(@RequestBody Map datamap){
        if(datamap.get("phoneNumber").equals("13121270825")){
            if(datamap.get("password").equals("abcd"))
                return 2;
            else return 1;
        }
        else return 0;
    }
}
