package com.example.demo.snmpServer.Data.Type;

public enum TranslationType {
     other(1), invalid(2), dynamic(3), Static(4);
     private int type;
     private TranslationType(int type){
         this.type = type;
     }
     public int getType(){
         return this.type;
     }

}
