# snmp网络管理软件 说明文档

> 编写人:xxr 2018.8.5

## 1. 项目综述

### 1.1 项目概述

本项目利用基于SNMP网管协议，实现对网络设备（包括主机，路由器，交换机）的基本管理。

### 1.2 简单网络管理协议(SNMP)介绍

简单网络管理协议（SNMP，Simple Network Management Protocol）构成了互联网工程工作小组（IETF，Internet Engineering Task Force）定义的Internet协议族的一部分。该协议能够支持网络管理系统，用以监测连接到网络上的设备是否有任何引起管理上关注的情况。它由一组网络管理的标准组成，包含一个应用层协议（application layer protocol）、数据库模型（database schema），和一组数据对象。 在典型的SNMP用法中，有许多系统被管理，而且是有一或多个系统在管理它们。每一个被管理的系统上有运行一个叫做代理者（agent）的软件组件，且通过SNMP对管理系统报告信息。

基本上，SNMP代理者以变量呈现管理数据。管理系统通过**GET**，**GETNEXT**和**GETBULK**协议指令取回信息，或是代理者在没有被询问的情况下，使用**TRAP**或**INFORM**发送数据。管理系统也可以发送配置更新或控制的请求，通过**SET**协议指令达到主动管理系统的目的。配置和控制指令只有当网络基本结构需要改变的时候使用，而监控指令则通常是常态性的工作。
可通过SNMP访问的变量以层次结构的方式结合。这些分层和其他元数据（例如变量的类型和描述）以管理信息库（MIBs）的方式描述。 一个SNMP管理的网络由下列三个关键组件组成： 
- 网络管理系统（NMSs，Network-management systems） 
- 被管理的设备（managed device） 
- 代理者（agent）

一个网络管理系统运行应用程序，以该应用程序监视并控制被管理的设备。也称为**管理实体**（managing entity），网络管理员在这儿与网络设备进行交互。网络管理系统提供网络管理需要的大量运算和记忆资源。一个被管理的网络可能存在一个以上的网络管理系统。 一个被管理的设备是一个网络节点，它包含一个存在于被管理的网络中的SNMP代理者。被管理的设备通过**管理信息库**（MIB）收集并存储管理信息，并且让网络管理系统能够通过SNMP代理者获取这项信息。 代理者是一种存在于被管理的设备中的网络管理软件模块。代理者控制本地机器的管理信息，以和SNMP兼容的格式发送这项信息。

从体系结构上来讲，SNMP框架由主代理、子代理和管理站组成。

* 主代理

主代理是一个在可运行SNMP的网络组件上运作的软件，可回应从管理站发出的SNMP要求。它的角色类似客户端/服务器结构（Client/Server）术语中的服务器。主代理依赖子代理提供有关特定功能的管理信息。 如果系统当前拥有多个可管理的子系统，主代理就会传递它从一个或多个子代理处收到的请求。这些子代理在一个子系统以及对那个子系统进行监测和管理操作的接口内为关心的对象建模。主代理和子代理的角色可以合并，在这种情况下我们可以简单的称之为代理（agent）。

* 子代理

子代理是一个在可运行SNMP的网络组件上运作的软件，运行在特定子系统的特定管理信息库（MIB，Management Information Base）中定义的信息和管理功能。子代理的一些能力有：

* 搜集主代理的信息
* 配置主代理的参数
* 回应管理者的要求
* 产生警告或陷阱

对协议和管理信息结构的良好分离使得使用SNMP来监测和管理同一网络内上百的不同子系统非常简单。MIB模型运行管理OSI参考模型的所有层，并可以扩展至诸如数据库，电子邮件以及J2EE参考模型之类的应用。


* 管理站

管理者或者管理站提供第三个组件。它和一个客户端/服务器结构下的客户端一样工作。它根据一个管理员或应用程序的行为发出管理操作的请求，也接收从代理处获得的TRAP。

以上是对SNMP协议的大致介绍

