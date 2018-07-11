package com.example.demo.Graph;

public class Edge {
    private Node dest;
    private int status;

    public void setStatus(int status) {
        this.status = status;
    }

    public int getDestIndex() {
        return this.dest.getIndex();
    }

    public Node getDest(){
        // 这里一开始无限递归了...
        return this.dest;
    }

    public int getStatus() {
        return status;
    }

    public void setDest(Node n) {
        this.dest = n;
    }

    public Edge(Node n){
        this.dest = n;
        this.status = 0;
    }
}
