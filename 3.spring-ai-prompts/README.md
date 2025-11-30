# Spring AI Prompts Cookbook

## 概述
什么是提示词？
我们输入给 AI 的那段话，无论是一个问题、一个要求，还是一个详细的指令，就是我们所说的提示词。
提示词是人类与AI高效沟通的桥梁。 提示词从最开始简单的一句话命令，逐步扩展为复杂的一项工程，我们称之为 `Prompt Engineering`，

## 核心类和接口

### 思考
在开始本章示例代码前，我们需要先知晓在 `Spring AI` 中是如何通过定义类与接口的关系来表达提示词相关信息。

让我们想想，如何设计一个简单的翻译提示词，它的元素有什么？ 
- 一份基础的角色定义
- 一段简单的任务描述吩咐大模型开始为我们执行任务
- 我们可能将部分入参能够动态放入到提示词中进行执行

``` text
system
你是一个专业的语言翻译专家，你会根据用户的输入，给出一个翻译结果，默认翻译语言为中文。

user
The {object} is under the chair.
```

这样一份简单的提示词大家应该很熟悉，我们可以简单思考后，发现它的元素包括有以下部分：
- `role`： 不同的提示词角色，有 system 和 user 两种
- `message`：消息内容，可以是任意文本，示例中有两段不同角色的文本
- `placeholder`：`user` 提示词文本内容中有字符串占位符，`{object}`

有了这些认知，我们下面看看在 `Spring AI` 中定义的有关 `Prompt` 的核心类与接口：

### 1. Role
在`Spring AI` 中，`Role` 是一个枚举类，它定义了提示词中的角色类型，包括 `system`、`user`、`assistant` 和 `tool`。
```java
public enum MessageType {
	USER("user"),
	ASSISTANT("assistant"),
	SYSTEM("system"),
	TOOL("tool");
    ...
}
```

### 2. Content
用于包装提示词内容与元数据的容器接口
```java
public interface Content {
    // 获取提示词内容
    String getContent();
    // 获取提示词元数据
    Map<String, Object> getMetadata();
}
```

### 3. Message
`Message` 接口继承至`Content` 接口，继承了获取内容和元数据的方法，同时定义了本消息的消息类型方法
```java
public interface Message extends Content {
    // 获取消息类型 `system`、`user`、`assistant` 和 `tool`
	MessageType getMessageType();
}
```
关于`Message` 接口，Spring AI 官方也提供了详细的类型图

