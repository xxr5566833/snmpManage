package com.example.demo.snmpServer.Data.Type;

public enum ProcessRunStatus {
    running(1), runnable(2), notRunnable(3), invalid(4);
    private int type;
    private ProcessRunStatus(int type){
        this.type = type;
    }
    public int getType(){
        return this.type;
    }
}
