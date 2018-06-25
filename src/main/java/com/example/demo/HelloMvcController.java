package com.example.demo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
//@RequestMapping("/hello")
public class HelloMvcController {

    @RequestMapping("/home")
    public String homeHandler(){
        return "home";
    }

    @RequestMapping("/test")
    public String testHandler(){
        return "test";
    }

    /**
     * 使用JSON作为响应内容
     */

    @CrossOrigin(origins="*",maxAge=3600)
    @RequestMapping(value="/getperson/{personID}",method=RequestMethod.GET)
    public @ResponseBody Person getPerson(@PathVariable String personID) {
        Person p = new Person();
        p.setName("Eric");
        p.setSex("male");
        p.setId(personID);
        return p;
    }
}
class Person{
    public String name;
    public String sex;
    public String id;
    void setName(String name){
        this.name = name;
    }
    void setSex(String sex){
        this.sex = sex;
    }
    void setId(String id){
        this.id = id;
    }
    String getName(){
        return this.name;
    }
    String getSex(){
        return this.sex;
    }
    String getId(){
        return this.id;
    }
}
