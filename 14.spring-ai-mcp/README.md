# 模型上下文协议(MCP)

<!--
写作思路：
  - 这一篇是「上手体验篇」，几乎不讲概念，只让读者先跑起来。
  - 避免解释什么是 LLM、function calling、MCP 规范，只在需要的地方用一句话浅带。
  - 重点是：Spring AI + MCP Starter 如何配置、如何调用 MCP Server 的工具、如何写一个最小的 MCP Server。
  - 读完后读者应该有这样的感觉：
    “原来 MCP 就是这样被 Spring AI 调用的，我大概知道它是个『AI 可调用工具协议』。”

大纲示例：
1.	为什么先从 Spring AI 开始？
  - MCP 很抽象，直接看规范容易劝退
  - 用 Spring Boot + Spring AI 做一个「AI 能用的工具」最直观
  - 本系列整体路线简单预告（用 1 图说明 8 篇）
2.	本文目标与预备环境
  - 目标：让 LLM 调用一个「查询系统时间」的 MCP 工具
  - 环境：JDK、Spring Boot、Spring AI 依赖、一个简单的 LLM provider（OpenAI / 本地）
3.	用 Spring AI 写一个最小 MCP Server
  - 引入 spring-ai-mcp-server starter
  - 定义一个简单的 Tool（例如 time.now）
  - 配置 server 端口、路径，启动起来
4.	用 Spring AI 写一个最小 MCP Client
  - 引入 spring-ai-mcp-client starter
  - 在 application.yml 里配置 MCP Server（比如 HTTP 或 stdio）
  - 写一段 Java 代码：通过 MCP Client 调用 time.now
  - 打印返回结果（证明 client-server 通了）
5.	把 LLM 拉进来：让模型「自动决定」调用 MCP 工具
  - 创建一个简单的 ChatClient（OpenAI / 其他）
  - 把 MCP 工具注册成模型工具（用 Spring AI 已封装的方式）
  - 演示用户输入一句话 → 模型自动选择工具 → 工具执行 → 返回结果
  - Console 日志截一段：可以看到工具调用过程
6.	小结：我现在对 MCP 的“直觉认识”
  - MCP 看起来像「给 AI 用的 RPC + 工具发现机制」
  - Spring AI 很大程度帮我们「挡住了协议细节」
  - 埋一个坑：
但在没有 Spring AI 的世界里，LLM 是怎么调用外部工具的？
MCP 到底解决了什么老问题？


这一篇我们只站在 Spring AI 的肩膀上，把 MCP 当成一个「可以远程调用的小工具」。
但你可能已经有疑问：
- 没有 MCP 之前，LLM 是怎么调用这些工具的？
- 为什么还要搞一个新的协议？

下一篇我们就从「传统 function calling 的痛点」出发，看看 MCP 是怎么被一步步“逼”出来的。
-->

## 什么是 MCP？

想象一下，你的 AI 助手就像一个智能机器人，但它本身只能"思考"，无法直接操作现实世界。MCP（Model Context Protocol，模型上下文协议）就像是为这个机器人提供的一套**标准化的"插件系统"**。

### 一个简单的比喻

如果把 AI 模型比作一台电脑，那么 MCP 就像是电脑的 **USB-C 接口**：

- 任何符合 MCP 标准的"外设"（工具、数据源）都可以轻松接入
- 不需要为每个外设都开发一套专用接口
- 插上就能用，拔掉也不影响电脑本身

### MCP 能做什么？

MCP 让 AI 模型能够：

- 🔧 **调用工具**：比如查询天气、发送邮件、操作数据库
- 📚 **读取数据**：从各种数据源获取信息作为上下文
- 💬 **使用提示词模板**：复用常用的提示词，提高效率

> 💡 **快速入门提示**：作为快速入门教程，这里我们只需要了解 MCP 的基本概念即可。不需要现在就深入理解所有细节，先跟着示例代码走一遍，感受一下 Spring AI MCP 的使用方式。等有了整体认识后，再深入学习也不迟！

### 架构示意

![](./imgs/20251129_P3vrNW.svg)

## 快速上手

让我们通过一个简单的例子来快速了解 Spring AI MCP。我们会创建一个天气查询服务，然后创建一个客户端来调用它。

> 🎯 **目标**：通过这个例子，你会了解如何创建一个 MCP 服务端和客户端，并看到它们是如何协作的。不需要理解所有细节，先跑起来再说！

### 前置准备

首先，确保你的项目使用 **Spring Boot 3.4.x**，并添加 Spring AI 的依赖管理：

```xml
<dependency>  
    <groupId>org.springframework.ai</groupId>  
    <artifactId>spring-ai-bom</artifactId>  
    <version>1.1.0</version>  
    <type>pom</type>  
    <scope>import</scope>  
</dependency>
```

### 第一步：创建 MCP 服务端

服务端的作用是"暴露能力"——告诉外界："我可以做什么"。

#### 1. 添加依赖

```xml
<dependencies>  
    <dependency>  
	    <groupId>org.springframework.ai</groupId>  
	    <artifactId>spring-ai-starter-mcp-server</artifactId>  
	</dependency>
</dependencies>
```

#### 2. 创建启动类

标准的 Spring Boot 启动类，没什么特别的：

```java
@SpringBootApplication  
public class McpServerApplication {  
    public static void main(String[] args) {  
        SpringApplication.run(McpServerApplication.class, args);  
    }  
}
```

#### 3. 定义工具服务

