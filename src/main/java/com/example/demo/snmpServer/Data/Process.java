package com.example.demo.snmpServer.Data;

public class Process {
    int index;
    String name;
    ProcessType type;
    ProcessRunStatus status;

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }



    public ProcessRunStatus getStatus() {
        return status;
    }

    public ProcessType getType() {
        return type;
    }

    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(ProcessRunStatus status) {
        this.status = status;
    }

    public void setType(ProcessType type) {
        this.type = type;
    }

}
