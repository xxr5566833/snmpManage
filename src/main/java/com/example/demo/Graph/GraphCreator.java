package com.example.demo.Graph;

import com.example.demo.snmpServer.Data.*;
import com.example.demo.snmpServer.Data.Type.DeviceType;
import com.example.demo.snmpServer.Data.Type.IPRouteType;
import com.example.demo.snmpServer.SnmpServer;
import com.example.demo.snmpServer.SnmpServerCreater;
import org.snmp4j.smi.VariableBinding;

import java.io.IOException;
import java.util.Vector;

public class GraphCreator {
    private static Graph mygraph;
    public static Graph createGraph(String ip){
        Graph graph = new Graph();
        // 通过ip创建对应的节点
        // 首先通过ip对应设备的类型来判断
        Node n = null;
        SnmpServer t = SnmpServerCreater.getServer(ip);
        if(t.getDeviceType() == DeviceType.host){
            n = new Node(ip, NodeType.host, 0);
            // 获取其默认网关，从默认网关开始探索
            IPRoute[] ips = t.getIpRoute();
            for(int i = 0 ; i < ips.length ; i++){
                if(ips[i].getIpRouteDest().equals("0.0.0.0")){
                    SnmpServer server = SnmpServerCreater.getServer(ips[i].getIpRouteNextHop());
                    if(server.isValid()) {
                        ip = ips[i].getIpRouteNextHop();
                        n = new Node(ip, NodeType.gateway, 0);
                        break;
                    }
                }
            }
            // 如果发现了默认网关，那么一切顺利，如果没有发现默认网关，那么说明这个设备就是在一个由交换机连接的子网内
            if(n.getType() == NodeType.host){
                // 此时就需要查看地址转换表，选择与自己在同一网段的IP，检查是否是路由器
                Vector<VariableBinding> vbs = null;
                try{
                    vbs = t.getSubTree(Constant.ipNetToMediaNet);
                }catch(IOException e){
                    e.printStackTrace();
                }
                for(int i = 0 ; i < vbs.size() ; i++){
                    String tmpip = vbs.elementAt(i).getVariable().toString();
                    if(IPv4.isSameSubnet(tmpip, n.getMainIp(), t.getMask()) && !tmpip.endsWith(".255")){
                        // 只看在同一网段的主机以及那些地址不是.255的
                        SnmpServer tmpserver = SnmpServerCreater.getServer(tmpip);
                        if(tmpserver.isValid() && tmpserver.getDeviceType() == DeviceType.router){
                            // 这个地址就是我们想要的地址
                            n = new Node(tmpip, NodeType.gateway, 0);
                            break;
                        }
                    }
                }
            }
        }
        else{
            // 说明是路由器或者是三层交换机
            // 路由器的话可以直接进入图遍历算法了，但是交换机怎么办，这里我们不管是路由器还是交换机遍历他的路由表，因为路交换机的路由表里一定由路由器的地址，而路由器的地址中没有交换机的地址
            // 我们可以利用这个性质，统一把当前IP 设置为路由表中可以找到的IP且该IP对应的设备不是主机，那么该设备一定是路由器
            n = new Node(ip, NodeType.gateway, 0);
            IPRoute[] ips = t.getIpRoute();
            for(int i = 0 ; i < ips.length ; i++){
                IPRoute ipr = ips[i];
                if(ipr.getIpRouteType() == IPRouteType.invalid)
                    continue;
                    String nexthop = ipr.getIpRouteNextHop();
                    // 首先去掉这些对生成网络拓扑无用的情况
                    if(nexthop.equals("127.0.0.1") || nexthop.equals("0.0.0.0"))
                        continue;
                    if(ipr.getIpRouteType() == IPRouteType.direct)
                        continue;
                    // 对于交换机和路由器来说，非direct的路由的下一跳一定是一个路由器的地址
                    // 过滤掉nexthop为0.0.0.0 和127.0.0.1
                    SnmpServer server = SnmpServerCreater.getServer(nexthop);
                    if(server.isValid()){
                        // 设置n的ip
                        n = new Node(nexthop, NodeType.gateway, 0);
                        break;
                        // 此时ip一定是一个路由器的ip
                    }
            }

        }
        /*t = SnmpServerCreater.getServer(ip);
        n.setIps(t.getOwnIp());*/
        graph.addNode(n);
        dfs(n, graph);
        graph.setTypeNode();
        mygraph = graph;
        return graph;
    }
    public static Graph getMygraph(){
        return mygraph;
    }
    // TODO 有个问题 当两个不同网段设备连接同一交换机的不同vlan借口时，这个算法会把它们识别为它们俩分别与一个交换机相连，也就是识别为两个交换机，而实际上只有一个交换机只不过是两个不同的vlan
    // TODO 问题是交换机没有ip地址，信息无法获取，所以这个问题不知道怎么解决
    private static void parseSubnet(Node n, Graph graph, String ip, String netmask){
        // 对子网进行链路层分析，找出交换机和主机
        // 具体做法就是根据子网掩码和网络号，遍历除了在路由器中出现过的所有的可能的IP，如果发现可以获取这个IP的信息，那么就确定这个是一个相连的设备

        Vector<Node> exchanges = new Vector<>();
        Vector<Node> hosts = new Vector<>();

        // 这里可以通过地址转换表进行简化
        SnmpServer server = SnmpServerCreater.getServer(n.getMainIp());
        String[] possibleip = server.getLinkedIp();
                // 开始解析这个子网的结构
                // 获得了ip和子网掩码，那么此时就可以获得这个子网的地址范围，接下来遍历这个子网的每一个ip，确保不会与与它相连的路由器ip地址重复
                for(int i = 0 ; i < possibleip.length ; i ++){
                    IPv4 ipv4 = new IPv4(possibleip[i], netmask);
                    if(graph.isRepeated(ipv4.getIP()) || !IPv4.isSameSubnet(ip, possibleip[i], netmask)) {
                        continue;
                    }
                    // 获取这个ip所对应的设备的信息
                    SnmpServer t = new SnmpServer(ipv4.getIP());

                    if (t.isValid()) {

                        DeviceType type = t.getDeviceType();
                        if (type == DeviceType.host) {
                            Node hostn = new Node(ipv4.getIP(), NodeType.host);
                            Vector<IP> ips = new Vector<>();
                            ips.add(new IP(hostn.getMainIp(), netmask));
                            graph.setIps(hostn, ips);
                            hosts.add(hostn);
                        } else {
                            Node exhangen = new Node(ipv4.getIP(), NodeType.exchange);
                            exchanges.add(exhangen);
                            Vector<IP> ips = t.getOwnIp();
                            graph.setIps(exhangen, ips);

                        }
                    }
                }

        for(int i = 0 ; i < hosts.size() ; i++){
             Node hostn = hosts.elementAt(i);
             // 遍历交换机找到管理它的交换机
            for(int j = 0 ; j < exchanges.size() ; j++){
                Node exchangen = exchanges.elementAt(j);
                if(exchangen.isSameSubnet(hostn.getMainIp())){
                    // 接下来还需要检查是不是当前交换机比之前主机已经连接的交换机的范围更小
                    Vector<Edge> edges = hostn.getEdges();
                    if(edges.size() > 0){
                        Edge e = edges.elementAt(0);
                        Node last = e.getDest();
                        if(IP.compare(last.getIps().elementAt(0).getIpNetMask(), exchangen.getIps().elementAt(0).getIpNetMask()) == 1){
                            hostn.deleteEdge(last);
                            last.deleteEdge(hostn);
                            hostn.addEdge(exchangen);
                        }
                    }
                    else{
                        hostn.addEdge(exchangen);
                        exchangen.addEdge(hostn);
                    }
                }
            }
        }
        for(int i  = 0 ; i < exchanges.size() ; i++){
                    //遍历交换机找到管理它的交换机和它管理的交换机
            Node exchange1 = exchanges.elementAt(i);
            for(int j = 0 ; j < exchanges.size() ; j++){
                if(j != i){
                    Node exchange2 = exchanges.elementAt(j);
                    if(exchange2.isSameSubnet(exchange1.getMainIp())){
                        // 接下来是管理和被管理的关系
                        Vector<Edge> edges = exchange1.getEdges();
                        for(int k = 0 ; k < edges.size() ; k++){
                            Node tmp = edges.elementAt(k).getDest();
                            if(tmp.getType() == NodeType.exchange){
                                // 如果是交换机又在同一个网段里，根据范围更新
                                if(IP.compare(tmp.getIps().elementAt(0).getIpNetMask(), exchange2.getIps().elementAt(0).getIpNetMask()) == 1){
                                    exchange1.deleteEdge(tmp);
                                    tmp.deleteEdge(exchange1);
                                }
                            }
                        }
                    }
                }
            }
        }
        if(exchanges.size() == 0 && hosts.size() != 0){
            // 如果发现不了交换机，说明交换机没有IP地址只是一个二层交换机而路由器是无法直接连到交换机上的，所以这里需要fake一个交换机
            Node fake = new Node("0.0.0.0", NodeType.exchange);
            for(int i = 0 ; i < hosts.size() ; i ++){
                Node host = hosts.elementAt(i);
                host.addEdge(fake);
                fake.addEdge(host);
            }
            exchanges.add(fake);
        }

        // 设备和交换机的连接信息都已经设置完毕，接下来就是添加到graph中
        for(int i = 0 ; i < hosts.size() ; i++){
                    Node host = hosts.elementAt(i);
                    host.setIndex(graph.getNodeNum());
                    graph.addNode(host);
        }
        for(int i = 0 ; i < exchanges.size() ; i++){
                    Node exchange = exchanges.elementAt(i);
                    exchange.setIndex(graph.getNodeNum());
                    graph.addNode(exchange);
            // 因为这些ip都是通过路由器的地址转换表得到的，一定是相连的
            graph.addEdge(n.getIndex(), exchange.getIndex());
        }

    }

