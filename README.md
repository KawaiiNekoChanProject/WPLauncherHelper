<div align="center"><h1>WPLauncherHelper</h1></div>

WPLauncherHelper 是一个使用 Kotlin/JVM 轻量的实现

**您必须合理地使用它, 项目贡献者及所有者 不会承担 使用者 滥用造成的责任**


## 功能列表

- 🟩 Mpay
    - 🟩 用户名密码登录
    - 🟩 手机号+一次性代码登录
    - 🟩 手机号+密码登录
- 🟩 4399
    - 🟨 创建账号 (受到速率限制)
    - 🟩 登录账号
    - 🟨 更改账号资料 (仅能更改一次)
    - 🟩 第三方游戏 Oauth 登陆
- 🟩 x19
    - 🟩 获取启动器最新版本 (x19_java_patchlist)
    - 🟩 登陆 (authentication-otp)
    - 🟩 心跳 (authentication/update)
    - 🟩 昵称
        - 🟩 初始化
        - 🟩 改名
    - 🟩 通用组件
        - 🟩 基本信息查看
        - 🟩 下载组件
        - 🟩 点赞/点踩
        - 🟩 评论
            - 🟩 查看
            - 🟩 发布
    - 🟩 单人模式云存档
        - 🟩 获取存档列表
        - 🟩 删除存档
        - 🟩 获取下载链接
        - 🟩 上传存档
    - 🟩 租赁服
        - 🟩 获取直连IP
    - 🟩 网络游戏
        - 🟩 创建角色
        - 🟩 获取直连IP
    - 🟩 在线联机
        - 🟩 搜索房间
        - 🟩 加入房间
        - 🟩 获取房间直连IP
        - 🟩 获取房间内成员列表
    - 🟨 好友系统
        - 🟩 搜索好友
        - 🟩 主动添加好友
        - 🟩 同意/拒绝好友申请
        - 🟩 查看好友列表
        - 🟩 删除好友
        - 🟩 设置在线状态
        - 🟥 发送/接收好友消息
    - 🟩 图床
        - 🟩 上传图片并获取图片直链 (需审核)
    - 🟩 游戏内
        - 🟥 与认证服务器通信以进服 (会被砍成血雾 暂不开源相关代码)
        - 🟩 根据 **启动器账号 EntityId** 获取玩家皮肤
    - 🟩 算法
        - 🟩 根据 **UUID** 计算 **启动器账号 EntityId**
- 🟥 g79

## 作为库使用

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    // 把 ${version} 替换成最新版本
    implementation("io.github.kawaiinekochanproject:wplauncher-helper:${version}")
    // 同时还需要引入一个 ktor http客户端 引擎
    // https://ktor.io/docs/client-engines.html
    implementation("io.ktor:ktor-client-java:${ktorVersion}")
}
```

## 快速开始

这是一个简单的示范 适用于 `kotlin/jvm`

```kotlin
fun main(vararg args: String) {
    val username = System.getenv("USERNAME")
    val password = System.getenv("PASSWORD")

    runBlocking {
        val state = when (username.contains("@")) {
            true -> {
                // mpay (aka. 163) login
                val api = UniSdkMpay.newInstance()
                val device = api.registerDevice()

                api.login(device, username, password).toCookie(device)
            }
            false -> {
                // 4399 mixed login
                val api = I4399GameSDKAPI.newInstance(CONFIG_NET_EASE_MC)
                api.registerDevice()

                api.login(
                    username = System.getenv("USERNAME"),
                    password =  System.getenv("PASSWORD"),
                    onCaptcha = { raw ->
                        File("captcha.png").writeBytes(raw)

                        println("Input captcha code(captcha.png):")
                        readln()
                    },
                    onRealName = { error("当前账号尚未完成认证") }
                )
            }
        }

        println("Cookie:")
        println(state.toWrappedCookie())
        
        // x19 protocol
        val x19 = WPLauncherAPI.newInstance().login(state)
        val self = x19.getSelfDetail()
        println("WPLauncher profile: ${self.name} (${x19.session.id})")

        val details = x19.getItemDetails(4661334467366178884UL)
        val connection = x19.getNetworkServerAddress(4661334467366178884UL)

        println("Heypixel name: ${details.name}") // 布吉岛·新玩法上线
        println("Heypixel online: ${details.currentOnline}")
        println("Heypixel address: ${connection.address}") // pc.bjdmc.net:25565
    }
}
```

## 一些常见问题

Q: 为什么要使用`kotlin`编写, 有其它语言编写的版本吗?

A: 很遗憾我们并没有相关时间去编写, 但您可以参考我们的代码用别的语言实现

Q: 为什么不能通过 `4399pc` 登录?

A: 请看[这里](https://news.4399.com/gonglue/wdshijie/zixun/m/1009306.html)

## Issues

如果你发现了 bug 或者需要新的功能, 你可以创建一个新的 issue

我们也欢迎您提交 pull request
