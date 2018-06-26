package com.example.demo.snmpServer.Data;

//定义interface的type的枚举变量
public enum IFType{
    other(1),
    regular1822(2),
    hdh1822(3),
    ddn_x25(4),
    rfc877_x25(5),
    ethernet_csmacd(6),
    iso88023_csmacd(7),
    iso88024_tokenBus(8),
    iso88025_tokenRing(9),
    iso88026_man(10),
    starLan(11),
    proteon_10Mbit(12),
    proteon_80Mbit(13),
    hyperchannel(14),
    fddi(15),
    lapb(16),
    sdlc(17),
    ds1(18),
    e1(19),
    basicISDN(20),
    primaryISDN(21),
    propPointToPointSerial(22),
    ppp(23),
    softwareLoopback(24),
    eon(25),
    ethernet_3Mbit(26),
    nsip(27),
    slip(28),
    ultra(29),
    ds3(30),
    sip(31),
    frame_relay(32),
    ieee80211(71),
    tunnel(131);

    private int type;
    private IFType(int value){
        this.type = value;
    }
    public int getType(){
        return type;
    }
    //因为value值不连续，不能通过values下标索引，只能case了，真麻烦
    public static IFType int2Type(int type){
        switch(type){
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
            case 31:
            case 32:
                return IFType.values()[type - 1];
            case 71:
                return ieee80211;
            case 131:
                return tunnel;
            default:
                return other;
        }
    }

}