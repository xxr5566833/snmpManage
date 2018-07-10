package com.example.demo.Graph;

import java.util.Vector;

public class Graph {
    public Vector<Node> nodes;
    public Vector<Vector<Edge>> edges;

    public Graph(){
        nodes = new Vector<Node>();
        edges = new Vector<Vector<Edge>>();
    }
}
