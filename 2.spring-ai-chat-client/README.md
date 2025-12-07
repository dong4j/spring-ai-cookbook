---
published: 2025.10.01
---

# Chat Client API

## 概述

## 目标

使用 Spring AI 搭建一个示例项目，主要使用 OpenAI 和 Anthropic 来测试聊天流程，为后续的 MCP、Tool 以及 Agent 功能做好准备。

## 项目配置

### 1. OpenAI 配置（通义千问兼容模式）

#### 配置文件

在 `application.yml` 中配置通义千问的兼容模式：

```yaml
spring:
  ai:
    openai:
      base-url: https://dashscope.aliyuncs.com/compatible-mode
      api-key: ${QIANWEN_API_KEY}
```

#### 代码配置

在 `ChatClientConfig.java` 中创建 OpenAI 聊天客户端：

```java
@Bean
public ChatClient openAiChatClient(OpenAiChatModel chatModel) {
    final OpenAiChatModel build = chatModel.mutate().defaultOptions(OpenAiChatOptions.builder()
                                                                        .model("qwen2.5-14b-instruct")
                                                                        .temperature(0.7)
                                                                        .build()).build();
    return ChatClient.create(build);
}
```

#### 重要注意事项

⚠️ **思考模型（Think Model）的问题**：

- `chatModel` 默认使用**非流式响应**
- 如果使用思考模型（think model），会报错
- 原因：通义千问的 `enable_thinking` 参数必须设置为 `false`，且该参数需要直接在请求体顶层，而不是嵌套在 `extra_body` 中
- Spring AI 的 `extraBody` 方法会创建嵌套的 `extra_body` 对象，这是 OpenAI API 的结构，但通义千问 API 不支持这种嵌套结构

**问题示例**：

```json
// Spring AI 的 extraBody 会生成这样的结构（不符合通义千问 API）
{
  "extra_body": {
    "enable_thinking": false
  }
}

// 通义千问 API 需要这样的结构
{
  "enable_thinking": false
}
```

**当前解决方案**：

- 暂时不使用思考模型
- 如果必须使用思考模型，需要等待 Spring AI 未来版本支持，或者自定义实现

### 2. Anthropic 配置（Claude Code Router 转发）

#### 配置文件

在 `application.yml` 中配置本地转发地址：

```yaml
spring:
  ai:
    anthropic:
      api-key: ${ANTHROPIC_AUTH_TOKEN}
      base-url: http://127.0.0.1:3456
```

#### 代码配置

在 `ChatClientConfig.java` 中创建 Anthropic 聊天客户端：

```java
@Bean
public ChatClient anthropicChatClient(AnthropicChatModel chatModel) {
    return ChatClient.create(chatModel);
}
```

#### 说明

- 使用 `claude-code-router` (CCR) 进行转发
- 直接配置本地 URL：`http://127.0.0.1:3456`
- API Key 可通过 CCR UI 中设置

### 3. Embedding 配置（解决 Bean 冲突）

#### 问题

引入 `spring-ai-starter-model-openai` 后，Spring AI 会自动装配一个 `openAiEmbeddingModel` Bean，这会与 `postgresMlEmbeddingModel` Bean 产生冲突。

#### 解决方案

在 `application.yml` 中显式指定使用 PostgresML 的 Embedding：

```yaml
spring:
  ai:
    model:
      # 因为引入了 openai, 也会带一个 openAiEmbeddingModel, 会与 postgresMlEmbeddingModel 冲突
      # 这里强制指定为 postgresml
      embedding: postgresml
```

#### 完整配置示例

```yaml
spring:
  ai:
    postgresml:
      embedding:
        create-extension: true # 自动创建扩展
        options:
          vector-type: pg_vector # 向量类型
```

## 项目结构

```
src/main/java/dev/dong4j/ai/spring/
├── config/
│   └── ChatClientConfig.java      # 聊天客户端配置
├── controller/
│   └── AdoptionsController.java   # 测试控制器
└── SpringAiTutorialApplication.java
```

## 测试接口

### OpenAI 聊天接口

```
GET /openAiChatClient/{user}/assistant?question=你的问题
```

### Anthropic 聊天接口

```
GET /anthropic/{user}/assistant?question=你的问题
```

**下一步计划**：

- 为后续的 MCP（Model Context Protocol）集成做准备
- 为 Tool 功能集成做准备
- 为 Agent 功能集成做准备

## 遇到的问题

### 1. 思考模型无法使用

**问题**：使用思考模型时报错，因为 `enable_thinking` 参数无法正确设置。

**原因**：Spring AI 的 `extraBody` 会创建嵌套结构，但通义千问 API 需要参数在顶层。

**当前状态**：暂时不使用思考模型，等待更好的解决方案。

### 2. Embedding Bean 冲突

**问题**：引入 OpenAI starter 后，自动装配的 Embedding Bean 与 PostgresML 冲突。

**解决**：通过 `spring.ai.model.embedding=postgresml` 显式指定。

## 参考资料

- [Spring AI 官方文档](https://docs.spring.io/spring-ai/reference/)
- [通义千问 API 文档](https://help.aliyun.com/zh/model-studio/qwen-api-reference)
- [Claude Code Router](https://github.com/anthropics/claude-code-router)

