---
published: 2025.10.01
---

# Spring AI 特性概览

## 概述

在 [[guide/2.quick-start|快速开始]] 的终端示例里，我们搭建了一个最小化的 Spring Boot 应用并调用 Spring AI 返回了第一条聊天结果。

虽然流程极简，但完全依赖终端操作：前置环境多、脱离日常开发场景、对非 Linux/Unix 用户也不够友好，而且仅覆盖了 Chat Client API 的最基础能力，对理解 Spring AI 帮助有限。

因此本章节改用贴近真实项目的最小案例，逐个演示 Spring AI 的核心特性，先建立整体认知，再在后续章节深入每个功能点。

## 前置准备

此项目基于 Maven 多模块构建, 已在父 pom.xml 中添加了必要的依赖以及版本信息, 比如 `spring-ai-bom`, `Spring Boot` 的版本, JDK 的版本等, 所以在各个子模块中只会添加必要的依赖.

接下来你应该准备一下环境:

1. 安装 JDK25
2. 通过各种途径获取至少一个 AI 服务商的 API_KEY 并设置到环境变量中
3. 保持网络畅通 🥲

---

## 快速入门

以下文档将带你逐步了解 Spring AI 的各项核心功能：

- [[2.spring-ai-chat-client.md|聊天客户端]] - Chat Client API 入门
- [[3.spring-ai-prompts.md|提示词管理]] - 提示词模板与变量替换
- [[4.spring-ai-structured.md|结构化输出]] - 将 AI 响应映射为 Java 对象
- [[5.spring-ai-multimodality.md|多模态]] - 处理文本、图像等多种输入类型
- [[6.spring-ai-model.md|模型 API]] - Spring AI 的统一模型接口
- [[7.spring-ai-model-chat.md|聊天模型]] - ChatModel 接口的详细用法
- [[8.spring-ai-model-embedding.md|嵌入模型]] - 文本向量化与相似度计算
- [[9.spring-ai-model-image.md|图像模型]] - 图像生成与处理
- [[10.spring-ai-model-audio.md|音频模型]] - 语音识别与合成
- [[11.spring-ai-model-moderation.md|内容审核]] - 内容安全检测
- [[12.spring-ai-model-memory.md|记忆]] - 对话历史与上下文管理
- [[13.spring-ai-model-tool-calling.md|工具调用]] - Function Calling 功能
- [[14.spring-ai-mcp.md|MCP]] - Model Context Protocol 集成
- [[15.spring-ai-rag.md|RAG]] - 检索增强生成
- [[16.spring-ai-model-evaluation.md|模型评估]] - 评估模型性能与质量
- [[17.spring-ai-vector-database.md|向量数据库]] - 向量存储与检索
- [[18.spring-ai-observability.md|可观测性]] - 监控与追踪
- [[19.spring-ai-docker.md|Docker]] - 容器化部署
- [[20.spring-ai-testcontainer.md|Testcontainers]] - 集成测试环境

## 资源

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

### GraalVM 原生支持

此项目已配置为允许生成轻量级容器或原生可执行文件。
也可以在原生镜像中运行测试。

#### 使用云原生构建包的轻量级容器

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

#### 使用原生构建工具的可执行文件

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


