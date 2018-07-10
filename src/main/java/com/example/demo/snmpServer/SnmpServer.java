package com.example.demo.snmpServer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Vector;

import com.example.demo.snmpServer.Data.*;
import com.sun.jmx.snmp.SnmpString;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.xml.ws.Response;

public class SnmpServer{
    private Snmp snmp = null;
    private Address targetAddress = null;
    private String readcommunity;
    private String writecommunity;
    private TransportMapping trapTransport;
    private Snmp trapSnmp;
    private Device device;
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


    public SnmpServer(String ip, int port, String readcommunity, String writecommunity) {
        // 设置Agent方的IP和端口
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
    public VariableBinding getTreeNode(OID oid) throws IOException{
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
        return null;
    }


    public ResponseEvent sendPDU(PDU pdu,String community) throws IOException {
        // 设置 target
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(community));
        target.setAddress(targetAddress);
        // 通信不成功时的重试次数
        target.setRetries(2);
        // 超时时间
        target.setTimeout(10000);
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
            if(ipforwarding.getVariable().toInt() != 1){
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


}
