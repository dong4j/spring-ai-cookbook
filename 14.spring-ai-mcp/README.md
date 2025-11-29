# 模型上下文协议(MCP)

## 1. 简介

模型上下文协议（MCP）是一个创新的开源协议，它重新定义了大语言模型（LLM）与外部世界的互动方式。MCP 提供了一种标准化方法，使任意大语言模型能够轻松连接各种数据源和工具，实现信息的无缝访问和处理。MCP 就像是 AI 应用程序的 USB-C 接口，为 AI 模型提供了一种标准化的方式来连接不同的数据源和工具。

### 1.1 服务架构

![](./imgs/20251129_P3vrNW.svg)

### 1.2 Agent架构

![](./imgs/20251129_5bX318.svg)

### 1.3 MCP流程调用

![](./imgs/20251129_zvtyql.svg)

### 1.4 官方架构

MCP就像是USB-C一样，可以让不同设备通过相同的接口连接在一起

![](./imgs/20251129_uVOeDf.png)

## 2. MCP概念

通俗一点讲MCP是作为一个远程的标准化的服务接口，在与大模型交互时，客户端端会自动通过标注协议获取远程服务的相关信息作为上下文传递给大模型

- Tools：服务器暴露可执行功能，供LLM调用以与外部系统交互
- Resources：服务器暴露数据和内容，供客户端读取并作为LLM上下文
- Prompts：服务器定义可复用的提示模板，引导LLM交互
- Sampling：让服务器借助客户端向LLM发起完成请求，实现复杂的智能行为
- Roots：客户端给服务器指定的一些地址，用来高速服务器该关注哪些资源和去哪里找这些资源

### 2.1 Tools

服务器所支持的工具能力，使用提供的装饰器就可以定义对应的工具

### 2.2 Resources

类似于服务端定义了一个api接口用于查询数据，可以给大模型提供上下文

### 2.3 Prompt

提示词，用于在服务端定义好自己的提示词来进行复用

### 2.4 Images

MCP提供的一个Image类，可以自动处理图像数据

### 2.5 Context

Context 对象为您的工具和资源提供对 MCP 功能的访问权限，在服务端的工具中可以调用对应的资源数据

### 2.6 Server

自定义Server提供了更加灵活的方式来组合资源、工具，包括服务启动的生命周期流程的控制

### 2.7 Sampling

MCP为我们提供的一个在执行工具前后可以执行的一些操作，类似回调函数

## 3. Spring AI MCP

### 3.1 依赖版本

Spring AI组件提供MCP服务组件框架集成，目前框架只支持 **Spring Boot 3.4.x**

```xml
<dependency>  
    <groupId>org.springframework.ai</groupId>  
    <artifactId>spring-ai-bom</artifactId>  
    <version>1.1.0</version>  
    <type>pom</type>  
    <scope>import</scope>  
</dependency>
```

### 3.2 简单MCP服务端

```xml
<dependencies>  
    <dependency>  
	    <groupId>org.springframework.ai</groupId>  
	    <artifactId>spring-ai-starter-mcp-server</artifactId>  
	</dependency>
</dependencies>
```

#### 3.2.1 启动类

```java
@SpringBootApplication  
public class McpServerApplication {  
  
    /**  
     * Main     *     * @param args args  
     * @since 1.0.0  
     */    public static void main(String[] args) {  
        SpringApplication.run(McpServerApplication.class, args);  
    }  
  
}
```

#### 3.2.2 工具服务

```java
@Service  
public class WeatherService {  
  
    /**  
     * Query weather     *     * @param city city  
     * @return the string  
     * @since 1.0.0  
     */    @Tool(description = "天气查询")  
    public String queryWeather(String city) {  
        return "天气查询";  
    }  
  
}
```

#### 3.2.3 注册工具

```java
@Configuration  
public class McpServerAutoConfiguration {  
  
    /**  
     * Weather tools     *     * @param weatherService weather service  
     * @return the tool callback provider  
     * @since 1.0.0  
     */    @Bean  
    public ToolCallbackProvider weatherTools(WeatherService weatherService) {  
        return MethodToolCallbackProvider.builder()  
                .toolObjects(weatherService)  
                .build();  
    }  
  
}
```

#### 3.2.4 配置文件

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

注意：创建的配置文件，必须要禁用banner否则客户端读取时就会出现以下错误

