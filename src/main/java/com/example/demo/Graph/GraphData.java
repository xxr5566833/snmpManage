package com.example.demo.Graph;


// 传给前段的数据
public class GraphData {
    private NodeData[] data;
    private LinkData[] link;
    private DevData[] devs;
    public LinkData[] getLink() {
        return link;
    }

    public NodeData[] getData() {
        return data;
    }

    public void setData(NodeData[] data) {
        this.data = data;
    }

    public void setLink(LinkData[] link) {
        this.link = link;
    }

    public DevData[] getDevs() {
        return devs;
    }

    public void setDevs(DevData[] devs) {
        this.devs = devs;
    }
}