![Message 关系图](https://docs.spring.io/spring-ai/reference/_images/spring-ai-message-api.jpg)

### 4. Prompt
`Prompt` 是 `Spring AI` 中最核心的类，它包含了一串消息，用于描述一个完整的提示词。还包含一个 `ChatOptions` 对象，用于定义提示词的配置选项。
```java
public class Prompt implements ModelRequest<List<Message>> {
    // 提示词消息列表
    private final List<Message> messages;
    // 提示词配置选项
    private ChatOptions chatOptions;
}
```

### 4. PromptTemplate
提示词模板旨在促进创建结构化的提示词，
将提示词内容定义为模板，并使用占位符进行填充，最终输出一个完整的提示词。


## 核心功能

### 1. 基础提示词管理
- 简单字符串提示词
- 多消息提示词组合
- 带配置选项的提示词配置

### 2. 提示词模板化
- 基础占位符，如 `{variable}`
- 自定义分隔符配置，如 `<variable>`
- 资源文件模板加载

### 3. 多角色提示词
- System 角色设置
- User 角色处理
- Assistant 角色管理
- Tool/Function 角色支持

### 4. 提示词工程技术
- Zero-shot 学习技术
- Few-shot 学习技术
- Chain-of-Thought (思维链) 推理
- ReAct (Reason + Act) 模式
- 提示词优化策略


## 快速开始

### 1. 配置设置
在 `application.yml` 中配置必要的参数：

```yaml
spring:
  ai:
    openai:
      base-url: https://dashscope.aliyuncs.com/compatible-mode
      api-key: ${QIANWEN_API_KEY}
    anthropic:
      api-key: ${ANTHROPIC_AUTH_TOKEN}
      base-url: http://127.0.0.1:3456
```

### 2. 启动应用
```bash
mvn spring-boot:run
```

### 3. 访问示例端点

#### 基础提示词示例
```bash
# 简单字符串提示词
curl "http://localhost:8080/prompts/simple?input=Spring%20AI"

# 多消息提示词
curl "http://localhost:8080/prompts/multi-message"

# 带选项的提示词
curl "http://localhost:8080/prompts/with-options?creative=true"
```

#### 提示词模板示例
```bash
# 基础模板
curl "http://localhost:8080/template/basic?adjective=interesting&topic=AI"

# 自定义分隔符模板
curl "http://localhost:8080/template/custom-delimiter?composer=John%20Williams"

# 资源文件模板
curl "http://localhost:8080/template/resource?language=Java"
```

#### 多角色提示词示例
```bash
# System + User 角色
curl "http://localhost:8080/roles/system-user?assistantName=技术专家&voice=专业&question=微服务架构"

# 完整对话流
curl "http://localhost:8080/roles/conversation?topic=Java编程"

# Tool 角色示例
curl "http://localhost:8080/roles/tool-call?calculation=add&value1=10&value2=5"
```

## 详细功能说明

### 基础提示词 (PromptController)

#### 简单字符串提示词
最基础的提示词方式，适合快速原型开发和简单问答场景：

```java
@GetMapping("/simple")
public String simplePrompt(@RequestParam(defaultValue = "Spring AI") String input) {
    return openAiChatClient.prompt()
            .user("Tell me just one interesting fact about " + input)
            .call()
            .content();
}
```

#### 多消息提示词
通过组合不同类型的消息，可以创建更精确和上下文相关的提示词：

```java
@GetMapping("/multi-message")
public String promptWithMultipleMessages() {
    Message systemMessage = new SystemMessage("""
        你是一个专业的技术导师。
        你的回答应该简洁明了，重点突出核心概念。
        每个回答不要超过200字。
        """);

    Message userMessage = new UserMessage("请解释什么是微服务架构？");

    Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
    return chatModel.call(prompt).getResult().getOutput().getText();
}
```

### 提示词模板 (PromptTemplateController)

#### 基础模板占位符
使用 `{variable}` 占位符创建可重用的提示词模板：

```java
@GetMapping("/basic")
public String basicTemplate(
        @RequestParam(defaultValue = "interesting") String adjective,
        @RequestParam(defaultValue = "AI") String topic) {
    
    PromptTemplate promptTemplate = new PromptTemplate(
        "Tell me a {adjective} fact about {topic}");

    Map<String, Object> variables = Map.of(
        "adjective", adjective,
        "topic", topic
    );

    Prompt prompt = promptTemplate.create(variables);
    return chatModel.call(prompt).getResult().getOutput().getText();
}
```

#### 自定义分隔符模板
当模板内容包含 JSON 等格式时，可以使用自定义分隔符避免冲突：

```java
@GetMapping("/custom-delimiter")
public String customDelimiterTemplate(@RequestParam(defaultValue = "John Williams") String composer) {
    var promptTemplate = PromptTemplate.builder()
        .renderer(StTemplateRenderer.builder()
            .startDelimiterToken('<')
            .endDelimiterToken('>')
            .build())
        .template("""
            Tell me the names of 5 movies whose soundtrack was composed by <composer>.
            For each movie, mention the year it was released.
            """)
        .build();

    return chatModel.call(promptTemplate.create(Map.of("composer", composer))).getResult().getOutput().getText();
}
```

### 多角色提示词 (PromptRoleController)

#### System + User 角色组合
System 角色用于定义 AI 的行为和回应风格，User 角色提供具体的问题或任务：

```java
@GetMapping("/system-user")
public String systemUserPrompt(
        @RequestParam(defaultValue = "技术专家") String assistantName,
        @RequestParam(defaultValue = "专业且友好") String voice,
        @RequestParam(defaultValue = "请解释什么是微服务架构") String question) {

    String systemText = """
        你是一个有帮助的AI助手，帮助人们找到信息。
        你的名字是：{name}
        你应该用{voice}的语调来回应用户的请求。
        回答应准确、全面且易于理解。
        """;

    PromptTemplate systemPromptTemplate = new PromptTemplate(systemText);
    Message systemMessage = systemPromptTemplate.createMessage(Map.of(
        "name", assistantName,
        "voice", voice
    ));

    Message userMessage = new UserMessage(question);
    Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
    
    return chatModel.call(prompt).getResult().getOutput().getText();
}
```

## 最佳实践指南

### 1. 提示词设计原则

#### 清晰性和具体性
- 使用明确的语言描述你想要的结果
- 提供具体的示例和格式要求
- 避免模糊不清的指令

#### 上下文提供
- 提供足够的背景信息帮助 AI 理解任务
- 使用 System 角色设定 AI 的专业领域和回应风格
- 在多轮对话中保持上下文的连贯性

#### 约束和指导
- 明确指定输出格式（JSON、Markdown、列表等）
- 设置长度限制和结构要求
- 提供必要的约束条件避免无关内容

### 2. 性能优化策略

#### Token 使用优化
- 精简提示词内容，移除冗余信息
- 使用模板系统避免重复的上下文描述
- 根据任务复杂度调整提示词长度

#### 成本控制
- 根据任务需求选择合适的模型（GPT-3.5 vs GPT-4）
- 设置合理的最大 Token 限制
- 实施缓存机制避免重复请求

#### 响应质量保证
- 使用适当的温度参数（创造性 vs 准确性）
- 实施重试机制处理临时性错误
- 添加质量验证和错误处理

## 常见问题和解决方案

## 进阶主题

### 1. 多模态提示词
Spring AI 支持多模态输入，包括文本、图像、音频等：

```java
// 创建包含图像的多模态消息
UserMessage multimodalMessage = new UserMessage(
    "请描述这张图片的内容",
    new Media(MimeTypeUtils.IMAGE_JPEG, imageResource)
);

Prompt prompt = new Prompt(List.of(multimodalMessage));
var response = chatModel.call(prompt);
```

## 总结

Spring AI Prompts Cookbook 提供了全面的提示词管理解决方案，涵盖了从基础使用到高级优化的各个方面。通过掌握这些技术和最佳实践，开发者可以：

1. **提高开发效率**: 使用模板化和工具类快速构建提示词
2. **优化使用成本**: 通过监控和优化策略控制 API 调用成本
3. **提升响应质量**: 应用提示词工程技术获得更准确的 AI 回应
4. **构建可靠应用**: 实施错误处理和监控机制确保系统稳定性

建议开发者根据具体项目需求选择合适的技术和方法，并持续优化提示词策略以获得最佳效果。

## 参考资料
- [Spring AI 官方文档](https://docs.spring.io/spring-ai/reference/)
- [OpenAI API 文档](https://platform.openai.com/docs/api-reference)
- [Anthropic Claude API 文档](https://docs.anthropic.com/claude/reference)
- [提示词工程指南](https://www.promptingguide.ai/)
