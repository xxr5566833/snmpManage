package com.example.demo.Graph;

public class Edge {
    private int destIndex;
    private int status;

    public void setStatus(int status) {
        this.status = status;
    }

    public int getDestIndex() {
        return destIndex;
    }

    public int getStatus() {
        return status;
    }

    public void setDestIndex(int destIndex) {
        this.destIndex = destIndex;
    }

    public Edge(int dest){
        this.destIndex = dest;
        this.status = 0;
    }
}