### 1.3 项目技术选型

#### 1.3.1 前端技术选择

本项目前端使用`Vue.js`作前端展示页面的主要框架，并且为了增加展示效果使用	`bootstrap`框架。

<div align=center>
![](/img/vue.png)
</div>


[Vue](https://cn.vuejs.org/)是一套用于构建用户界面的渐进式框架。与其它大型框架不同的是，Vue 被设计为可以自底向上逐层应用。Vue 的核心库只关注视图层，不仅易于上手，还便于与第三方库或既有项目整合。另一方面，当与现代化的工具链以及各种支持类库结合使用时，Vue 也完全能够为复杂的单页应用提供驱动。

<div align=center>
![](/img/bootstrap.jpg)
</div>

[bootstrap](https://getbootstrap.com/)是一个简洁、直观、强悍的前端开发框架，让web开发更迅速、简单。

#### 1.3.2 后端技术选择

后端是基于Java的[Snmp4j](http://www.snmp4j.org/)软件包实现对数据的获取，修改的。

<div align=center>
![](/img/SNMP4J.png)
</div>

#### 1.3.3 前后端交互技术选择

前端命令的发出是通过[axios](https://www.npmjs.com/package/axios)实现的，而后端对命令的接收和回复是通过[Springboot](https://spring.io/)实现的
<div align=center>
![](/img/springboot.png)
</div>

#### 1.3.4 整体技术框架

综上，整个程序的大致技术框架如下：

<div align=center>
![](/img/programStructure.jpg)
</div>

## 2 项目需求分析

我们以指导老师给出的需求作为导向进行开发，因为时间有限，目前我们已经完成的需求如下：

* 实现对主机的基本管理
* 实现网络拓扑的自动发现
* 实现流量监控
* 网络拓扑中任意主机之间是否有连接
* 对网络设备的基本信息进行展示和管理


## 3. 功能分析

### 3.1 基本功能概述

<div align=center>
![](/img/basicFunctions.jpg)
</div>

可以看到，我们实现的功能大致分为：

* 网络设备基本信息的展示，下面详述
* 网络拓扑发现
* 流量监控
* TCP连接的检测

下面对每一个功能进行详细展示和阐述

### 3.2 基本信息展示

我们目前支持的网络设备有三种：

* 交换机（仅支持有IP地址的三层交换机）
* 路由器
* 主机
<div align=center>
![](/img/basicInfomation3.jpg)
</div>
如上图，对于这三种设备，它们可以共同展示的信息有：

* 基本系统信息展示，下面详述
* 端口信息展示与管理
* 路由表信息的展示
* 地址转换表信息的展示
* vlan信息的展示（对于主机没有Vlan）

对于主机，我们还支持：

* 硬盘信息展示
* cpu使用率显示
* 进程信息的展示

下面对每一种信息展示做详细说明和展示

#### 3.2.1 基本系统信息展示

基本系统信息主要包括以下几个方面：

- 系统名称
- 系统IP
- 联系人
- 设备所在位置（如果设置过的话）
- 设备的OID
- 设备在启动后经过的时间
- 系统描述
- CPU利用率（仅主机可以获取）

展示结果：

<div align=center>
![](/img/system.jpg)
</div>

#### 3.2.2 端口信息的展示和管理

端口信息主要包括：

- 端口名称
- 端口状态
- 所属的Vlan
- 入流量
- 出流量
- 物理地址
- MTU

我们还可以设置一些端口的状态，实际的效果就是这个端口是否被shutdown。当然仅仅只能设置已经连接的端口的状态，因为对于本身无连接的端口我们也不可能设置它的状态为UP。

下面是展示结果：

<div align=center>
![](/img/interface.jpg)
</div>

#### 3.2.3 路由表信息的展示

我们可以展示的路由表信息包括：

- 目标IP地址
- 子网掩码
- 下一跳地址
- 协议类型
- 花费
- Age

展示结果如下：

<div align=center>
![](/img/routingtable.jpg)
</div>

#### 3.2.4 地址转换表信息的展示

对于地址转换表，我们展示：

- 对应的端口index
- 物理地址
- IP地址
- 类型

展示结果如下：

<div align=center>
![](/img/translationtable.jpg)
</div>

#### 3.2.5 Vlan信息的展示

我们展示vlan的以下信息：

* vlan名
* vlan状态
* 包含哪些端口号
* 入流量
* 出流量
* MTU
* 物理地址
* 距离上次改变的时间

同样的，我们可以设置vlan是否被shutdown

展示结果如下：

<div align=center>
![](/img/vlan.jpg)
</div>

#### 3.2.6 磁盘信息的展示（仅主机支持）

对于磁盘，我们主要展示这些信息：

- 描述
- 磁盘空间单元
- 使用率
- 总大小

展示结果如下：

<div align=center>
![](/img/disk.jpg)
</div>

#### 3.2.7 进程信息的展示（仅主机支持）

我们同样可以展示主机上进程的基本信息：

- 名称
- 进程类型
- 进程的运行状态

展示结果如下：

<div align=center>
![](/img/process.jpg)
</div>

到这里，所有的基本信息的展示功能就介绍完毕了。

### 3.3 网络拓扑发现

我们实现了网络拓扑发现功能，主要包括以下几个基本功能：

- 用户输入一个起始IP地址后，算法会自动帮助用户搜索出整个网络拓扑图，并通过动画和文字说明把发现的顺序展示出来
- 在经过一次拓扑发现后，如果个别网络设备因为人为或者意外连接中断后，再次拓扑发现后，可以向用户报告那些“失去连接”的设备，即这些设备在上一次拓扑发现时是可以检测到的，但是在后一次拓扑发现却没有找到，说明这台设备可能连接上有了故障，方便用户调试

展示效果如下：

<div align=center>
![](/img/graphSearch1.jpg)
</div>

此时用户已经输入了初始的IP，点击按钮即可开始搜索

<div align=center>
![](/img/graphSearch2.jpg)
</div>

可以看到上面的文字框展示了网络拓扑发现的顺序，下面的拓扑图已经连接好了。此时我们断开一个设备的连接，并再一次拓扑发现。

<div align=center>
![](/img/graphSearch3.jpg)
</div>

# 可以看到效果。（待修改）

### 3.4 流量监控

目前，我们实现了对 每个状态为UP的端口/整个端口的入流量和出流量的展示，展示效果如下：

<div align=center>
![](/img/flow.jpg)
</div>

### 3.5 TCP连接的检测

对于网络拓扑中的每一台主机，我们可以检测每一台主机之间是否存在TCP连接。

<div align=center>
![](/img/tcp.jpg)
</div>

## 4. 技术分析

上面介绍完了程序所实现的功能，下面详细介绍一下整个项目实现所用到的关键技术，关键算法，关键逻辑以及程序架构。

之前说到我们的程序架构大致是这样的：
![](/img/programStructure.jpg)
下面也将按照这个顺序进行介绍。

### 4.1 前端技术分析

正如一开始所说，我们使用了**Vue.js**作为我们前端的主要实现框架，并使用**bootstrap**配合实现一些更好的UI效果。

#### 4.1.1 页面组织方式

这是前端页面的组织方式：

<div align=center>
![](/img/frontend.jpg)
</div>

实现页面之间的切换是通过vue的**router**功能，具体可以参考[vue的官方文档](https://cn.vuejs.org/v2/guide/)，我们的项目中，封装router功能的文件是 src\router\index.js，可以自己查看

#### 4.1.2 前端请求的封装

前端需要向后端服务器发送请求以实现相应的功能，我们把前端所有可能与后端交互的请求方法封装为api，并在src\api\api.js中保存，下面我把每个api以及用到的参数做一说明：

|api名称|方式|url|参数|说明|
|:--|:--|:--|:--|:--|
|getDisks|post|/getDisks|ip, community|获取磁盘信息|
|getTranslationTable|post|/getTranslationTable|ip, community| 获取地址转换表信息|
|getProcesses|post|/getProcesses|ip, community|获取进程信息|
|getInterface|post|/getInterface|ip, community|获取接口信息|
|getDeviceType|post|/getDeviceType|ip, community|判断设备是否是主机|
|getNetGraph|post|/getNetGraph|ip, community|获取网络拓扑图相关信息|
|getVlan|post|/getVlan|ip, community|获取vlan信息|
|getRoutingTable|post|/getRoutingTable|ip, community|获取路由表信息|
|updateStatus|post|/updateStatus|ip, community|更新所有端口的UP/DOWN信息|
|setAdminStatus|post|/setAdminStatus|ip, community, status, index|设置端口状态|
|getTCP|get|/getTCP|ip, community|获取tcp连接信息|
|getFlow|get|/getFlow|ip, community|获取流量信息|

### 4.2 前后端交互技术分析

#### 4.2.1 axios介绍

我们使用axios实现前端命令的发出，这些api都封装在src\api\api.js文件中，可以自行查看，很好理解，关于axois更多的使用方法，可以查看[中文文档](https://www.kancloud.cn/yunye/axios/234845)。

#### 4.2.2 后端Springboot介绍

我们使用Springboot作为后端的开发框架，Springboot可以把前端发来的命令**映射**为相应的方法，传递前端发来的参数，最后可以返回所需要的数据或结果。

举一个例子：

	@RequestMapping("/getTranslationTable")
    public AddressTranslation[] getTranslationTable(@RequestBody Map datamap){
        String ip = (String)datamap.get("ip");
        String readcommunity = (String)datamap.get("readcommunity");
        String writecommunity = (String)datamap.get("writecommunity");
        SnmpServer t = creater.getServer(ip, readcommunity,writecommunity);
        return t.getATtable();
    }
对这段代码做一个解释：

* `@RequestMapping("/getTranslationTable")`把/getTranslationTable映射为它下面的这个方法`getTranslationTable`
* 前端传来的参数会赋给`datamap`，作为后端方法的参数
* 后端的返回值会传递给前端

这是对前后端交互的一个说明实例，想要了解更多的Springboot的使用方法可以查看[官方文档](https://docs.spring.io/spring-boot/docs/current/reference/)或者博客。有很多资料可以参考。

### 4.3 后端技术分析

本项目是基于[Snmp4j](http://www.snmp4j.org/)开发包完成对各种数据的获取管理，逻辑判断等。

#### 4.3.1 后端文件结构

* `Controller.java` 记录所有前后端交互用到的api
* `CorsConfig.java` 与跨域请求有关
* `DemoApplication.java` 项目运行入口
* `snmpServer`文件夹
	* `Data`文件夹保存了需要用到的所有数据结构，这里不展开了
	* `SnmpServer.java`实现了基本Snmp功能的封装
	* `SnmpServerCreater.java`负责创建Snmp服务器

* `Graph`文件夹
	* `Graph.java`是拓扑图类
	* `GraphCreator.java`是拓扑图的创建类
	* 其他都是一些拓扑图需要用到的数据结构

#### 4.3.2 后端代码基本架构

<div align=center>
![](/img/backend.jpg)
</div>

可以看到，后端代码其实可以分成三层：

* 基本的snmp4J提供的api
* 封装snmp4j的api后实现的四个基本api，包括:
	* `snmpget`方法，可以获取单个OID节点上的信息
	* `snmpgetSubTree`方法，可以获取一个OID子树上的信息
	* `snmpwalk`方法，可以获取尽可能多的（一个PDU最多可以承载的数据量）OID节点的信息
	* `snmpset`方法，可以对有写权限的OID节点进行set操作

* 通过这四个基本api实现的各种各样的功能，因为上面已经列举了，这里不再赘述

#### 4.3.3 四个基本api的详细说明

|方法名|参数|返回值|说明|
|:--|:--|:--|:--|
|getTreeNode|节点OID|OID节点对应的data|可以获取任意可读的节点处的data|
|getSubTree|OID， OID掩码长度|与该OID和掩码共同决定的同一OID范围内的所有data的值（不能超过一个PDU最多可以承载的量）|OID和IP地址类似，可以用这种方法获取指定范围内的data
|getBulk|OID|获取从这个OID开始遍历，尽可能多的获取最多的data信息|获取的data由一个PDU最大数据量所限制|
|SetStatus|OID， 想要设置的值|是否成功修改|仅能对开放写权限的节点进行修改|

#### 4.3.4 网络拓扑发现算法逻辑细节

网络拓扑发现算法实现用户在输入一个起始IP地址后，自动发现网络拓扑并保存相关的数据待前端获取。

它的大致实现逻辑如下：

<div align=center>
![](/img/graph.jpg)
</div> 

目前的不足之处在于：
* 拓扑发现算法仅仅只能用于有路由器的网络的拓扑结构的发现，对于无路由器，甚至交换机仅仅是二层交换机（没有自己的IP）的网络结构，因为Snmp必须要有IP地址的这个局限性，无法正确地生成相应的拓扑结构
* 对于实现了不同vlan但是却没有相应的IP地址的交换机，目前的算法会因为认为两个vlan是两个不同子网，而把这一个交换机识别为两个不同的交换机。


## 运行环境配置

1. 更新Chrome浏览器， 我们测试的版本是` 67.0.3396.99（正式版本） （32 位）`
2. 安装node.js环境，我们使用的是`node-v8.11.3-x64.msi`
3. 安装java环境，我们使用的是`jdk-8u171-windows-x64.exe`
4. 首先运行后端服务器， 在[github上](https://github.com/xxr5566833/snmpManage/releases/download/v1.0.0/demo-0.0.1-SNAPSHOT.jar)可以下载，下载后，使用命令行进入该jar所在路径，执行命令`java -jar demo-0.0.1-SNAPSHOT.jar`(当然可以重命名)，后端服务器就开始运行了
5. 然后运行前端，进入`web`文件夹，执行如下命令
	1. `npm install http-server -g` 这是全局安装http服务器（第一次执行需要这个命令， 因为是全局安装，故之后运行不需要这个命令）
	2. 在该文件夹下使用cmd执行`http-server `命令
	3. 之后在谷歌浏览器中输入[localhost:8080]()即可进入 
6. 如果想要使用本机`127.0.0.1`测试Snmp功能，还需要在本机上运行Snmp代理端程序，可以使用下面说的`net-snmp`的代理端程序，但Windows自带的代理端程序更好，具体设置的步骤可以参考[这篇博文](https://blog.csdn.net/xumajie88/article/details/18406763)，Windows自带的代理端程序个人感觉速度更快，功能更全，还不需要配置（只需对community做简单配置即可）

## 开发环境配置

这里顺便总结一下开发过程中可能用到的工具

1. Java后端开发IDE`Intelij IDEA`
2. 前端开发编辑器，我一般使用`Sublime` ，当然也可以选择`Webstorm`或者`VSCode`等编辑器
3. Snmp协议的相关工具，可以帮助你更好的理解Snmp，对开发有很大的帮助
	1. `net-snmp`，这里我使用的是`net-snmp-5.5.0-2.x64`，要根据自己的系统版本选择。这个工具可以帮助你在本机运行snmp代理端程序，便于你使用snmp协议对自己电脑做简单管理，而不一定必须处于与交换机，路由器等网络设备真实连接的环境。同时它还实现了`snmpget`,`snmpwalk`,`snmpset`等操作，便于对自己的程序运行状况做出准确判断。
	2. `MIB Browser`，这个工具很实用，它可以实现对相应设备MIB的OID节点信息的浏览和获取，方便你知道哪些OID是实现了的，内容是什么。

4. `git`等版本控制工具