```txt
10:15:40.979 [pool-1-thread-1] ERROR io.modelcontextprotocol.client.transport.StdioClientTransport -- Error processing inbound message for line: 
com.fasterxml.jackson.databind.exc.MismatchedInputException: No content to map due to end-of-input
 at [Source: REDACTED (`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled); line: 1]
	at com.fasterxml.jackson.databind.exc.MismatchedInputException.from(MismatchedInputException.java:59)
	at com.fasterxml.jackson.databind.ObjectMapper._initForReading(ObjectMapper.java:5008)
	at com.fasterxml.jackson.databind.ObjectMapper._readMapAndClose(ObjectMapper.java:4910)
	at com.fasterxml.jackson.databind.ObjectMapper.readValue(ObjectMapper.java:3860)
	at com.fasterxml.jackson.databind.ObjectMapper.readValue(ObjectMapper.java:3843)
	at io.modelcontextprotocol.spec.McpSchema.deserializeJsonRpcMessage(McpSchema.java:153)
	at io.modelcontextprotocol.client.transport.StdioClientTransport.lambda$startInboundProcessing$6(StdioClientTransport.java:261)
	at reactor.core.scheduler.SchedulerTask.call(SchedulerTask.java:68)
	at reactor.core.scheduler.SchedulerTask.call(SchedulerTask.java:28)
	at java.base/java.util.concurrent.FutureTask.run$$$capture(FutureTask.java:264)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1136)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635)
	at java.base/java.lang.Thread.run(Thread.java:840)
```

### 3.3 简单客户端

```xml
<dependencies>  
    <dependency>  
	    <groupId>org.springframework.ai</groupId>  
	    <artifactId>spring-ai-starter-mcp-client</artifactId>  
	</dependency>
</dependencies>
```

#### 3.3.1 启动类

```java
@SpringBootApplication  
public class McpClientApplication {  
  
