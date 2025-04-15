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

### 使用流程

本项目合作开发者初次使用时，流程如下:
- 本地初始化
  - 在空项目的Terminal中，使用`git init`新建本地git仓库。
  - 使用`git config --global user.email "邮箱"`和`git config --global user.name "用户名"`配置全局用户信息 
  - 使用`git remote add <别名> <仓库URL>`将本地仓库关联上远程仓库（记住这个别名，自己设置，一般是叫origin） 
  - 使用`git clone <仓库URL>`，初次下载远程仓库至本地 
  - 在本地开发一般不使用初始的master分支，使用`git branch <分支名>`创建自己的本地开发分支。
  - 使用`git push <远程仓库别名> HEAD:<新分支名>`，在远程仓库创建一个自己的分支（之后提交代码代码至远程仓库，
  使用`git push <远程仓库别名> <分支名>`提交至自己的分支）
  
- 拉取代码
  - 首先说明一下各分支的交互关系  远程仓库master分支 - 本地仓库master分支 - 本地仓库子分支 - 远程仓库子分支 - 远程仓库master分支，一个循环。
  我们开发代码后提交到本地仓库的子分支，每次开发前，需要让本地子分支与远程仓库master分支同步。  
  大致思路是：将远程仓库主分支代码拉取到本地仓库主分支，
  将本地仓库主分支与本地仓库的开发子分支合并（若合并过程有冲突则解决冲突）。冲突修改后，将修改后的代码提交至本地开发子分支，然后就可以开始开发。
  - 因此，每次开发代码前，需要做的流程如下：
    - 第一，使用`git checkout <分支名>`切换到本地开发子分支，之后使用`git add .`和`git commit -m "提交说明"`将未提交的代码先保存在本地仓库开发子分支（其实一般不会有修改需要提交，因为如果严格按照
    开发前拉取、开发完提交来做，在每次拉取前，就不该有已经修改但未提交的文件，但为了防止因疏漏没有按要求做，还是要执行一下这两行保存修改）
    - 第二，使用`git checkout <分支名>`切换到本地master分支，之后使用`git pull <远程仓库别名> <分支名>`将远程仓库的主分支代码拉取至本地master分支（因为我们的本地master分支代码是干净的，
    即我们不会在本地master分支进行代码修改，所以不会发生冲突，拉取代码就类似直接用远程仓库master分支覆盖了本地master分支）。
    - 第三，使用`git checkout <分支名>`切换到本地开发子分支，之后使用`git merge <分支名>`将本地master分支代码合并到本地开发子分支。
    - 第四，如果发生冲突，按下面的冲突解决方法来解决。
- 提交代码
  - 提交代码大致思路是：开发过程中，每完成一个文件，就将文件提交到本地仓库开发子分支一次；或者一天开发结束时，对每个修改过的文件进行单独提交到本地开发子分支一次
    (这是因为commit会有一个提交说明，如果所有修改文件一起提交，就只有一个提交说明)。当完成一天的代码开发后，需要进行一次本地开发子分支到远程仓库子分支的提交。
    最后，在github上提交一个Pull request（PR）,请求合并到主分支（但不是每天都要提交，当自己的功能完成时提交一次PR,因为要确保远程仓库的master分支是一个可用的版本）
  - 因此，每次开发代码完后，需要做的流程如下：
    - 第一，切换到本地开发分支，使用`git add <文件名>`将指定文件添加到暂存区
    - 第二，使用`git commit -m "提交说明"`将暂存区内容提交到本地仓库开发分支
    - 第三，重复第一和第二，直至将所有修改过的文件提交到本地开发分支
    - 第四，使用`git push <远程仓库别名> <分支名>`将本地仓库开发子分支内容推送到远程仓库的对应子分支
    - 第五，当代码没有bug且实现完全功能后，在github上提交一个PR，否则不用执行第五步

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
  - `git branch`：列出本地分支 (-a查看所有分支，包括远程和本地存在的分支)
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



