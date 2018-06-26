package com.example.demo.snmpServer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Vector;

import com.sun.jmx.snmp.SnmpString;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
public class SnmpServer {
    private Snmp snmp = null;
    private Address targetAddress = null;
    public SnmpServer(String ip, int port) {
        // 设置Agent方的IP和端口
        targetAddress = GenericAddress.parse("udp:"+ ip + "/" + port);
        try {
            TransportMapping transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            transport.listen();
        }
        catch(IOException e){
            e.printStackTrace();
        }
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
        target.setCommunity(new OctetString("public"));
        target.setAddress(targetAddress);
        // 通信不成功时的重试次数
        target.setRetries(2);
        // 超时时间
        target.setTimeout(1500);
        target.setVersion(SnmpConstants.version1);
        // 向Agent发送PDU，并返回Response
        return snmp.send(pdu, target);
    }



    public void setPDU(int[] oid) throws IOException {
        // set PDU
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(oid), new OctetString("SNMPTEST")));
        pdu.setType(PDU.SET);
        sendPDU(pdu);
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