    /**  
     * Main     *     * @param args args  
     * @since 1.0.0  
     */    public static void main(String[] args) {  
        SpringApplication.run(McpClientApplication.class, args);  
    }  
  
```java
@Bean  
public CommandLineRunner predefinedQuestions(List<McpSyncClient> mcpSyncClients) {  
    return args -> {  
        mcpSyncClients.forEach(mcpSyncClient -> {    
            mcpSyncClient.listTools().tools().forEach(tool -> {  
                System.out.println("工具数据：" + tool.name());  
            });  
        });  
    };  
}
```

```

### 3.4 通讯方式

#### 3.4.1 stdio

标准的输入输出的方式进行通讯，跟python一样可以指定java启动某个java包，也可以使用npx启动某个npm的包

```yaml
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
                - xxx.jar  
            server2:  
              command: npx  
              args:  
                - -y  
                - "@modelcontextprotocol/server-filesystem"  
                - /Users/haijun/Work/my-work/
```

#### 3.4.2 sse

要支持sse可以使用以下依赖组件，下面的主键也同时支持stdio的方式通信

```xml
<dependency> 
	<groupId>org.springframework.ai</groupId> 
	<artifactId>spring-ai-starter-mcp-server-webmvc</artifactId> 
</dependency>
```

客户端配置

```yml
spring:  
  ai:  
    mcp:  
      client:  
        sse:  
          connections:  
            server1: http://localhost:8080
```

服务端配置

```yml
spring:  
  ai:  
    mcp:  
      server:  
        name: stdio-mcp-server  
        version: 1.0.0  
        sse-endpoint: /sse  
        sse-message-endpoint: /mcp/message
```

只需要修改配置即可，其余代码跟上面例子使用一样

#### 3.4.3 webflux

webflux的依赖包是同时兼容了，sse、stdio等方式进行通信，其余逻辑都是一样只是底层的代码变成了响应式

```xml
<dependency> 
	<groupId>org.springframework.ai</groupId> 
	<artifactId>spring-ai-starter-mcp-server-webflux</artifactId> 
</dependency>

<dependency>  
    <groupId>org.springframework.ai</groupId>  
    <artifactId>spring-ai-starter-mcp-client-webflux</artifactId>  
</dependency>
```

### 3.5 常用API

#### 3.5.1 Tools

##### ToolCallbackProvider

![](./imgs/20251129_naGufT.png)

- MethodToolCallbackProvider：方法工具回调函数提供器
- StaticToolCallbackProvider：里面包含了FunctionCallback包装了一层
- SyncMcpToolCallbackProvider/AsyncMcpToolCallbackProvider：同步异步工具回调函数提供器

```java
@Bean  
public ToolCallbackProvider weatherTools(WeatherService weatherService) {  
    return MethodToolCallbackProvider.builder()  
            .toolObjects(weatherService)  
            .build();  
}
```

##### ToolCallback

通过上面的提供器最终构建的还是 **ToolCallback** 接口类型的实例对象

![](./imgs/20251129_ij7yxF.png)

- MethodToolCallback：普通@Tool标识的方法工具回调函数
- FunctionToolCallback：函数回调工具
- SyncMcpToolCallback/AsyncMcpToolCallback：同步异步工具回调函数

#### 3.5.2 Resource

资源，用于给mcp客户端提供资源的查询能力，例如：api接口，数据库查询。可以理解为spring mvc接口

```java
@Bean  
public List<McpServerFeatures.SyncResourceSpecification> resource() {  
    McpSchema.Annotations annotations = new McpSchema.Annotations(List.of(McpSchema.Role.USER), 0.1);  
    var systemInfoResource = new McpSchema.Resource("/v1/api/query", "queryDatabase", "数据库查询", "", annotations);  
    var resourceSpecification = new McpServerFeatures.SyncResourceSpecification(systemInfoResource, (exchange, request) -> {  
        try {  
            var systemInfo = Map.of("order", "叔叔叔叔");  
            String jsonContent = new ObjectMapper().writeValueAsString(systemInfo);  
            return new McpSchema.ReadResourceResult(  
                    List.of(new McpSchema.TextResourceContents(request.uri(), "application/json", jsonContent)));  
        } catch (Exception e) {  
            throw new RuntimeException("Failed to generate system info", e);  
        }  
    });  
    return List.of(resourceSpecification);  
}
```

#### 3.5.3 Prompt

提示词管理

```java
@Bean  
public List<McpServerFeatures.SyncPromptSpecification> prompts1() {  
    var prompt = new McpSchema.Prompt("greeting", "一个友好的提示词模板",  
            List.of(new McpSchema.PromptArgument("name", "名称", true)));  
  
    var promptSpecification = new McpServerFeatures.SyncPromptSpecification(prompt,  
            (exchange, getPromptRequest) -> {  
                // 获取到里面的参数  
                String nameArgument = (String) getPromptRequest.arguments().get("name");  
                if (nameArgument == null) {  
                    nameArgument = "朋友";  
                }  
                var userMessage = new McpSchema.PromptMessage(McpSchema.Role.USER, new McpSchema.TextContent("你好 " + nameArgument + "! 我可以帮助你什么?"));  
                return new McpSchema.GetPromptResult("个性化问候语", List.of(userMessage));  
            });  
    return List.of(promptSpecification);  
}
```

#### 3.5.4 Roots

根资源管理，目的是用于告诉服务端需要关注的资源范围。当客户端连接到服务器时，会声明应该关注哪些根资源。根资源可以是文件路径，也可以是http url地址

> file:///home/user/projects/myapp
> https://api.example.com/v1

第一步先配置客户端支持Roots根资源

```java
@Component  
public static class CustomMcpSyncClientCustomizer implements McpSyncClientCustomizer {  
    @Override  
    public void customize(String serverConfigurationName, McpClient.SyncSpec spec) {  
  
        McpSchema.ClientCapabilities clientCapabilities = McpSchema.ClientCapabilities.builder()  
        .experimental(Map.of())  
        .roots(true)  
        .sampling()  
        .build();  
        spec.capabilities(clientCapabilities);  
  
        spec.loggingConsumer((McpSchema.LoggingMessageNotification log) -> {  
            System.out.println("消息提醒：" + log.data());  
        });  
    }  
}
```

客户端启动后添加一个根资源

```java
@Bean  
public CommandLineRunner predefinedQuestions(List<McpSyncClient> mcpSyncClients) {  
    return args -> {  
        mcpSyncClients.forEach(mcpSyncClient -> {    
            McpSchema.Root root = new McpSchema.Root("http://127.0.0.1", "端点");  
            mcpSyncClient.addRoot(root);  
        });  
    };  
}
```

服务监听根资源哪些需要监听，如果有资源发生变化则通过日志进行提醒

```java
@Bean  
public BiConsumer<McpSyncServerExchange, List<McpSchema.Root>> rootsChangeHandler() {  
    return (exchange, roots) -> {  
        roots.forEach(root -> {  
            String uri = root.uri();  
            System.out.println("roots资源:" + uri);  
  
            McpSchema.LoggingMessageNotification notification = McpSchema.LoggingMessageNotification.builder()  
                    .data("资源发生了变化")  
                    .level(McpSchema.LoggingLevel.INFO)  
                    .logger("rootsChangeHandler")  
                    .build();  
            exchange.loggingNotification(notification);  
        });  
    };  
}
```

#### 3.5.5 Sampling

客户端提供给服务端的回调函数

```java
@Component  
public static class CustomMcpSyncClientCustomizer implements McpSyncClientCustomizer {  
    @Override  
    public void customize(String serverConfigurationName, McpClient.SyncSpec spec) {  
  
        // Sets a custom sampling handler for processing message creation requests.  
        spec.sampling((McpSchema.CreateMessageRequest messageRequest) -> {  
            // Handle sampling  
            List<McpSchema.SamplingMessage> messages = messageRequest.messages();  
            McpSchema.CreateMessageResult messageResult = null;  
            for (McpSchema.SamplingMessage message : messages) {  
                McpSchema.Content content = message.content();  
                if ("text".equals(content.type())) {  
                    McpSchema.TextContent textContent = (McpSchema.TextContent) content;  
                    System.out.println("收到服务端的回调函数：" + textContent.text());  
  
                    messageResult = McpSchema.CreateMessageResult.builder()  
                            .message("我确认：" + textContent.text())  
                            .build();  
                }  
            }  
            return messageResult;  
        });   
    }  
}
```

服务端调用工具时发起调用sampling工具的请求

```java
/**  
 * Query weather * * @param city city  
 * @return the string  
 * @since 1.0.0  
 */
@Tool(description = "天气查询")  
public String queryWeather(String city, ToolContext toolContext) {  
    System.out.println("回调函数确认：" + this.callMcpSampling(toolContext, city));  
    return "天气查询";  
}  
  
/**  
 * Call mcp sampling * * @param toolContext tool context  
 * @param city        city  
 * @return the string  
 * @since 1.0.0  
 */
private String callMcpSampling(ToolContext toolContext, String city) {  
    StringBuilder stringBuilder = new StringBuilder();  
    McpToolUtils.getMcpExchange(toolContext)  
            .ifPresent(exchange -> {  
                if (exchange.getClientCapabilities().sampling() != null) {  
                    McpSchema.CreateMessageRequest messageRequestBuilder = McpSchema.CreateMessageRequest.builder()  
                            .messages(List.of(new McpSchema.SamplingMessage(McpSchema.Role.USER,  
                                    new McpSchema.TextContent("你确定要查询:" + city))))  
                            .build();  
                    McpSchema.CreateMessageResult result = exchange.createMessage(messageRequestBuilder);  
                    stringBuilder.append(result.content().text());  
                }  
            });  
    return stringBuilder.toString();  
}
```

#### 3.5.6 监听器

当工具、资源、提示词等资源进行变动时，都可以通过配置对应的消费者来监听数据的变动

```java
@Component  
public static class CustomMcpSyncClientCustomizer implements McpSyncClientCustomizer {  
    @Override  
    public void customize(String serverConfigurationName, McpClient.SyncSpec spec) {  
  
        // Customize the request timeout configuration
        spec.requestTimeout(Duration.ofSeconds(30));  
        McpSchema.ClientCapabilities clientCapabilities = McpSchema.ClientCapabilities.builder()  
                .experimental(Map.of())  
                .roots(true)  
                .sampling()  
                .build();  
        spec.capabilities(clientCapabilities);  
  
        // Sets a custom sampling handler for processing message creation requests.  
        spec.sampling((McpSchema.CreateMessageRequest messageRequest) -> {  
            // Handle sampling  
            return null;  
        });  
  
        spec.toolsChangeConsumer((List<McpSchema.Tool> tools) -> {  
        });  
  
        spec.resourcesChangeConsumer((List<McpSchema.Resource> resources) -> {  
        });  
  
        spec.promptsChangeConsumer((List<McpSchema.Prompt> prompts) -> {  
        });  
  
  
        // 日志打印
        spec.loggingConsumer((McpSchema.LoggingMessageNotification log) -> {  
            System.out.println("消息提醒：" + log.data());  
        });  
    } 
}
```

## 4. 故障排除

### 4.1 包装Request

在有些现有的服务中可能会对http请求类进行包装缓存一层

## 5. 开源MCP市场

- [魔搭社区MCP广场](https://www.modelscope.cn/mcp)
- [MCP Server](https://mcp.so/)
- [MCP客户端和服务端推荐](https://github.com/yzfly/Awesome-MCP-ZH?tab=readme-ov-file)
- [Awesome MCP Servers](https://mcpservers.org/)
- [Cursor MCP](https://cursor.directory/)
- [官方提供的MCP服务](https://github.com/modelcontextprotocol/servers)
