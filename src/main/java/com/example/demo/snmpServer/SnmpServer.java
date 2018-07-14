package com.example.demo.snmpServer;

import java.io.IOException;
import java.util.Vector;

import com.example.demo.snmpServer.Data.*;
import com.example.demo.snmpServer.Data.Process;
import com.example.demo.snmpServer.Data.Type.*;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class SnmpServer{
    private Snmp snmp = null;
    private Address targetAddress = null;
    private String readcommunity;
    private String writecommunity;
    private TransportMapping trapTransport;
    private Snmp trapSnmp;
    private Device device;
    private String ip;
    public void initTrapListen(String ip, int port){
        // 设置接收trap的IP和端口
        try {
            this.trapTransport = new DefaultUdpTransportMapping(new UdpAddress(
                    "127.0.0.1/" + port));
        }catch(Exception e){
            e.printStackTrace();
        }
        trapSnmp = new Snmp(this.trapTransport);
        CommandResponder trapRec = new CommandResponder() {
            public synchronized void processPdu(CommandResponderEvent e) {
                // 接收trap
                PDU command = e.getPDU();
                System.out.println("收到！");

                if (command != null) {
                    System.out.println(command.toString());
                    TrapManager.trapCache.add(command);
                    // 你可以在这里添加Trap提示
                }
            }
        };
        trapSnmp.addCommandResponder(trapRec);
        try {
            this.trapTransport.listen();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public synchronized void listen() {
        System.out.println("Waiting for traps..");
        try {
            this.wait();//Wait for traps to come in
        } catch (InterruptedException ex) {
            System.out.println("Interrupted while waiting for traps: " + ex);
            System.exit(-1);
        }
    }
    public void run(){
        this.listen();
    }

    public SnmpServer(String ip){
        this(ip, 161, "public", "private");
    }

    public SnmpServer(String ip, int port, String readcommunity, String writecommunity) {
        // 设置Agent方的IP和端口
        this.ip = ip;
        targetAddress = GenericAddress.parse("udp:"+ ip + "/" + port);
        this.readcommunity = readcommunity;
        this.writecommunity = writecommunity;
        this.device = new Device();
        try {
            TransportMapping transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            transport.listen();
        }
        catch(IOException e){
            e.printStackTrace();
        }
        // 默认trap监听162
        //this.initTrapListen(ip, 162);
    }
    public Vector<VariableBinding> getSubTree(int[] oid) throws IOException{
        OID newoid = new OID(oid);
        return this.getSubTree(newoid, newoid.size());
    }

    public Vector<VariableBinding> getSubTree(OID newoid) throws IOException{
        return this.getSubTree(newoid, newoid.size());
    }
    //
    public Vector<VariableBinding> getSubTree(int[] oid, int leftlength) throws IOException{
        OID newoid = new OID(oid);
        return this.getSubTree(newoid, leftlength);
    }
    // 获取属于这个oid下的所有子节点，且要求当前子节点就是叶子节点
    public Vector<VariableBinding> getSubTree(OID oid, int leftlength) throws IOException{
        // 初始化snmp服务器
        // 设置PDU信息
        Vector<VariableBinding> vbs = new Vector<VariableBinding>();
        OID rootoid = new OID(oid);
        OID newoid = new OID(oid);
        while(true){
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(newoid));
            pdu.setType(PDU.GETNEXT);
            ResponseEvent respEvnt = sendPDU(pdu,readcommunity);
            // System.out.println(respEvnt.getResponse());
            if (respEvnt != null && respEvnt.getResponse() != null) {
                Vector<? extends VariableBinding> recVBs = respEvnt.getResponse()
                        .getVariableBindings();
                // 这里假定都是叶子节点，且只获得一个oid
                VariableBinding recVB = recVBs.elementAt(0);
                newoid = recVB.getOid();
                if(newoid.leftMostCompare(leftlength, rootoid) != 0) {
                    break;
                }
                else{
                    vbs.add(recVB);
                    System.out.println(respEvnt.getResponse());
                }
            }
            else{
                // 不加这个会陷入死循环
                break;
            }
        }
        return vbs;
    }

    public Vector<VariableBinding> getBulk(int[] oid){
        OID newoid = new OID(oid);
        return this.getBulk(newoid);
    }
    // 从这个oid开始，顺次获得最大PDU长度的可能的所有节点的信息，可能会获得很多冗余信息
    public Vector<VariableBinding> getBulk(OID oid){
        // get PDU
        Vector<VariableBinding> vbs = new Vector<VariableBinding>();
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(oid));
        pdu.setType(PDU.GETBULK);
        pdu.setMaxRepetitions(1000);
        ResponseEvent respEvnt = null;
        try {
            respEvnt = sendPDU(pdu,readcommunity);
        }catch(Exception e){
            e.printStackTrace();
        }
        System.out.println(respEvnt.getResponse());
        if (respEvnt != null && respEvnt.getResponse() != null) {
            Vector<? extends VariableBinding> recVBs = respEvnt.getResponse()
                    .getVariableBindings();
            vbs.addAll(recVBs);
        }

        return vbs;
    }
    public void close(){
        try {
            this.snmp.close();
            this.trapSnmp.close();
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public VariableBinding getTreeNode(int[] oid) throws IOException{
        OID newoid = new OID(oid);
        return this.getTreeNode(newoid);
    }
    // 获得这个oid 所对应的节点的值，注意这里假定这个oid就是某个子树节点
    public VariableBinding getTreeNode(OID oid) throws IOException, NullPointerException{
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(oid));
        pdu.setType(PDU.GET);
        ResponseEvent respEvnt = sendPDU(pdu,readcommunity);
        System.out.println(respEvnt.getResponse());
        if (respEvnt != null && respEvnt.getResponse() != null) {
            Vector<? extends VariableBinding> recVBs = respEvnt.getResponse()
                    .getVariableBindings();
            return recVBs.elementAt(0);
                /*//有些需要转换，但是有些不必转化比如mac地址
                if(recVB.getSyntax() == 4 && transflag && recVB.getVariable().toString().charAt(2) == ':'){
                    v[i] = octetStr2Readable(recVB.getVariable().toString());
                }
                else {
                    v[i] = recVB.getVariable().toString();
                }*/
        }
        else {
            // 这里fake一个数据返回
            throw new IOException();
        }
    }

    public boolean isValid(){
        // 通过获取都实现的一个OID是否有正确信息传回来判断是否这个设备可以被管理
        boolean result = false;
        try{
            this.getTreeNode(Constant.SysName);
            result = true;
        }catch(Exception e){
            e.printStackTrace();
            result = false;
        }
        return result;
    }


    public ResponseEvent sendPDU(PDU pdu,String community) throws IOException {
        // 设置 target
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(community));
        target.setAddress(targetAddress);
        // 通信不成功时的重试次数
        target.setRetries(2);
        // 超时时间
        target.setTimeout(2000);
        //终于知道问题的关键所在了  2c版本才增加了GETBULK..
        //那么为什么之前在试2c时发现1可以2c却不可以呢？具体哪个例子我也忘了，浪费这么长时间哎
        // 统一用版本2吧
        target.setVersion(SnmpConstants.version2c);
        // 向Agent发送PDU，并返回Response
        return snmp.send(pdu, target);
    }

    public boolean setStatus(int index, int status){
        ResponseEvent respEvnt = null;
        // set PDU
        PDU pdu = new PDU();
        // 既然Integer32是Variable的子类，那直接传Integer32就好
        OID oid= new OID(Constant.IfAdminStatus);
        oid.append(index);
        pdu.add(new VariableBinding(oid, new Integer32(status)));
        // errorStatus=Wrong value(10), The value cannot be assigned to the variable.
        pdu.setType(PDU.SET);
        System.out.println(pdu);
        try{
            sendPDU(pdu,writecommunity);
        }catch(Exception e){
            e.printStackTrace();
        }
        try {
            Thread.sleep(1000);
        }catch(Exception e){
            e.printStackTrace();
        }
        pdu = new PDU();
        pdu.add(new VariableBinding(new OID(Constant.IfOperStatus).append(index)));
        pdu.setType(PDU.GET);
        try {
            respEvnt = sendPDU(pdu,readcommunity);
        }catch (IOException e){
            e.printStackTrace();
        }
        System.out.println(respEvnt.getResponse());
        // TODO: SET 还是不能成功设置，很奇怪
        return respEvnt.getResponse().getVariableBindings().elementAt(0).getVariable().toInt() == status;
    }

    //snmp4j贴心的帮我把中文转成了octet的String形式，然后不给我转回来的方法？？
    //害得我得一个字符一个字符先转化为字节数组，然后转化为最后的GBK编码的String
    public static String octetStr2Readable(String s){
        //byte[] trues = s.getBytes();
        /*for(int j = 0 ; j < trues.length ; j++){
            System.out.println(trues[j]);
        }*/
        System.out.println(s);
        byte[] octets = new byte[0];
        try {
            octets = s.getBytes();
        }catch(Exception e){
            e.printStackTrace();
        }
        byte[] bytes = new byte[octets.length - (octets.length - 2) / 3];
        int length = 0;
        //System.out.println("---------------");
        for(int j = 0 ; j < octets.length ; j++){
            byte result = 0;
            if(octets[j] >= '0' && octets[j] <= '9'){
                result += (byte)(octets[j] - '0');
            }
            else if(octets[j] >= 'a' && octets[j] <= 'f'){
                result += (byte)(octets[j] - 'a' + 10);
            }
            j++;
            // length 竟然可以是奇数
            if(j < octets.length)
            {
                if(octets[j] >= '0' && octets[j] <= '9'){
                    result = (byte)(octets[j] - '0' + result * 16);
                }
                else if(octets[j] >= 'a' && octets[j] <= 'f'){
                    result = (byte)(octets[j] - 'a' + 10 + + result * 16);
                }
                j++;
            }

            bytes[length++] = result;
        }
        String news = new String();
        try {
            //TMD终于选对了字符集，早就该查windows采用的字符编码方式的
            news = new String(bytes, "GB2312").trim();
        }catch(Exception e){
            e.printStackTrace();
        }
        return news;
    }


    // 获得接口数量
    public int getInterfaceNum(){
        Variable v = null;
        try{
            v = this.getTreeNode(Constant.IfNum).getVariable();
        }catch(IOException e){
            e.printStackTrace();
        }
        return v.toInt();
    }

    // 获得接口的String状态表示
    public String[] getInterfacesStatus(){
        Vector<VariableBinding> status = null;
        try{
            status = this.getSubTree(Constant.IfOperStatus);
        }catch(IOException e){
            e.printStackTrace();
        }
        String[] result = new String[status.size()];
        for(int i = 0 ; i < status.size() ; i++){
            //TODO 根据rfc标准，这里应该有三种状态
            result[i] = status.elementAt(i).getVariable().toInt() == 1 ? "UP" : "DOWN";
        }
        return result;
    }

    // 获得设备系统信息
    public SysInfo getSysInfo(){
        // 还得一个一个获取
        SysInfo sys = new SysInfo();
        try {
            sys.setSysDescr(this.getTreeNode(Constant.SysDescr).getVariable().toString());
            sys.setSysContact(this.getTreeNode(Constant.SysContact).getVariable().toString());
            sys.setSysLocation(this.getTreeNode(Constant.SysLocation).getVariable().toString());
            sys.setSysName(this.getTreeNode(Constant.SysName).getVariable().toString());
            sys.setSysObjectId(this.getTreeNode(Constant.SysObjectId).getVariable().toString());
            sys.setSysUpTime(this.getTreeNode(Constant.SysUpTime).getVariable());
            sys.setSysCpuUsedRate(this.collectCPU());
        }catch(IOException e){
            e.printStackTrace();
        }
        return sys;
    }

    // 获得vlan的开始index
    public int getVlanBegin(){
        Vector<VariableBinding> vbs = null;
        try
        {
            vbs = this.getSubTree(Constant.IfDescr);
        }catch(IOException e){
            e.printStackTrace();
        }
        for(int i = 0 ; i < vbs.size() ; i++){
            String descr = vbs.elementAt(i).getVariable().toString();
            if(descr.length() >= 3 && descr.charAt(2) == ':'){
                descr = octetStr2Readable(descr);
            }
            if(descr.length() >= 4 && descr.substring(0, 4).equals("Vlan")) {
                System.out.println(descr);
                return vbs.elementAt(i).getOid().last() - 1;
            }
        }
        return -1;
    }

    // 获得该设备所有ip
    public Vector<IP> getOwnIp(){
        Vector<VariableBinding> ipaddrs = null;
        Vector<VariableBinding> ipifindexs = null;
        Vector<VariableBinding> ipnetmasks = null;
        Vector<VariableBinding> ipmaxsizes = null;
        try {
            ipaddrs = this.getSubTree(Constant.IpAdEntAddr);
            ipifindexs = this.getSubTree(Constant.IpAdEntIfAddr);
            ipnetmasks = this.getSubTree(Constant.IpAdEntNetmask);
            ipmaxsizes = this.getSubTree(Constant.IpAdEntReasmMaxSize);
        }catch(IOException e){
            e.printStackTrace();
        }
        Vector<IP> ips = new Vector<IP>();
        for(int i = 0 ; i < ipaddrs.size() ; i++){
            IP ip  = new IP();
            ip.setIpAddress(ipaddrs.elementAt(i).getVariable().toString());
            ip.setIpIfIndex(ipifindexs.elementAt(i).getVariable().toInt());
            ip.setIpNetMask(ipnetmasks.elementAt(i).getVariable().toString());
            ip.setIpMaxSize(ipmaxsizes.elementAt(i).getVariable().toInt());
            ips.add(ip);
        }
        return ips;
    }

    public DeviceType getDeviceType(){
        if(this.device.getType() == DeviceType.none){
            //首先判断ipForwarding
            VariableBinding ipforwarding = null;
            try {
                ipforwarding = this.getTreeNode(Constant.IpForwarding);
            }catch(IOException e){
                e.printStackTrace();
            }
            if(ipforwarding.getVariable().getSyntax() == 128 || ipforwarding.getVariable().toInt() != 1){
                this.device.setType(DeviceType.host);
            }
            else{
                // 交换机和路由器
                VariableBinding v = null;
                try{
                    v = this.getTreeNode(Constant.OnlyRouterOid);
                }catch(IOException e){
                    e.printStackTrace();
                }
                // 128是NoSuchObject的对应syntax的编号
                if(v.getSyntax() == 128)
                {
                    this.device.setType(DeviceType.exchange);
                }
                else {
                    // 路由器或者是三层交换机
                    this.device.setType( DeviceType.router);
                }
            }
        }

        return this.device.getType();
    }

    public IPRoute[] getIpRoute(){
        Vector<VariableBinding> destvbs = null;
        Vector<VariableBinding> ifindexvbs = null;
        Vector<VariableBinding> metric1vbs = null;
        Vector<VariableBinding> metric2vbs = null;
        Vector<VariableBinding> metric3vbs = null;
        Vector<VariableBinding> metric4vbs = null;
        Vector<VariableBinding> nexthopvbs = null;
        Vector<VariableBinding> typevbs = null;
        Vector<VariableBinding> protovbs = null;
        Vector<VariableBinding> agevbs = null;
        Vector<VariableBinding> maskvbs = null;
        Vector<VariableBinding> metric5vbs = null;
        try{
            destvbs = this.getSubTree(Constant.IpRouteDest);
            ifindexvbs = this.getSubTree(Constant.IpRouteIfIndex);
            metric1vbs = this.getSubTree(Constant.IpRouteMetric1);
            metric2vbs = this.getSubTree(Constant.IpRouteMetric2);
            metric3vbs = this.getSubTree(Constant.IpRouteMetric3);
            metric4vbs = this.getSubTree(Constant.IpRouteMetric4);
            nexthopvbs = this.getSubTree(Constant.IpRouteNextHop);
            typevbs = this.getSubTree(Constant.IpRouteType);
            protovbs = this.getSubTree(Constant.IpRouteProto);
            agevbs = this.getSubTree(Constant.IpRouteAge);
            maskvbs = this.getSubTree(Constant.IpRouteMask);
            metric5vbs = this.getSubTree(Constant.IpRouteMetric5);
        }catch(IOException e){
            e.printStackTrace();
        }
        IPRoute[] irs = new IPRoute[destvbs.size()];
        for(int i = 0 ; i < destvbs.size() ; i++){
            IPRoute ir = new IPRoute();
            ir.setIpRouteAge(agevbs.elementAt(i).getVariable().toInt());
            ir.setIpRouteDest(destvbs.elementAt(i).getVariable().toString());
            ir.setIpRouteIfIndex(ifindexvbs.elementAt(i).getVariable().toInt());
            ir.setIpRouteMask(maskvbs.elementAt(i).getVariable().toString());
            ir.setIpRouteMetric1(metric1vbs.elementAt(i).getVariable().toInt());
            ir.setIpRouteMetric2(metric2vbs.elementAt(i).getVariable().toInt());
            ir.setIpRouteMetric3(metric3vbs.elementAt(i).getVariable().toInt());
            ir.setIpRouteMetric4(metric4vbs.elementAt(i).getVariable().toInt());
            ir.setIpRouteMetric5(metric5vbs.elementAt(i).getVariable().toInt());
            ir.setIpRouteNextHop(nexthopvbs.elementAt(i).getVariable().toString());
            ir.setIpRouteProto(IPRouteProto.int2type(protovbs.elementAt(i).getVariable().toInt()));
            ir.setIpRouteType(IPRouteType.int2Type(typevbs.elementAt(i).getVariable().toInt()));
            irs[i] = ir;
        }

        return irs;
    }

    public String[] getLinkedIp(){
        Vector<VariableBinding> vbs = new Vector<>();
        // 这里需要通过这个设备的所有IP来获取与它相连的设备
            try {
                vbs.addAll(this.getSubTree(Constant.atNetAddress));
            }catch(IOException e){
                e.printStackTrace();
            }
        String[] ips = new String[vbs.size()];
        for(int i = 0 ; i < ips.length ; i ++){
            ips[i] = vbs.elementAt(i).getVariable().toString();
        }
        return ips;
    }

    public int collectCPU(){
        Vector<VariableBinding> vbs = null;
        try{
            vbs = this.getSubTree(Constant.hrProcessorLoad);
        }catch(IOException e){
            e.printStackTrace();
        }
        int sum = 0;
        for(int i = 0 ; i < vbs.size() ; i++){
            sum += vbs.elementAt(i).getVariable().toInt();
        }
        if(vbs.size() == 0){
            // 路由器和交换机没有实现这个MIB
            return 0;
        }
        else{
            return sum / vbs.size();
        }
    }

    public Disk[] collectDisk(){
        Vector<VariableBinding> indexvbs = null;
        Vector<VariableBinding> descrvbs = null;
        Vector<VariableBinding> unitvbs = null;
        Vector<VariableBinding> sizevbs = null;
        Vector<VariableBinding> usedvbs = null;
        try{
            indexvbs = this.getSubTree(Constant.hrStorageIndex);
            descrvbs = this.getSubTree(Constant.hrStorageDescr);
            unitvbs = this.getSubTree(Constant.hrStorageUnit);
            sizevbs = this.getSubTree(Constant.hrStorageSize);
            usedvbs = this.getSubTree(Constant.hrStorageUsed);
        }catch(IOException e){
            e.printStackTrace();
        }
        Disk[] disks = new Disk[indexvbs.size()];
        for(int i = 0 ; i < disks.length ; i++){
            Disk disk = new Disk();
            disk.setStorageDescr(descrvbs.elementAt(i).getVariable().toString());
            disk.setStorageUnits(unitvbs.elementAt(i).getVariable().toInt());
            disk.setUsedRate(sizevbs.elementAt(i).getVariable().toLong(), usedvbs.elementAt(i).getVariable().toLong());
            disks[i] = disk;
        }
        return disks;
    }
    public Process[] getProcesses(){
        Vector<VariableBinding> indexvbs = null;
        Vector<VariableBinding> namevbs = null;
        Vector<VariableBinding> typevbs = null;
        // Vector<VariableBinding> memoryvbs = null;
        Vector<VariableBinding> statusvbs = null;
        try{
            indexvbs = this.getSubTree(Constant.hrSWRunIndex);
            namevbs = this.getSubTree(Constant.hrSWRunName);
            typevbs = this.getSubTree(Constant.hrSWRunType);
            // memoryvbs = this.getSubTree(Constant.hrSWRunMemory);
            statusvbs = this.getSubTree(Constant.hrSWRunStatus);
        }catch(IOException e){
            e.printStackTrace();
        }
        Process[] processes = new Process[indexvbs.size()];
        for(int i = 0 ; i < processes.length ; i++){
            Process process = new Process();
            process.setIndex(indexvbs.elementAt(i).getVariable().toInt());
            // process.setMemory(memoryvbs.elementAt(i).getVariable().toInt());
            process.setName(namevbs.elementAt(i).getVariable().toString());
            process.setType(ProcessType.values()[typevbs.elementAt(i).getVariable().toInt() - 1]);
            process.setStatus(ProcessRunStatus.values()[statusvbs.elementAt(i).getVariable().toInt() - 1]);
            processes[i] = process;
        }
        return processes;
    }

    public int getInBound(){
        Vector<VariableBinding> vbs = null;
        try{
            vbs = this.getSubTree(Constant.IfInBound);
        }catch(Exception e){
            e.printStackTrace();
        }
        int result = 0;
        for(int i = 0 ; i < vbs.size() ; i++){
            result += vbs.elementAt(i).getVariable().toInt();
        }
        return result;
    }

    public int getInBound(int index){
        VariableBinding vb = null;
        try{
            OID oid = new OID(Constant.IfInBound);
            oid.append(index);
            vb = this.getTreeNode(oid);
        }catch(Exception e){
            vb = new VariableBinding();
        }
        return vb.getVariable().toInt();
    }

    public int getOutBound(){
        Vector<VariableBinding> vbs = null;
        try{
            vbs = this.getSubTree(Constant.IfOutBound);
        }catch(Exception e){
            e.printStackTrace();
        }
        int result = 0;
        for(int i = 0 ; i < vbs.size() ; i++){
            result += vbs.elementAt(i).getVariable().toInt();
        }
        return result;
    }

    public int getOutBound(int index){
        VariableBinding vb = null;
        try{
            OID oid = new OID(Constant.IfOutBound);
            oid.append(index);
            vb = this.getTreeNode(oid);
        }catch(Exception e){
            vb = new VariableBinding();
        }
        return vb.getVariable().toInt();
    }

    public String getMask(){
        Vector<VariableBinding> ipvbs = null;
        Vector<VariableBinding> maskvbs = null;
        try{
            ipvbs = this.getSubTree(Constant.IpAdEntAddr);
            maskvbs = this.getSubTree(Constant.IpAdEntNetmask);
        }catch(Exception e){
            e.printStackTrace();
        }
        for(int i = 0 ; i < ipvbs.size() ; i++){
            String ip = ipvbs.elementAt(i).getVariable().toString();
            if(ip.equals(this.ip)){
                return maskvbs.elementAt(i).getVariable().toString();
            }
        }
        // 默认255.255.255.0
        return "255.255.255.0";
    }
    public AddressTranslation[] getATtable(){
        Vector<VariableBinding> indexvbs = null;
        Vector<VariableBinding> phyvbs = null;
        Vector<VariableBinding> netvbs = null;
        Vector<VariableBinding> typevbs = null;
        try{
            indexvbs = this.getSubTree(Constant.ipNetToMediaIfIndex);
            phyvbs = this.getSubTree(Constant.ipNetToMediaPhy);
            netvbs = this.getSubTree(Constant.ipNetToMediaNet);
            typevbs = this.getSubTree(Constant.ipNetToMediaType);
        }catch(IOException e){
            e.printStackTrace();
        }
        AddressTranslation[] ats = new AddressTranslation[indexvbs.size()];
        for(int i = 0 ; i < ats.length ; i++){
            AddressTranslation at = new AddressTranslation();
            at.setIfIndex(indexvbs.elementAt(i).getVariable().toInt());
            at.setNetAddress(netvbs.elementAt(i).getVariable().toString());
            at.setPhyAddress(phyvbs.elementAt(i).getVariable().toString());
            at.setTranslationType(TranslationType.values()[typevbs.elementAt(i).getVariable().toInt() - 1]);
            ats[i] = at;
        }
        return ats;
    }

}
