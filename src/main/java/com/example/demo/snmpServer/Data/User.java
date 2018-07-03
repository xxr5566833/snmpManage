package com.example.demo.snmpServer.Data;

public class User {
    int phoneNumber;
    String password;
    int power;
    public User(int phoneNumber,String password,int power){
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.power = power;
    }

    public int getPower() {
        return power;
    }

    public int getPhoneNumber() {
        return phoneNumber;
    }
}
