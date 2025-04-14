# PDR安卓手机定位APP（开发版文档）
<!--Markdown渲染提示-->
<!--Android Studio一般无法直接将Markdown文件在右侧渲染-->
<!--需要在File->Settings->Plugins中搜索安装Markdown Editor插件-->
<!--同时需要安装JCEF, 双击shift, 搜索Choose Boot java Runtime, 在new:中选择最新版本安装-->
<!--最后，重启Android Studio后即可打开并渲染md文档-->
<!--蓝色字体为链接，可以点击跳转-->
### 目录

1.[开发规范](#开发规范)

2.[项目介绍](#项目介绍)

3.[项目结构](#项目结构)

4.[功能模块](#功能模块)

5.[Git使用](#Git使用)

---

## 开发规范

1.[设置作者信息](#设置作者信息)

2.[代码风格](#代码风格)

### 设置作者信息

每新建一个文档时，需要在文档开头标注创建者、创建日期、创建时间。  
设置作者信息流程：  `File->Settings->Editor->File and Code Templates`  
左侧栏选择`includes->File Header`  
填写如下:  
```java
/**    
* @Author: Replace_Your_Name  
* @Date: ${DATA}  
* @Time: ${TIME}  
*/  
```
注意，这里只需要替换你的名字即可

### 代码风格

- 命名规范

    类名：采用UpperCamelCase风格（大驼峰命名法），如 StudentBook

    方法名、参数名、变量名：采用lowerCamelCase风格（小驼峰命名法), 如 bookNumber

    常量名：常量名全部大写，单词之间用下划线分隔, 如 PI_VALUE


- 代码格式

    单行字符不得超过120个，若超过需要换行


- 注释

    1. 在方法名前需要加文档注释，解释该方法的描述、输入、输出、异常  
    ```java
    /**
     * 方法的描述
     *
     * @param 参数1 描述参数1
     * @param 参数2 描述参数2
     * ...
     * @return 返回值描述
     * @throws 异常类型 异常描述
     */       
     ```          
  
    2. 定义变量需要在变量后加单行注释
    ```java
    int studentNum = 0;  // 学生数量 
    ```
  

---

## 项目介绍

本项目是一个安卓移动端PDR定位APP，使用手机自带的传感器数据（如加速度计，陀螺仪，
磁力计）作为输入，通过定位算法，获得定位结果。  
定位结果会显示在软件中的二维视图中。  
并且，为了之后的二次开发（如添加新的定位算法，添加新的传感器数据，添加三维视图等），本项目具备良好的可扩展性。

---

## 项目结构
PDR_Locator/  
├── app/  
│   ├── src/  
│   │   ├── main/  
│   │   │   ├── java/  
│   │   │   │   ├── com.example.pdr_locator/  
│   │   │   │   │   ├── model/  
│   │   │   │   │   ├── view/  
│   │   │   │   │   ├── controller/  
│   │   │   │   │   ├── algorithm/  
│   │   │   │   │   ├── assets/  
│   │   │   │   │   ├── sensor/  
│   │   │   │   │   ├── util/  
│   │   │   │   │   └── config/  
│   │   │   ├── res/  
│   │   │   ├── assets/  
│   │   │   │   └── model.pt  
│   │   │   └── AndroidManifest.xml  
│   ├── build.gradle  
└── build.gradle  

---

## 功能模块
1. [Model层](#Model层)
2. [View层](#View层)
3. [Controller层](#Controller层)
4. [Algorithm层](#Algorithm层)
5. [Assets层](#Assets层)
6. [Sensor层](#Sensor层)
7.  [Util层](#Util层)
8. [Config层](#Config层)

### Model层
- 职责：负责数据存储，包括实体类和model类
- 子模块：
  - Quat：存储一个四元数
  - QuatModel：处理四元数姿态估计以及更新
  - SensorData：存储IMU数据（加速度计、磁力计、陀螺仪）
  - SensorType：传感器类型枚举

### View层
- 职责：负责UI展示和用户交互
- 子模块：
  - MainActivity：主活动，负责启动应用和初始化各模块

### Controller层
- 职责：协调Model和View之间的交互，处理业务逻辑
- 子模块：
  - MainController：主控制器，负责数据采集、算法处理和UI更新
  - SensorController：管理传感器数据的采集和处理
  - AlgorithmController：管理算法的加载和运行
  - DisplayController：管理定位结果的显示逻辑

### Algorithm层
- 职责：实现具体定位算法
- 子模块：
  - IAlgorithm：算法接口，定义算法的基本方法
  - PdrLocalOriAlgorithm：目前已有的PdrLocalOri算法，若有其他算法可以继续添加子模块进行扩展
  - AlgorithmFactory：算法工厂，用于动态加载和切换算法

### Assets层
- 职责：存放模型的.pt文件

### Sensor层
- 职责：负责IMU传感器的管理
- 子模块：
  - ISensor：传感器管理接口，定义传感器的基本操作
  - AccelerometerSensor：加速度计管理器
  - GyroscopeSensor：陀螺仪管理器
  - MagnetometerSensor：磁力计管理器
  - SensorConfig：传感器配置器
  - SensorDataCollector: 传感器数据采集器

### Util层
- 职责：提供工具类和辅助功能
- 子模块：
  - QuaternionUtil：四元数运算工具类
  - SlidingWindowManager：滑动窗口管理器工具类，用于生成消息队列任务

### Config层

- 职责：管理应用配置和参数
- 子模块：
  - APPConfig: 应用全局配置（如默认算法、采样率）
  - AlgorithmConfig: 算法相关配置
  - SensorConfig: 传感器相关配置

---

## Git使用

1. 基础操作  
  - `git init`：将当前目录变为Git仓库
  - `git clone <仓库URL>`：下载远程仓库到本地
  - `git status`：显示工作区与暂存区文件状态
  - `git add <文件名>`：将指定文件添加到暂存区
  - `git add .`：添加所有新文件和修改文件到暂存区
  - `git commit -m "提交说明"`：将暂存区内容提交到本地仓库
  - `git log`：显示提交历史
  - `git log --online --graph`：单行显示提交历史，带分支图

2. 分支管理
  - `git branch`：列出本地分支 (-a查看所有分支)
  - `git branch <分支名>`：基于当前提交创建新分支
  - `git checkout <分支名>`：切换到指定分支
  - `git checkout -b <分支名>`：创建新分支并立即切换
  - `git merge <分支名>`：将指定分支合并到当前分支
  - `git branch -d <分支名>`：删除已合并的分支
  - `git branch -m <旧分支名> <新分支名>`：修改当前分支名称

3. 远程仓库
  - `git remote add <别名> <仓库URL>`：关联远程仓库（如origin）
  - `git remote -v`：显示已关联的远程仓库
  - `git pull <远程仓库别名> <分支名>`：拉取远程分支合并到当前分支
  - `git push <远程仓库别名> <分支名>`：将本地分支推送到远程仓库
  - `git push -f <远程仓库别名> <分支名>`：强制提交，覆盖远程提交历史

4. 冲突处理
  - 拉取时冲突：
    - 执行git pull后，编辑冲突文件内容（冲突标记为 <<<<<<< 和 >>>>>>>）
    - 解决冲突后，执行`git add <冲突文件名>`和`git commit -m "解决冲突""`
  - 合并时冲突：
    - 解决步骤同上，最后再执行`git merge --continue`



