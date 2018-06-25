package com.example.demo.snmpServer;

import java.io.IOException;
import java.util.Vector;
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
    public String[] getInfo(int[] oid) {
        try {
            //this.setPDU(oid);
            String[] s = this.getPDU();
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



    public String[] getPDU() throws IOException {
        // get PDU
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(new int[] { 1, 3, 6, 1, 2, 1, 1, 2, 0})));
        pdu.setType(PDU.GETBULK);
        return readResponse(sendPDU(pdu));

    }



    public String[] readResponse(ResponseEvent respEvnt) {
        // 解析Response
        if (respEvnt != null && respEvnt.getResponse() != null) {
            Vector<? extends VariableBinding> recVBs = respEvnt.getResponse()
                    .getVariableBindings();
            String[] v = new String[recVBs.size()];
            for (int i = 0; i < recVBs.size(); i++) {
                VariableBinding recVB = recVBs.elementAt(i);
                v[i] = recVB.getVariable().toString();
            }
            return v;
        }
        return null;

    }

    /*public static void main(String[] args){
        SnmpServer server = new SnmpServer("127.0.0.1", 161);
        int[] oid = {1, 3, 6, 1, 2, 1, 1, 1};
        String[] strs = server.getInfo(oid);
    }*/

}
