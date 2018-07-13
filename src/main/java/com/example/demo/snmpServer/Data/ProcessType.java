package com.example.demo.snmpServer.Data;

public enum ProcessType {
    unknown(1),
    operatingSystem(2),
    deviceDriver(3),
    application(4);
    private int type;

    private ProcessType(int type) {
        this.type = type;
    }

    public int getType() {
        return this.type;
    }
}
