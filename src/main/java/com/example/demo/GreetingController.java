package com.example.demo;


import com.example.demo.snmpServer.SnmpServer;
import org.snmp4j.smi.Variable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.bind.DatatypeConverter;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class GreetingController {
    private static String template = "Hello, %s";
    private final AtomicLong counter = new AtomicLong();
    public static String toHexString(byte[] array) {
        return DatatypeConverter.printHexBinary(array);
    }

    public static byte[] toByteArray(String s) {
        return DatatypeConverter.parseHexBinary(s);
    }
    @RequestMapping("/greeting")
    public String[] greeting (@RequestParam(value = "name", defaultValue = "World")  String name){
        SnmpServer t = new SnmpServer("127.0.0.1", 161);
        int[] oid = {1, 3, 6, 1, 2, 1, 1, 1, 0};
        String[] v = t.getInfo(oid);
        String[] result = new String[v.length];
        for(int i = 0 ; i < result.length ; i++){
            result[i] = v[i];
            System.out.println(result[i]);
        }
        return result;
    }
}
