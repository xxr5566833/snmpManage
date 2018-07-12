package com.example.demo.Graph;

public class NodeData {
    private String name;
    private int category;

    public void setCategory (NodeType type) {
        this.category = type.getType();
    }

    public int getCategory() {
        return this.category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
