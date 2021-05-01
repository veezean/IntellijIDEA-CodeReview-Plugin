# Intellij IDEA代码检视插件/ Intellij IDEA CodeReview Plugin

**写在前面的话：**

> 这是一个业余时间写的Intellij IDEA的一个Code Review代码检视、代码评审的插件。当初开发的时候也仅是按照自己的习惯，写了这么个插件来辅助工作中的代码检视事务。没想到能得到这么多小伙伴的支持、最近抽空优化了下相关功能，加了个很多小伙伴提出的网络交互能力，希望能帮到大家~~


---


最近在搞代码Review，一直想找个简单易用、适合自己习惯的`Intellij IDEA`的代码检视插件，但是一直没找到。想起好几年前在HUAWEI使用过的一个Eclipse的code review插件的用起来挺顺手的，所以凭着脑海中一些零星的记忆，重新写了个Intellij IDEA上的Code Review插件。

借助此插件，可以`方便的在本地IDEA工具里面记录代码检视发现的问题或者需要备注的信息`，同时`支持将IDEA中的评审意见导出为Excel表格`，方便发送给其它同事导入到自己的IDEA中，实现双击跳转到对应的代码位置，进行问题的确认。

**主要功能：**
  * Alt+A快速添加注释
  * 行号旁边图标标识有检视意见的位置
  * 支持双击评审意见跳转到代码对应位置
  * 支持对评审意见的删除、修改
  * 支持评审意见内容导出为Excel表格
  * 支持将导出的Excel表格中的评审意见导入到IDEA中进行查看与管理。
  * 支持网络交互，适合团队协作场景使用（V2.0版本新增）


**使用方法&效果图演示如下：**

常规用法

![](assets/post_pics/README.md/use_guide_showcase.gif)


网络版本用法：

![](assets/post_pics/README.md/net_version_usage.gif)

>
> 可以使用下面测试环境服务器地址，体验下网络版本的能力：
> 服务器地址：  https://codereview.codingcoder.cn
> 用户名：      test
> 密码：        123456
>
> **提醒：  测试环境数据随时可能被删除，请勿使用测试环境作为常用环境。**
> 

**安装方法**

从release目录下，下载对应的插件版本，然后按照下面方法进行安装。

```
导航到 File | Settings | Plugins 页面，点击 Install plugin from disk
```

![](assets/post_pics/README.md/install_local_plugin_showcase.gif)

**配套服务端**

网络版本配套的服务端，参见如下地址获取：
[github仓库地址](https://github.com/veezean/CodeReviewServer)
[gitee仓库地址](https://gitee.com/veezean/CodeReviewServer)

**功版本变更记录：**

* **V2.0版本，2021-05-01更新：**
  1. 增加了好久前承诺要做的网络版本（配合服务端一起）
  2. 使用体验优化了下
  3. 解决了几个bug

* **V1.3版本，2021-04-24更新：**
  1. 部分已知bug修复
  2. 增加处理人、关联需求、关联版本等字段
  3. 代码优化

* **V1.2版本，2019-12-07更新：**
  1. 删除、清空等操作增加二次确认，防止误操作
  2. 优化左侧行标定位逻辑的精准度

* **V1.1版本，2019-10-08更新：**
  1. 项目维度独立开，同时打开多个IDEA的时候，相互review的内容互不干扰

* **V1.0版本，2019-10-04更新：**
  1. 支持Alt+A快速添加选中代码部分的评审意见
  2. 支持添加了评审意见的地方，在代码的左侧窗口行号旁边显示一个图标标识
  3. 支持双击评审意见，直接跳转到代码对应位置
  4. 支持评审意见导出Excel表格
