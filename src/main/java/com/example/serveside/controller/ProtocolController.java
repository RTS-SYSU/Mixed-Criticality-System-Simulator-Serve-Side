package com.example.serveside.controller;

import com.example.serveside.service.msrp.entity.MixedCriticalSystem;
import com.example.serveside.response.GanttInformation;
import com.example.serveside.response.TaskInformation;
import com.example.serveside.request.ConfigurationInformation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/* @CrossOrigin 注解：允许进行跨域*/
@Controller
@CrossOrigin
@RequestMapping(path = "/api")
public class ProtocolController {


    @ResponseBody // @ResponseBody注解将 java 对象转化为 json 返回给前端
    @PostMapping(value = "/msrp")
    public List<GanttInformation> MSRP(ConfigurationInformation requestInformation)
    {
//        手动测试接口
        List<TaskInformation> taskInformationList = new ArrayList<>();
        taskInformationList.add(new TaskInformation(0, 1, "normal-execution", 5, 10));
        taskInformationList.add(new TaskInformation(0, 1, "spinning", 20, 23));
        taskInformationList.add(new TaskInformation(0, 1, "access-resource", 24, 25));
        taskInformationList.add(new TaskInformation(0, 1, "normal-execution", 28, 30));

        List<GanttInformation> ganttInformations = new ArrayList<>();
        ganttInformations.add(new GanttInformation(taskInformationList));

        String[] args = new String[]{"Hello World", "SYSU"};
//        运行 MixedCriticalSystem 的 main 函数跑一下 msrp 下的模拟器
        MixedCriticalSystem.main(args);
        return ganttInformations;
    }


}