    /*private static void dfs(Node n, Graph graph){
        // tmd，直接用地址转换表不就行了
        // 从路由器开始
        SnmpServer t = SnmpServerCreater.getServer(n.getMainIp());
        if(n.getType() == NodeType.host){
            // 对于主机不需要继续进行搜索
            return ;
        }
        // 接下来就是路由器或者交换机了，类型可以通过很多方法判断出来
        String[] linkedips = t.getLinkedIp();
        for(int i = 0 ; i < linkedips.length ; i++){
            // 对于每一个相连的IP
            String ip = linkedips[i];
            if(graph.isRepeated(ip))
                continue;
            SnmpServer server = SnmpServerCreater.getServer(ip);
            Node newn = new Node(ip, NodeType.other, graph.getNodeNum());
            graph.addNode(newn);
            if(server.getDeviceType() == DeviceType.host){
                newn.setType(NodeType.host);
            }
            // 需要区分交换机和路由器
            // 区分方法就是交换机中有路由器的地址，额路由器中没有相连的交换机的地址，因此所有在路由表中出现过的地址，它对应的设备的类型一定是路由器
            // 但是目前来看交换机和路由器没有区别，所以这个方法在之后进行，目前先设置为other;
            graph.addEdge(n.getIndex(), newn.getIndex());
            graph.addEdge(newn.getIndex(), n.getIndex());
            // 同时可以得到自己的所有IP
            Vector<IP> ownips = server.getOwnIp();
            newn.setIps(ownips);
            dfs(newn, graph);
        }
    }*/

