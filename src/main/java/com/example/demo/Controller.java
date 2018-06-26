package com.example.demo;


import com.example.demo.snmpServer.Data.IFType;
import com.example.demo.snmpServer.Data.InterFace;
import com.example.demo.snmpServer.Data.Status;
import com.example.demo.snmpServer.Data.SysInfo;
import com.example.demo.snmpServer.SnmpServer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.bind.DatatypeConverter;
import java.net.InterfaceAddress;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class Controller {
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping("/test")
    public String[] test (@RequestParam(value = "name", defaultValue = "World")  String name){
        SnmpServer t = new SnmpServer("127.0.0.1", 161);
        int[] oid = {1, 3, 6, 1, 2, 1, 2, 2, 1, 9, 19};
        String[] v = t.getInfo(oid, false);
        for(int i = 0 ; i < v.length ; i++)
            System.out.println(v[i]);
        return v;
    }

    @RequestMapping("/sys")
    public SysInfo getSysInfo(){
        //除了 7 以外其他的都获取
        SnmpServer t = new SnmpServer("127.0.0.1", 161);
        int[] oid = {1, 3, 6, 1, 2, 1, 1, 1, 0};
        SysInfo sys = new SysInfo();
        String[] s = t.getInfo(oid, false);
        sys.setSysDescr(s[0]);
        oid[7] += 1;
        s = t.getInfo(oid, false);
        sys.setSysObjectId(s[0]);
        oid[7] += 1;
        s = t.getInfo(oid, false);
        sys.setSysUpTime(s[0]);
        oid[7] += 1;
        s = t.getInfo(oid, false);
        sys.setSysContact(s[0]);
        oid[7] += 1;
        s = t.getInfo(oid, false);
        sys.setSysName(s[0]);
        //???为啥要+=2？
        oid[7] += 2;
        s = t.getInfo(oid, false);
        sys.setSysLocation(s[0]);
        return sys;
    }
    //完成，不过速度有点慢，之后调用getBulk看能不能改善这个情况
    @RequestMapping("/interface")
    public InterFace[] getInterfaces(){
        //获取设备的接口情况,获取oid为9以前的情况
        Vector<InterFace> interFaces = new Vector<InterFace>();
        int interfacenum = 0;
        int[] oid = {1, 3, 6, 1, 2, 1, 2, 1, 0};
        SnmpServer t = new SnmpServer("127.0.0.1", 161);
        String[] s = t.getInfo(oid, false);
        interfacenum = Integer.parseInt(s[0]);
        int[] newoid = {1, 3, 6, 1, 2, 1, 2, 2, 1, 1, 0};
        for(int i = 0 ; i < interfacenum ; i++){
            //每个接口每个属性取信息
            newoid[10] += 1;
            newoid[9] = 1;
            InterFace inter = new InterFace();
            //ifDescr
            newoid[9] += 1;
            s = t.getInfo(newoid, true);
            inter.setIfDescr(s[0]);
            //ifType
            newoid[9] += 1;
            s = t.getInfo(newoid, false);
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
            interFaces.add(inter);
        }

        InterFace[] inters = interFaces.toArray(new InterFace[interFaces.size()]);
        return inters;
    }


}