这里我们创建一个天气查询服务。注意 `@Tool` 注解，它告诉 Spring AI："这个方法是一个工具，可以被 MCP 客户端调用"：

```java
@Service  
public class WeatherService {  
    @Tool(description = "天气查询")  
    public String queryWeather(String city) {  
        return city + "的天气是18°";  
    }  
}
```

#### 4. 注册工具

告诉 Spring AI 框架："这个服务里的工具可以被 MCP 使用"：

```java
@Configuration  
public class McpServerAutoConfiguration {  
    @Bean  
    public ToolCallbackProvider weatherTools(WeatherService weatherService) {  
        return MethodToolCallbackProvider.builder()  
                .toolObjects(weatherService)  
                .build();  
    }  
}
```

#### 5. 配置文件

```yml
spring:  
  ai:  
    mcp:  
      server:  
        name: stdio-mcp-server  
        version: 1.0.0  
        type: SYNC  
        stdio: true  
  main:  
    banner-mode: off  
logging:  
  pattern:  
    console:
```

> ⚠️ **重要提示**：`banner-mode: off` 这一行很重要！如果不禁用 banner，客户端读取时会出错。记住这一点就好。

### 第二步：创建 MCP 客户端

客户端的作用是"使用能力"——连接到服务端，然后调用它提供的工具。

#### 1. 添加依赖

```xml
<dependencies>  
    <dependency>  
	    <groupId>org.springframework.ai</groupId>  
	    <artifactId>spring-ai-starter-mcp-client</artifactId>  
	</dependency>
</dependencies>
```

#### 2. 创建启动类

同样，标准的 Spring Boot 启动类：

```java
@SpringBootApplication  
public class McpClientApplication {  
    public static void main(String[] args) {  
        SpringApplication.run(McpClientApplication.class, args);  
    }  
}
```

#### 3. 客户端配置

这里告诉客户端："去连接这个服务端"。`stdio` 表示使用标准输入输出进行通信（就像命令行工具那样）：

```yml
spring:  
  ai:  
    mcp:  
      client:  
        stdio:  
          connections:  
            server1:  
              command: java  
              args:  
            - -jar  
            - mcp-server.jar  # 你的服务端 jar 包路径
```

#### 4. 调用示例

现在可以调用服务端提供的工具了！这段代码会在应用启动时自动执行：

```java
@Bean  
public CommandLineRunner predefinedQuestions(List<McpSyncClient> mcpSyncClients) {  
    return args -> {  
        mcpSyncClients.forEach(mcpSyncClient -> {    
            // 看看服务端提供了哪些工具
            mcpSyncClient.listTools().tools().forEach(tool -> {  
                System.out.println("发现工具：" + tool.name());  
            });  
            
            // 调用天气查询工具
            McpSchema.CallToolResult result = mcpSyncClient.callTool(
                new McpSchema.CallToolRequest("queryWeather", Map.of("city", "北京"))
            );
            
            // 打印结果
            result.content().forEach(content -> {
                if (content instanceof McpSchema.TextContent textContent) {
                    System.out.println("查询结果：" + textContent.text());
                }
            });
        });  
    };  
}
```

### 通讯方式（简单了解）

MCP 支持多种通讯方式，目前我们用的是 `stdio`（标准输入输出），这是最简单的方式。还有其他方式如 `SSE`、`WebFlux` 等，但作为快速入门，暂时不需要了解太多。

> 📖 **想深入了解？** 更多关于通讯方式的详细配置，请参考 [docs/1.mcp-advanced.md](./docs/1.mcp-advanced.md)

## 接下来做什么？

恭喜！如果你已经成功运行了上面的示例，说明你已经掌握了 Spring AI MCP 的基本使用。现在你可能想知道：

- **更多功能**：除了工具调用，MCP 还支持资源读取、提示词模板等高级功能
- **适用场景**：MCP 适合什么场景？什么时候不应该用 MCP？
- **常见问题**：遇到问题怎么办？有哪些坑需要注意？

我们准备了详细的文档来回答这些问题：

- 📚 **[MCP 进阶使用](./docs/1.mcp-advanced.md)** - 深入了解 MCP 的高级功能
- 🤔 **[MCP 是最优解吗？](./docs/2.mcp-not-best.md)** - 了解 MCP 的适用场景
- ⚠️ **[MCP 的局限性与安全性](./docs/3.mcp-problems.md)** - 了解需要注意的问题

## 常见问题

### Banner 错误

**症状**：客户端启动时出现 JSON 解析错误

**原因**：服务端输出了 banner，干扰了通信协议

**解决**：确保服务端配置中有 `banner-mode: off`

```yml
spring:  
  main:  
    banner-mode: off
```

### 其他问题

遇到其他问题？查看 [详细的问题排查指南](./docs/3.mcp-problems.md)

## 探索更多

想要找现成的 MCP 服务？或者想看看别人是怎么用的？这里有一些资源：

- 🌟 [魔搭社区 MCP 广场](https://www.modelscope.cn/mcp) - 中文社区的 MCP 服务集合
- 🔧 [MCP Server](https://mcp.so/) - MCP 服务目录
- 📦 [Awesome MCP](https://github.com/yzfly/Awesome-MCP-ZH) - 中文 MCP 资源推荐
- 🎯 [Cursor MCP](https://cursor.directory/) - Cursor 编辑器的 MCP 目录
- 🏢 [官方 MCP 服务](https://github.com/modelcontextprotocol/servers) - MCP 官方提供的服务示例
