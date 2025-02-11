# IDEA代码检视插件Code Review Helper(支持团队协同)

---

V4.2.1版本正式发布，更新内容： [点击查看](https://mp.weixin.qq.com/s/w-hL-pEbB8FbiAAvHCvDQg)

---


**写在前面的话：**

这是一个业余时间写的基于IDEA的Code Review代码检视插件。当初开发的时候也仅是按照自己的习惯，写了这么个插件来辅助工作中的代码检视事务，没想到开源&上线IDEA插件应用市场之后会得到那么多小伙伴的青睐与支持，也收到了很多小伙伴反馈的功能建议，尤其是对团队协同场景的强烈呼声。

经过多个版本迭代，本插件功能上进一步完善，支持自定义评审字段，满足不同团队的个性化诉求。同时，配套的服务端版本也完成开发发布上线，提供了团队中代码检视的一种更便捷的方式。

![](https://pics.codingcoder.cn/pics/202307222357867.png)

---

## 一种更简单高效的代码review体验

在我们的项目开发过程中，代码`review`是不可或缺的一个环节。虽然市面上已有一些成熟的代码`review`系统，或者是基于`git`提交记录进行的在线review操作，也许其功能更强大，但是使用上总是不够方便：

- 代码不同于小说审稿，纯文本类型的阅读式review模式，很难发现逻辑层面的问题
- 代码review完成之后，针对评审意见的逐个确认、跟踪闭环也比较麻烦
- 平时项目开发的时候没法同步记录发现的问题
- ...

对于程序员来说，`IDEA`中查看代码才是最佳模式，在IDEA中可以跳转、搜索、分析调用，然后才能检视出深层的代码逻辑问题。此外，平时开发过程中，如果写代码的时候发现一些问题点，如果可以直接在IDEA中记录下来，然后交由对应责任人去修改，这样的代码review体验岂不是更方便、更高效。

基于此想法，利用业余时间开发了IDEA配套的代码review插件，上到应用市场之后，也收获了相对比较高的评分，也收到很多同学的私信赞扬，说明程序员“**苦code review久矣**”！


当然，随着使用的同学数量增加，也收到越来越多的同学反馈希望加一个**团队协作能力**，这样可以方便团队内评审活动的开展。

于是，在原有的本地review功能基础上，增加了插件配套的服务端交互能力，这样就实现了团队内成员间代码review意见的管理、统计以及彼此的协同。团队协同版本的交互逻辑如下示意：

![](https://pics.codingcoder.cn/pics/202307230012353.png)

## What's New

2023年3月底的时候，我迎来了生命中的一位重要的小伙伴。在3月初的时候，打算刚好趁着3月底四月初的时候在月子中心期间，我休陪产假期间可以有时间将4.x版本以及配套的全新服务端开发完成。但是现实总是啪啪的打脸，一个高需求宝宝教会了我什么叫做“**Too young too simple**”... 还妄图陪产假期间写代码？哈哈哈...

庆幸的是，新版本虽迟但到，也算是完成了之前对很多小伙伴的承诺。在该版本中，全面完善了插件的自由定制能力与团队协作能力。

下面是4.x版本新增的主要功能：

```
1. 支持自行配置评审信息字段，按照配置的字段生成对应的评审记录
2. 支持切换中英文显示
3. 导出excel的数据表格支持设定每个字段的列宽，导出的结果更美观
4. 支持切换到网络版本（团队协作版本），对接新版本服务端，实现团队协同能力
5. 支持多个IDEA插件打开不同的项目
6. 众多细节bug修复
```

**注意**

> 由于V4.x开始使用了一些新API能力，所以仅支持版本为`2019.03`及以上的IDEA进行安装，低于此版本的无法安装。

## Code Review 插件主要功能

本插件构建的目标就是打造一款真正适合程序员使用习惯且简单易用的代码检视工具。主要的功能如下：

  * Alt+A快速添加注释
  * 行号旁边图标标识有检视意见的位置
  * 支持双击评审意见跳转到代码对应位置
  * 支持对评审意见的删除、修改
  * 支持评审意见内容导出为Excel表格
  * 支持将导出的Excel表格中的评审意见导入到IDEA中进行查看与管理。
  * 支持网络交互，适合团队协作场景使用
  * 支持评审字段的自行定制、或者团队统一定制

除了作为代码检视工具，本工具还可以作为IDEA中的一个便签记录使用，替代`TODO`注释的使用场景，避免TODO被误提交。

## 使用方法&效果演示


### 基本使用方法

1. 选中代码内容，然后`alt+a`可以打开添加评审意见的窗口，在窗口中添加评审信息后点击保存即可完成评审意见的添加。

![](https://pics.codingcoder.cn/pics/202307202207769.png)

2. 按住`alt`按钮，并点击对应记录，可以弹出确认窗口，可以对别人提的评审意见进行确认。

![](https://pics.codingcoder.cn/pics/202307202209616.png)

3. 双击评审意见表格中黄色的区域，可以直接在表格中修改对应字段的值；双击评审意见表格中白色区域，可以直接跳转到评审意见对应的代码位置

4. 代码中被提过评审意见的地方，代码行号旁边会有对应标识提醒（刚添加评审意见之后不会出现，必须要关闭当前类再次打开的时候才会出现）

![](https://pics.codingcoder.cn/pics/202307202208616.png)

5. 本地评审数据导出到Excel表格中：

![](https://pics.codingcoder.cn/pics/202307232109603.png)

6. 支持将本地Excel表格中内容导入到IDEA中，方便在IDEA中进行跳转查看意见内容

![](https://pics.codingcoder.cn/pics/202307232111848.png)

7. 自定义评审字段的内容。

`V4.0.1`开始支持评审字段内容的自定义能力，在设置中可以进行调整，定制符合您实际诉求的评审字段内容。

![](https://pics.codingcoder.cn/pics/202307232113878.png)

在字段自定义的界面中，对配置文件进行修改，增加或者删除字段值，修改完成之后点击保存即可。

![](https://pics.codingcoder.cn/pics/202307232114192.png)

**重要**
配置操作前，请先了解下配置字段中每个字段的具体含义，以免配置错误影响插件功能。具体说明，可以[点此了解](https://blog.codingcoder.cn/post/codereviewfieldmodifyhelper.html)

如果配置错误导致插件功能出现问题，您可以点击配置界面左下角的`恢复默认配置`按钮，恢复到插件默认状态，然后重新去修改配置即可。


### 网络版本使用方式

1. 打开idea插件界面进行网络版本配置：

点击`settings`按钮，打开设置界面，可以切换界面中英文显示，然后切换到`网络版本`：

![](https://pics.codingcoder.cn/pics/202307202157298.png)

在网络版本中，输入搭建好的服务端地址，点击`连接测试`成功后，再输入账号和密码，点击`登录测试`,验证成功后即可点击下方的`保存按钮`。

![](https://pics.codingcoder.cn/pics/202307202201121.png)

2. 设置完成后，会自动从服务端拉取已经配置好的评审字段配置信息、以及服务端的项目信息列表等。使用过程中用户也可以手动点击`同步配置`按钮，从服务端拉取最新的配置信息。

![](https://pics.codingcoder.cn/pics/202307202202880.png)


3. 点击`提交服务端`，可以将本地的评审意见内容提交到服务端（如果有配置webhook通知，会收到相关消息推送）

![](https://pics.codingcoder.cn/pics/202307202210345.png)

4. 选择具体项目以及拉取范围后，点击`服务端下载`可以从服务端拉取评审意见到本地IDEA中。比如别人给我提了评审意见，我可以拉取到自己的IDEA中，双击跳转到对应的代码位置，进行问题的确认处理，确认完成后，可以在本地IDEA中对评审意见进行答复，答复完成后提交本地数据到服务端，完成整个review过程的闭环。

![](https://pics.codingcoder.cn/pics/202307202212457.png)



## 如何获取插件

### 通过市场安装（推荐）

本插件已经提交到IDEA的Marketplace中，您可以通过`IDEA--settings--plugins`窗口，输入`code review`关键字，搜索到本插件，然后点击安装即可，后续有新版本也可以快捷一键更新。

![](https://pics.codingcoder.cn/pics/202307232022504.png)

> 如果本插件有帮助到你，如果你也觉得插件不错，欢迎到IDEA 插件主页上帮忙打个分。

从应用市场的评分与下载量来看，大家还是蛮认可本插件的。

![](https://pics.codingcoder.cn/pics/202307222357867.png)

### 本地安装

自行clone本仓库代码进行编译，或者直接从已发布版本中，下载最新的发布版本二进制文件，然后按照下面方法在IDEA中进行安装。

```
IDEA中，点击File > Settings > Plugins 页面，点击 Install plugin from disk
```

具体操作可参见下面引导图：

![](https://pics.codingcoder.cn/pics/202307202149710.gif)

### 安装完成打开界面

安装完成后，在IDEA主界面下方，可以看到CodeReview的页签：

![](https://pics.codingcoder.cn/pics/202307202156189.png)


## 配套服务端能力获取

如果需要在团队中使用本插件，并希望能够集中管理评审内容、彼此实现代码review协同能力，需要自行私有化部署对应的服务端服务。

您可以通过如下途径获取配套服务端：

[github仓库地址](https://github.com/veezean/CodeReviewServer)

[gitee仓库地址](https://gitee.com/veezean/CodeReviewServer)

具体服务端的部署与使用介绍，您可 [点此查看](https://blog.codingcoder.cn/post/codereviewserverdeploydoc.html)。


![](https://pics.codingcoder.cn/pics/202307230022440.png)

![](https://pics.codingcoder.cn/pics/202307230022600.png)

![](https://pics.codingcoder.cn/pics/202307230023029.png)

![](https://pics.codingcoder.cn/pics/202307230023684.png)


## 功版本变更记录

本插件上线以来一直在不断更新，作者重视您提的任何建议与使用感受，也在不遗余力的利用业余空闲时间来不断的升级迭代。详细版本变更记录，您可以  [点此查看](https://blog.codingcoder.cn/post/codereviewversions.html)

## 问题&建议

使用过程中，如果发现有bug或者有功能建议，欢迎提issue单，或者通过公众号`是vzn呀`联系到作者，获取更为及时的支持。

![](https://pics.codingcoder.cn/pics/202207091317876.png)

当然，如果觉得本软件帮助到了您的工作，也欢迎支持我继续更新维护下去~

![](https://pics.codingcoder.cn/pics/202307231540263.png)
