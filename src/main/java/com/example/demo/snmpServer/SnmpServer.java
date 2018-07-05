package com.example.demo.snmpServer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Vector;

import com.sun.jmx.snmp.SnmpString;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import javax.xml.ws.Response;

public class SnmpServer implements Runnable{
    private Snmp snmp = null;
    private Address targetAddress = null;
    private String community;
    private TransportMapping trapTransport;
    private Snmp trapSnmp;
    public void initTrapListen(String ip, int port){
        // 设置接收trap的IP和端口
        try {
            this.trapTransport = new DefaultUdpTransportMapping(new UdpAddress(
                    ip + "/" + port));
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
        this.initTrapListen(ip, 162);
    }
    public String[] walkInfo(int[] oid, boolean transflag){
        try {
            //this.setPDU(oid);
            String[] s = this.walkPDU(oid, transflag);
            return s;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public void close(){
        try {
            this.snmp.close();
            this.trapSnmp.close();
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public String[] walkPDU(int[] oid, boolean transflag) throws IOException {
        // get PDU
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(oid)));
        pdu.setType(PDU.GETBULK);
        pdu.setMaxRepetitions(1000);

        ResponseEvent respEvnt = sendPDU(pdu);
        System.out.println(respEvnt.getResponse());
        if (respEvnt != null && respEvnt.getResponse() != null) {
            Vector<? extends VariableBinding> recVBs = respEvnt.getResponse()
                    .getVariableBindings();
            String[] v = new String[recVBs.size()];
            for (int i = 0; i < recVBs.size(); i++) {
                VariableBinding recVB = recVBs.elementAt(i);
                //有些需要转换，但是有些不必转化比如mac地址
                if(recVB.getSyntax() == 4 && transflag){
                    v[i] = octetStr2Readable(recVB.getVariable().toString());
                }
                else {
                    v[i] = recVB.getVariable().toString();
                }
            }
            return v;
        }
        return null;

    }

    public Vector<? extends VariableBinding> walkVB(int[] oid, boolean transflag){
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
            return recVBs;

        }
        return null;
    }
    public VariableBinding getVB(int[] oid, boolean transflag){
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(oid)));
        pdu.setType(PDU.GET);
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
            return recVBs.elementAt(0);

        }
        return null;
    }

    public String[] getInfo(int[] oid, boolean transflag) {
        try {
            //this.setPDU(oid);
            String[] s = this.getPDU(oid, transflag);
            return s;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public ResponseEvent sendPDU(PDU pdu) throws IOException {
        // 设置 target
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(this.community));
        target.setAddress(targetAddress);
        // 通信不成功时的重试次数
        target.setRetries(2);
        // 超时时间
        target.setTimeout(1500);
        //终于知道问题的关键所在了  2c版本才增加了GETBULK..
        //那么为什么之前在试2c时发现1可以2c却不可以呢？具体哪个例子我也忘了，浪费这么长时间哎
        target.setVersion(SnmpConstants.version2c);
        // 向Agent发送PDU，并返回Response
        return snmp.send(pdu, target);
    }

    public boolean setStatus(int[] oid, int status){
        ResponseEvent respEvnt = null;
        // set PDU
        PDU pdu = new PDU();
        // 既然Integer32是Variable的子类，那直接传Integer32就好
        pdu.add(new VariableBinding(new OID(oid), new Integer32(status)));
        // errorStatus=Wrong value(10), The value cannot be assigned to the variable.
        pdu.setType(PDU.SET);
        try{
            respEvnt = sendPDU(pdu);
        }catch(Exception e){
            e.printStackTrace();
        }
        System.out.println(respEvnt.getResponse());
        PDU response = respEvnt.getResponse();
        return response.getErrorIndex() == 0;
    }

    public ResponseEvent setPDU(int[] oid) throws IOException {
        // set PDU
        PDU pdu = new PDU();
        // 既然Integer32是Variable的子类，那直接传Integer32就好
        pdu.add(new VariableBinding(new OID(oid), new Integer32(oid[10])));
        // errorStatus=Wrong value(10), The value cannot be assigned to the variable.
        pdu.setType(PDU.SET);
        return sendPDU(pdu);
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
            if(octets[j] >= '0' && octets[j] <= '9'){
                result = (byte)(octets[j] - '0' + result * 16);
            }
            else if(octets[j] >= 'a' && octets[j] <= 'f'){
                result = (byte)(octets[j] - 'a' + 10 + + result * 16);
            }
            j++;
            bytes[length++] = result;
        }
        String news = new String();
        try {
            //TMD终于选对了字符集，早就该查windows采用的字符编码方式的，nice！
            news = new String(bytes, "GB2312").trim();
        }catch(Exception e){
            e.printStackTrace();
        }
        return news;
    }

    public String[] getPDU(int[] oid, boolean transflag) throws IOException {
        // get PDU
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(oid)));
        pdu.setType(PDU.GET);
        ResponseEvent respEvnt = sendPDU(pdu);
        System.out.println(respEvnt.getResponse());
        if (respEvnt != null && respEvnt.getResponse() != null) {
            Vector<? extends VariableBinding> recVBs = respEvnt.getResponse()
                    .getVariableBindings();
            String[] v = new String[recVBs.size()];
            for (int i = 0; i < recVBs.size(); i++) {
                VariableBinding recVB = recVBs.elementAt(i);
                //有些需要转换，但是有些不必转化比如mac地址
                if(recVB.getSyntax() == 4 && transflag){
                    v[i] = octetStr2Readable(recVB.getVariable().toString());
                }
                else {
                    v[i] = recVB.getVariable().toString();
                }
            }
            return v;
        }
        return null;

    }

}
