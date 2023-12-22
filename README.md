# 混合关键系统模拟工具简介

在华为胡杨林项目的支持下，中山大学计算机学院RTS实验室研发了一款面向复杂混合关键实时系统资源共享协议的模拟工具。该模拟工具能够帮助用户全面评估不同条件下资源共享协议的性能表现、对比不同资源共享协议的优缺点、验证资源共享协议的正确性。这款工具提供以下三个功能：

- 以可视化形式展示任务全周期运行情况，即在甘特图上使用不同的颜色和符号来表示任务的状态（独立执行、等待资源、访问资源等）。这个功能有助于用户更好地理解指定资源共享协议运行规则，便捷对比不同资源共享协议的优缺点、深入了解系统任务之间的相互影响关系、及时发现系统潜在问题，如死锁和饥饿。
- 模拟指定任务最坏运行情况，即模拟任务的极端执行情况。通过模拟任务的极端执行情况，用户可以深入了解任务在最不利情况下的运行状况，有助于验证系统任务的响应时间，定位系统可调度性瓶颈并为资源共享协议调优提供关键信息。
- 调整系统运行参数。这项功能使用户能够模拟各种工作负载和资源配置，从而全面评估不同条件下资源共享协议的性能表现。



# 后端实现技术栈

混合关键系统模拟工具选用前后端分离的开发模式进行构建。本仓库存储后端代码，前端仓库地址为[RTS-SYSU/Mixed-Criticality-System-Simulator-Client-Side (github.com)](https://github.com/RTS-SYSU/Mixed-Criticality-System-Simulator-Client-Side)。混合关键系统模拟工具采用`Spring Boot`和`Java`来构建后端服务。



# 后端代码文件夹说明

在`Mixed-Criticality-System-Simulator-Client-Side`文件夹中，`src/main/java/com/example/serveside`存放后端服务代码，其他内容为IDEA项目自带文件。`src/main/java/com/example/serveside`文件夹的内容具体如下：

- `controller/ProtocolController.java`：`Spring Boot`中的控制器类，负责响应前端发起的请求并返回处理结果。
- `request/ConfigurationInformation.java`：前端向后端传递的混合关键系统模拟工具系统环境配置参数。
- `response`：后端向前端传递的一系列响应信息，包括任务信息、资源信息、任务运行情况等等。
- `service`：后端真正处理前端请求的代码，包括模拟任务的执行、查看任务的最坏运行情况等功能的实现。



# 后端代码编写环境

混合关键系统模拟工具的后端服务使用`Spring Boot`框架和`Java`语言，对应的版本如下：

- `Java`：1.8.0\_381
- `Spring Boot`：2.7.6



# 后端代码运行指南

后端代码的运行指南如下：

- 从本仓库下载代码至本地。
- 使用`IDEA`打开`Mixed-Criticality-System-Simulator-Serve-Side`文件夹。
- `IDEA`会自动根据`pom.xml`文件下载项目所需的依赖项。
- 运行后端代码。



# 前后端整合

若想要将前端代码和后端代码整合成一个应用，按以下步骤执行：

- 控制台在前端代码文件夹路径中执行命令`npm run electron:build`，将前端界面打包成一个应用并存储在`dist_electron`文件夹中。

- 使用`IDEA`打开后端代码文件夹，使用`Maven`插件将项目打包成`jar`包，存储在`target`文件夹中。

- 复制打包好的`jar`包`serve-side-0.0.1-SNAPSHOT.jar`到前端代码文件夹下的`dist_electron`文件夹中。

- 往`dist_electron`文件夹中创建`Simulator.bat`文件，写入以下内容，然后运行即可。

  ```bat
      start "serve" javaw -jar serve-side-0.0.1-SNAPSHOT.jar
      
      timeout /t 2
      
      @REM 开启前端界面
      start "client" "client-side Setup 0.1.0.exe"
      
      @REM 等前端界面执行完再终止后端服务 
      :WAIT_CLIENT
      timeout /t 5 /nobreak >nul
      tasklist | find "client-side.exe" 
      echo %errorlevel%
      if %errorlevel%==0 (
          goto WAIT_CLIENT
      )
      taskkill /f /im "javaw.exe"
  ```

  
