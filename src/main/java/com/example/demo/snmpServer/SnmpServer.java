package com.example.demo.snmpServer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Vector;

import com.example.demo.snmpServer.Data.Constant;
import com.example.demo.snmpServer.Data.SysInfo;
import com.sun.jmx.snmp.SnmpString;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import javax.xml.ws.Response;

public class SnmpServer{
    private Snmp snmp = null;
    private Address targetAddress = null;
    private String community;
    private TransportMapping trapTransport;
    private Snmp trapSnmp;
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


    public SnmpServer(String ip, int port, String community) {
        // 设置Agent方的IP和端口
        targetAddress = GenericAddress.parse("udp:"+ ip + "/" + port);
        this.community = community;
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
    // 获取属于这个oid下的所有子节点，且要求当前子节点就是叶子节点
    public Vector<Variable> getSubTree(int[] oid) throws IOException{
        // 初始化snmp服务器
        // 设置PDU信息
        Vector<Variable> vbs = new Vector<Variable>();
        OID rootoid = new OID(oid);
        OID newoid = new OID(oid);
        while(true){
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(newoid));
            pdu.setType(PDU.GETNEXT);
            ResponseEvent respEvnt = sendPDU(pdu);
            // System.out.println(respEvnt.getResponse());
            if (respEvnt != null && respEvnt.getResponse() != null) {
                Vector<? extends VariableBinding> recVBs = respEvnt.getResponse()
                        .getVariableBindings();
                // 这里假定都是叶子节点，且只获得一个oid
                VariableBinding recVB = recVBs.elementAt(0);
                newoid = recVB.getOid();
                if(newoid.leftMostCompare(oid.length, rootoid) != 0) {
                    break;
                }
                else{
                    vbs.add(recVB.getVariable());
                    System.out.println(respEvnt.getResponse());
                }
            }
        }
        return vbs;
    }


    // 从这个oid开始，顺次获得最大PDU长度的可能的所有节点的信息，可能会获得很多冗余信息
    public Vector<Variable> getBulk(int[] oid){
        // get PDU
        Vector<Variable> vbs = new Vector<Variable>();
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(oid)));
        pdu.setType(PDU.GETBULK);
        pdu.setMaxRepetitions(1000);
        ResponseEvent respEvnt = null;
        try {
            respEvnt = sendPDU(pdu);
        }catch(Exception e){
            e.printStackTrace();
        }
        System.out.println(respEvnt.getResponse());
        if (respEvnt != null && respEvnt.getResponse() != null) {
            Vector<? extends VariableBinding> recVBs = respEvnt.getResponse()
                    .getVariableBindings();
            for (int i = 0; i < recVBs.size(); i++) {
                vbs.add(recVBs.elementAt(i).getVariable());
            }
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


    // 获得这个oid 所对应的节点的值，注意这里假定这个oid就是某个子树节点
    public Variable getTreeNode(int[] oid) throws IOException{
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(oid)));
        pdu.setType(PDU.GET);
        ResponseEvent respEvnt = sendPDU(pdu);
        System.out.println(respEvnt.getResponse());
        if (respEvnt != null && respEvnt.getResponse() != null) {
            Vector<? extends VariableBinding> recVBs = respEvnt.getResponse()
                    .getVariableBindings();
            return recVBs.elementAt(0).getVariable();
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


    public ResponseEvent sendPDU(PDU pdu) throws IOException {
        // 设置 target
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(this.community));
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
            sendPDU(pdu);
        }catch(Exception e){
            e.printStackTrace();
        }
        pdu = new PDU();
        pdu.add(new VariableBinding(new OID(Constant.IfOperStatus).append(index)));
        pdu.setType(PDU.GET);
        try {
            respEvnt = sendPDU(pdu);
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
            v = this.getTreeNode(Constant.IfNum);
        }catch(IOException e){
            e.printStackTrace();
        }
        return v.toInt();
    }

    // 获得接口的String状态表示
    public String[] getInterfacesStatus(){
        Vector<Variable> status = null;
        try{
            status = this.getSubTree(Constant.IfOperStatus);
        }catch(IOException e){
            e.printStackTrace();
        }
        String[] result = new String[status.size()];
        for(int i = 0 ; i < status.size() ; i++){
            //TODO 根据rfc标准，这里应该有三种状态
            result[i] = status.elementAt(i).toInt() == 1 ? "UP" : "DOWN";
        }
        return result;
    }

    // 获得设备系统信息
    public SysInfo getSysInfo(){
        // 还得一个一个获取
        SysInfo sys = new SysInfo();
        try {
            sys.setSysDescr(this.getTreeNode(Constant.SysDescr).toString());
            sys.setSysContact(this.getTreeNode(Constant.SysContact).toString());
            sys.setSysLocation(this.getTreeNode(Constant.SysLocation).toString());
            sys.setSysName(this.getTreeNode(Constant.SysName).toString());
            sys.setSysObjectId(this.getTreeNode(Constant.SysObjectId).toString());
            sys.setSysUpTime(this.getTreeNode(Constant.SysUpTime));
        }catch(IOException e){
            e.printStackTrace();
        }
        return sys;
    }
}
