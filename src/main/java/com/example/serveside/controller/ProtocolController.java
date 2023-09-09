package com.example.serveside.controller;

import com.example.serveside.response.GanttInformation;
import com.example.serveside.request.ConfigurationInformation;

import com.example.serveside.response.ToTalInformation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/* @CrossOrigin 注解：允许进行跨域*/
@Controller
@CrossOrigin
@RequestMapping(path = "/api")
public class ProtocolController {

    /*
    * 该函数接受响应，把 MSRP 的运行结果以及任务对应的信息打包成 JSON 形式然后传递给前端
    * */
    @ResponseBody // @ResponseBody注解将 java 对象转化为 json 返回给前端
    @PostMapping(value = "/msrp")
    public ToTalInformation MSRP(ConfigurationInformation requestInformation)
    {
//        以后这个需要往里面添加参数然后把信息传递给后端
        String[] args = new String[]{"Hello World", "SYSU"};
//        运行 MixedCriticalSystem 的 main 函数跑一下 msrp 下的模拟器
        com.example.serveside.service.msrp.entity.MixedCriticalSystem.main(args);

        return com.example.serveside.service.msrp.entity.MixedCriticalSystem.PackageGanttInformation();
    }
}
