# 快速搭建 Spring AI 项目

## 最小化 Chat 应用

### 1. 创建 Spring Boot 项目

```bash [bash]
curl https://start.spring.io/starter.zip \
  -d dependencies=spring-ai-openai \
  -d bootVersion=3.5.8 \
  -d javaVersion=17 \
  -d type=maven-project \
  -o spring-ai-demo.zip
```

### 2. 修改启动类

```java [java]
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

@EnableAutoConfiguration
public class StartedApplication {

    public static void main(String[] args) {
        // 设置必要的参数(这里使用通义千问的 openai api)
        System.setProperty("spring.ai.openai.api-key", System.getenv("QIANWEN_API_KEY"));
        System.setProperty("spring.ai.openai.base-url", "https://dashscope.aliyuncs.com/compatible-mode");
        // 需使用非思考模型
        System.setProperty("spring.ai.openai.chat.options.model", "qwen2.5-14b-instruct");

        SpringApplication app = new SpringApplication(StartedApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        ConfigurableApplicationContext ctx = app.run(args);

        // spring-ai-starter-model-openai 自动注入了 OpenAiChatModel
        OpenAiChatModel chatModel = ctx.getBean(OpenAiChatModel.class);
        ChatClient client = ChatClient.create(chatModel);

        String reply = client.prompt("我说 ping, 你说 pong")
            .call()
            .content();

        System.out.println("AI 回复: " + reply);

        // 关闭应用上下文
        ctx.close();
    }
}

```

### 3. 启动测试

::: code-group

```bash [bash]
./mvnw spring-boot:run
```

```
AI 回复: pong! 😊 你想玩别的吗？
```

:::

---

### 参考文档

如需进一步参考，请考虑以下部分：

* [官方 Apache Maven 文档](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven 插件参考指南](https://docs.spring.io/spring-boot/3.5.8/maven-plugin)
* [创建 OCI 镜像](https://docs.spring.io/spring-boot/3.5.8/maven-plugin/build-image.html)
* [GraalVM 原生镜像支持](https://docs.spring.io/spring-boot/3.5.8/reference/packaging/native-image/introducing-graalvm-native-images.html)
* [PGvector 向量数据库](https://docs.spring.io/spring-ai/reference/api/vectordbs/pgvector.html)
* [Spring Boot Actuator](https://docs.spring.io/spring-boot/3.5.8/reference/actuator/index.html)
* [Spring Data JDBC](https://docs.spring.io/spring-boot/3.5.8/reference/data/sql.html#data.sql.jdbc)
* [JDBC 聊天内存仓库](https://docs.spring.io/spring-ai/reference/api/chat-memory.html)
* [PostgresML](https://docs.spring.io/spring-ai/reference/api/embeddings/postgresml-embeddings.html)
* [Spring Boot DevTools](https://docs.spring.io/spring-boot/3.5.8/reference/using/devtools.html)
* [Spring Web](https://docs.spring.io/spring-boot/3.5.8/reference/web/servlet.html)

### 指南

以下指南具体说明了如何使用某些功能：

* [使用 Spring Boot Actuator 构建 RESTful Web 服务](https://spring.io/guides/gs/actuator-service/)
* [使用 Spring Data JDBC](https://github.com/spring-projects/spring-data-examples/tree/main/jdbc/basics)
* [构建 RESTful Web 服务](https://spring.io/guides/gs/rest-service/)
* [使用 Spring MVC 提供网页内容](https://spring.io/guides/gs/serving-web-content/)
* [使用 Spring 构建 REST 服务](https://spring.io/guides/tutorials/rest/)

### 附加链接

这些附加参考也应该有帮助：

* [在构建插件中配置 AOT 设置](https://docs.spring.io/spring-boot/3.5.8/how-to/aot.html)

## GraalVM 原生支持

此项目已配置为允许生成轻量级容器或原生可执行文件。
也可以在原生镜像中运行测试。

### 使用云原生构建包的轻量级容器

如果已经熟悉 Spring Boot 容器镜像支持，这是最简单的入门方式。
在创建镜像之前，应该在机器上安装并配置 Docker。

要创建镜像，请运行以下目标：

```
$ ./mvnw spring-boot:build-image -Pnative
```

然后，可以像运行任何其他容器一样运行应用程序：

```
$ docker run --rm -p 8080:8080 spring-ai-tutorial:0.0.1-SNAPSHOT
```

### 使用原生构建工具的可执行文件

如果想探索更多选项，例如在原生镜像中运行测试，请使用此选项。
应该在机器上安装并配置 GraalVM `native-image` 编译器。

注意：需要 GraalVM 22.3+ 版本。

要创建可执行文件，请运行以下目标：

```
$ ./mvnw native:compile -Pnative
```

然后可以按如下方式运行应用程序：

```
$ target/spring-ai-started
```

也可以在原生镜像中运行现有的测试套件。
这是验证应用程序兼容性的有效方法。

要在原生镜像中运行现有测试，请运行以下目标：

```
$ ./mvnw test -PnativeTest
```

### Maven 父级覆盖

由于 Maven 的设计，元素会从父级 POM 继承到项目 POM。
虽然大部分继承都很好，但它也会从父级继承不需要的元素，如 `<license>` 和 `<developers>`。
为防止这种情况，项目 POM 包含这些元素的空覆盖。
如果手动切换到不同的父级并确实需要继承，则需要删除这些覆盖。

