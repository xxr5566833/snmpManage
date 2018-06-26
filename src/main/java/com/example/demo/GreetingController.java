package com.example.demo;


import com.example.demo.snmpServer.Data.SysInfo;
import com.example.demo.snmpServer.SnmpServer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.bind.DatatypeConverter;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class GreetingController {
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping("/test")
    public String[] test (@RequestParam(value = "name", defaultValue = "World")  String name){
        SnmpServer t = new SnmpServer("127.0.0.1", 161);
        int[] oid = {1, 3, 6, 1, 2, 1, 2, 2, 1, 2, 19};
        String[] v = t.getInfo(oid);
        for(int i = 0 ; i < v.length ; i++)
            System.out.println(v[i]);
        return v;
    }

    @RequestMapping("/sys")
    public SysInfo getSysInfo(){
        SnmpServer t = new SnmpServer("127.0.0.1", 161);
        int[] oid = {1, 3, 6, 1, 2, 1, 1, 1, 0};
        SysInfo sys = new SysInfo();
        String[] s = t.getInfo(oid);
        sys.setSysDescr(s[0]);
        oid[7] += 1;
        s = t.getInfo(oid);
        sys.setSysObjectId(s[0]);
        oid[7] += 1;
        s = t.getInfo(oid);
        sys.setSysUpTime(s[0]);
        oid[7] += 1;
        s = t.getInfo(oid);
        sys.setSysContact(s[0]);
        oid[7] += 1;
        s = t.getInfo(oid);
        sys.setSysName(s[0]);
        //???为啥要+=2？
        oid[7] += 2;
        s = t.getInfo(oid);
        sys.setSysLocation(s[0]);
        return sys;
    }

    //@RequestMapping("/interface")

}