    private static void dfs(Node n, Graph graph){

        // 现在需要通过snmp服务器获得一些信息
        // 类型
        if(n.getType() != NodeType.gateway)
            // 非网关不需要继续深度遍历
            return ;
        SnmpServer t = SnmpServerCreater.getServer(n.getMainIp());
        DeviceType type = t.getDeviceType();
        // 对于同一个路由器来说，不要重复查询多个相同子网
        Vector<String> subnetips = new Vector<>();
        // 首先是自己的ip信息
        Vector<IP> ips = t.getOwnIp();
        graph.setIps(n, ips);
        // 获得路由表，通过路由表获得它所有相连节点
        IPRoute[] iproutes = t.getIpRoute();
        // 获得路由表后，需要区分直接路由和间接路由，直接对应了子网，间接路由对应了三层交换机或者路由器
        // 必须先获取路由器，然后获取交换机和主机，所以这里路由表必须遍历两次，先把所有路由器给得到
        for(int i = 0 ; i < iproutes.length ; i++){
            IPRoute ipr = iproutes[i];
            if(ipr.getIpRouteType() == IPRouteType.invalid)
                continue;
            if(ipr.getIpRouteType() == IPRouteType.direct) {
                // 直接相连还要分情况
                String nexthop = ipr.getIpRouteNextHop();
                // 首先去掉这些对生成网络拓扑无用的情况
                if(nexthop.equals("127.0.0.1") || nexthop.equals("0.0.0.0"))
                    continue;
                if(n.isRepeated(nexthop)){
                    // 查看下一跳地址是否是它自己的，如果是，那么查看对应的destmask
                   continue;
                }
                else {
                    if (!graph.isRepeated(ipr.getIpRouteDest())) {
                        if (ipr.getIpRouteMask().equals("255.255.255.255")) {
                            // 说明可能有直连路由器，那么此时需要检验dest的合法性
                            if (!IP.isAllOne(ipr.getIpRouteDest(), ipr.getIpRouteMask())) {
                                Node newn = new Node(ipr.getIpRouteDest(), NodeType.gateway, graph.getNodeNum());
                                graph.addNode(newn);
                                graph.addEdge(n.getIndex(), newn.getIndex());
                            }
                        } else {
                            // 说明是子网，因为子网号不可能为全1
                            // 接下来需要解析这个子网
                            continue;
                            //parseSubnet(n, graph, ipr.getIpRouteDest(), ipr.getIpRouteMask());
                        }
                    }
                }
            }
            else{
                String nexthop = ipr.getIpRouteNextHop();
                if (!graph.isRepeated(nexthop)) {
                    // IP地址不重复，那么相当于发现新设备
                    Node newn = new Node(nexthop, NodeType.gateway,  graph.getNodeNum());
                    graph.addNode(newn);
                    graph.addEdge(n.getIndex(), newn.getIndex());
                }
            }
        }

        for(int i = 0 ; i < iproutes.length ; i++){
            IPRoute ipr = iproutes[i];
            if(ipr.getIpRouteType() == IPRouteType.direct) {
                // 直接相连还要分情况
                String nexthop = ipr.getIpRouteNextHop();
                // 首先去掉这些对生成网络拓扑无用的情况
                if(nexthop.equals("127.0.0.1") || nexthop.equals("0.0.0.0"))
                    continue;
                if(n.isRepeated(nexthop)){
                    // 查看下一跳地址是否是它自己的，如果是，那么查看对应的destmask
                    if(ipr.getIpRouteMask().equals("255.255.255.255")){
                        continue;
                    }
                    else{
                        boolean flag = true;
                        for(int j = 0 ; j < subnetips.size() ; j++){
                            if(subnetips.elementAt(j).equals(ipr.getIpRouteDest())){
                                flag = false;
                                break;
                            }
                        }
                        if(flag){
                            parseSubnet(n, graph, ipr.getIpRouteDest(), ipr.getIpRouteMask());
                            subnetips.add(ipr.getIpRouteDest());
                        }
                    }
                }
                else {
                    if (!graph.isRepeated(ipr.getIpRouteDest())) {
                        if (ipr.getIpRouteMask().equals("255.255.255.255")) {
                            // 说明可能有直连路由器，那么此时需要检验dest的合法性
                            if (!IP.isAllOne(ipr.getIpRouteDest(), ipr.getIpRouteMask())) {
                                continue;
                            }
                        } else {
                            // 说明是子网，因为子网号不可能为全1
                            // 接下来需要解析这个子网
                            boolean flag = true;
                            for(int j = 0 ; j < subnetips.size() ; j++){
                                if(subnetips.elementAt(j).equals(ipr.getIpRouteDest())){
                                    flag = false;
                                    break;
                                }
                            }
                            if(flag){
                                parseSubnet(n, graph, ipr.getIpRouteDest(), ipr.getIpRouteMask());
                                subnetips.add(ipr.getIpRouteDest());
                            }
                        }
                    }
                }
            }
            else{
                String nexthop = ipr.getIpRouteNextHop();
                if (!graph.isRepeated(nexthop)) {
                    // IP地址不重复，那么相当于发现新设备
                    continue;
                }
            }
        }
        // 获取到了连线后，开始深度优先
        Vector<Edge> edges = graph.getNbrs(n.getIndex());
        for(int i = 0 ; i < edges.size() ; i ++){
            Edge e = edges.elementAt(i);
            if(e.getStatus() == 0 && e.getDest().getType() == NodeType.gateway){
                // 反过来也要设置
                e.setStatus(1);
                graph.getNbrs(e.getDestIndex()).elementAt(n.getIndex()).setStatus(1);
                dfs(graph.getNode(e.getDestIndex()), graph);
            }
        }
    }
}
