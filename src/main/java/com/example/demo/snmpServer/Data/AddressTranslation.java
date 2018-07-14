package com.example.demo.snmpServer.Data;

import com.example.demo.snmpServer.Data.Type.TranslationType;

public class AddressTranslation {
    private int ifIndex;
    private String phyAddress;
    private String netAddress;
    private TranslationType translationType;

    public int getIfIndex() {
        return ifIndex;
    }

    public String getNetAddress() {
        return netAddress;
    }

    public String getPhyAddress() {
        return phyAddress;
    }

    public TranslationType getTranslationType() {
        return translationType;
    }

    public void setIfIndex(int ifIndex) {
        this.ifIndex = ifIndex;
    }

    public void setNetAddress(String netAddress) {
        this.netAddress = netAddress;
    }

    public void setPhyAddress(String phyAddress) {
        this.phyAddress = phyAddress;
    }

    public void setTranslationType(TranslationType type) {
        this.translationType = type;
    }
}
