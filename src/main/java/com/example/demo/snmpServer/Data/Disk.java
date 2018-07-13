package com.example.demo.snmpServer.Data;

import com.example.demo.snmpServer.SnmpServer;

public class Disk {
    String storageDescr;
    int storageUnits;
    String usedRate;
    long totalSize;
    long usedSize;

    public int getStorageUnits() {
        return storageUnits;
    }

    public String getStorageDescr() {
        return storageDescr;
    }

    public String getUsedRate() {
        return usedRate;
    }

    public String getTotalSize() {
        return (this.totalSize * this.storageUnits / (1 << 30) ) + "G";
    }

    public void setUsedRate(long totalsize, long usedsize) {
        this.totalSize = totalsize;
        this.usedSize = usedsize;
        double usedRate = (double) usedsize / (double) totalsize;
        this.usedRate =   (this.totalSize == 0 ? "" : String.format("%.2f", usedRate) + "%");
    }

    public void setStorageDescr(String storageDescr) {
        if(storageDescr.charAt(2) == ':'){
            storageDescr = SnmpServer.octetStr2Readable(storageDescr);
        }
        this.storageDescr = storageDescr;

    }

    public void setStorageUnits(int storageUnits) {
        this.storageUnits = storageUnits;
    }






}
