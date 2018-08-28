package com.example.demo.snmpServer.Data;

import java.util.Vector;

public class Vlan extends InterFace{
    private Vector<Integer> ports;
    private int index;
    String port;
    public Vlan(int index, String s){
        this.index = index;
        ports = new Vector<>();
        if(s.equals("null"))
        {
            return ;
        }else{
            String[] strs = s.split(":");
            for(int i = 0 ; i < strs.length ; i++){
                int result = toInt(strs[i]);
                int j = 8;
                while(j-- > 0){
                    if((result & 1) == 1){
                        ports.add(i * 8 + 8 - j);
                    }
                    result >>= 1;
                }
            }
            this.port = this.portToString();
        }

    }

    public Vector<Integer> getPorts(){
        return this.ports;
    }

    public int toInt(String str){
        // str是一个两位十六进制的字符串，现在需要把它解析为相应的8位int变量，然后通过移位获得它每一位上的值
        int result = 0;
        result = hexToInt(str.charAt(0)) * 16 + hexToInt(str.charAt(1));
        return result;
    }
    private int hexToInt(char c){
        // c是一个表示16进制数字的字符 0-9 a b c d e f
        // 现在要把它变成相应的数字
        if(c >= '0' && c <= '9'){
            return c - '0';
        }
        else{
            // c只有可能是abcdef中的一个
            return c - 'a' + 10;
        }
    }
    private String portToString(){
        String s = new String();
        for(int i = 0 ; i < this.ports.size() ; i++){
            s += (this.ports.elementAt(i) + " ");
        }
        return s;
    }

    public String getPort(){
        return this.portToString();
    }



}
