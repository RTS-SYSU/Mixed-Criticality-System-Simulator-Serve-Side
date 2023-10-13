package com.example.serveside.controller;

import com.example.serveside.request.ConfigurationInformation;

import com.example.serveside.response.ToTalInformation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.example.serveside.response.SchedulableInformation;

/* @CrossOrigin 注解：允许进行跨域*/
@Controller
@CrossOrigin
@RequestMapping(path = "/api")
public class ProtocolController {

    /*
    * 该函数接受响应，把 MSRP 的运行结果以及任务对应的信息打包成 JSON 形式然后传递给前端
    * */
    @ResponseBody // @ResponseBody注解将 java 对象转化为 json 返回给前端
    @GetMapping(value = "/msrp")
    public ToTalInformation MSRP()
    {
        return com.example.serveside.service.msrp.entity.MixedCriticalSystem.PackageTotalInformation();
    }

    /*
    * 该函数接受响应，把 MrsP 的运行结果以及任务对应的信息打包成 JSON 形式然后传递给前端
    * */
    @ResponseBody
    @GetMapping(value = "/mrsp")
    public ToTalInformation MrsP()
    {
        return com.example.serveside.service.mrsp.entity.MixedCriticalSystem.PackageTotalInformation();
    }

    /*
    * 该函数接受前端发过来的系统环境配置，并返回三个协议是否可调度的结果
    * */
    @ResponseBody
    @PostMapping(value = "/isSchedulable")
    public SchedulableInformation IsSchedulable(ConfigurationInformation requestInformation)
    {
        return com.example.serveside.service.FrontInterfaces.SchedulableResult();
    }
}
