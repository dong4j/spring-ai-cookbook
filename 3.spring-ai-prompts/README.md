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

### 5. PromptTemplate
提示词模板旨在促进创建结构化的提示词，
将提示词内容定义为模板，并使用占位符进行填充，最终输出一个完整的提示词交给大模型。

#### 1. PromptTemplateStringActions
返回 `String` 类型的提示词模板渲染接口，专注于创建和渲染提示字符串，提示词接口中最基本的形式

```java
public interface PromptTemplateStringActions {
	// 返回字符内容，不需要额外数据
	String render();
	// 可结合参数，替换提示词模板中的占位符
	String render(Map<String, Object> model);

}
```

#### 2. PromptTemplateMessageActions
返回 `Message` 类型的提示词模板渲染接口，专为通过生成和操作 Message 对象来创建提示而设计
还记得嘛？一个 `Message` 对象继承 `Content`，包含一个 `MessageType` 的 get 方法

```java
public interface PromptTemplateMessageActions {
    
    // 直接创建一个 Message，不需要额外数据，用于静态或预定义的消息内容
    Message createMessage();
        
    // 用 `List<Media>` 转换创建一个 Message
    Message createMessage(List<Media> mediaList);

    // 可结合参数，替换提示词模板中的占位符
    Message createMessage(Map<String, Object> model);
}
```

#### 3. PromptTemplateActions
`PromptTemplateActions` 用于返回 `Prompt` 对象，从 #4. Prompt 中我们可以知道 `Prompt` 对象中含有一串 `Message` 和
用于与大模型对话过程中可调节的一些参数对象 `ChatOptions`

该接口继承至 `PromptTemplateStringActions` 他的定义如下

```java
public interface PromptTemplateActions extends PromptTemplateStringActions {

	Prompt create();

	Prompt create(ChatOptions modelOptions);

	Prompt create(Map<String, Object> model);

	Prompt create(Map<String, Object> model, ChatOptions modelOptions);
}
```

## 快速开始
现在让我们快速进行配置，启动一个 Spring Boot 应用，并定义一些示例代码

### 1. 配置设置
在 `application.yml` 中配置必要的参数：

```yaml
spring:
  ai:
    openai:
      base-url: https://dashscope.aliyuncs.com/compatible-mode
      api-key: ${QIANWEN_API_KEY}
```

### 2. 启动应用
```bash
mvn spring-boot:run
```

### 3. 访问示例端点

#### 基础提示词示例
```bash
# 简单字符串提示词
curl "http://localhost:8080/prompts/simple?input=给我讲个笑话吧"

# 用户消息提示词
curl "http://localhost:8080/prompts/user-message"

# 多消息提示词
curl "http://localhost:8080/prompts/multi-message"

# 带选项的提示词（创意模式）
curl "http://localhost:8080/prompts/with-options?creative=true"

# 带选项的提示词（普通模式）
curl "http://localhost:8080/prompts/with-options?creative=false"
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
curl "http://localhost:8080/roles/system-user?assistantName=技术专家&voice=专业且友好&question=请解释什么是微服务架构"
```

#### 提示词工程示例
```bash
# Zero-shot 学习技术
curl "http://localhost:8080/engineering/zero-shot?task=情感分类&input=这个产品的质量真的很棒！"

# Few-shot 学习技术
curl "http://localhost:8080/engineering/few-shot?task=文本风格转换&input=请把这句话改为正式商务风格：我们明天开会讨论"
```

## 示例说明

### 基础提示词 (PromptController)

#### 简单字符串提示词
最基础的提示词方式，适合快速原型开发和简单问答场景：

```java
@GetMapping("/simple")
public String simplePrompt(@RequestParam(defaultValue = "给我讲个笑话吧") String input) {
    return openAiChatClient.prompt(input)
        .call()
        .content();
}
```

#### 用户消息提示词
显式创建 UserMessage 对象，提供更细粒度的控制：

```java
@GetMapping("/user-message")
public String promptWithUserMessage() {
    String userText = """
        告诉我两种著名的编程语言，并分别为每种语言提供简要概述。
        """;
    UserMessage userMessage = new UserMessage(userText);

    return openAiChatClient.prompt().messages(userMessage).call().content();
}
```

#### 多消息提示词
通过组合不同类型的消息，可以创建更精确和上下文相关的提示词：

```java
@GetMapping("/multi-message")
public String promptWithMultipleMessages() {
    // 创建系统消息
    var systemMessage = new SystemMessage("""
        你是一个专业的技术导师。
        你的回答应该简洁明了，重点突出核心概念。
        每个回答不要超过200字。
        """);

    // 创建用户消息
    var userMessage = new UserMessage("请解释什么是微服务架构？");

    // 创建提示词
    Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

    // call
    return openAiChatClient.prompt(prompt).call().content();
}
```

#### 带选项的提示词
通过配置选项控制 AI 模型的行为和输出特征：

```java
@GetMapping("/with-options")
public String promptWithOptions(@RequestParam(defaultValue = "false") boolean creative) {
    String promptText = "请用创意的方式解释什么是人工智能";

    // 根据参数设置不同的选项
    var options = creative
        ? OpenAiChatOptions.builder().temperature(0.9).build()
        : OpenAiChatOptions.builder().temperature(0.3).build();

    Prompt prompt = new Prompt(promptText, options);
    var content = openAiChatClient.prompt(prompt).call().content();

    return String.format(
        "创意模式: %s%n温度参数: %s%n%n%s", creative, options.getTemperature(), content);
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

    String template = "Tell me a {adjective} fact about {topic}";

    PromptTemplate promptTemplate = new PromptTemplate(template);
    Map<String, Object> variables = new HashMap<>();
    variables.put("adjective", adjective);
    variables.put("topic", topic);

    String renderedPrompt = promptTemplate.render(variables);

    return openAiChatClient.prompt().user(renderedPrompt).call().content();
}
```

#### 自定义分隔符模板
当模板内容包含 JSON 等格式时，可以使用自定义分隔符避免冲突：

```java
@GetMapping("/custom-delimiter")
public String customDelimiterTemplate(
    @RequestParam(defaultValue = "John Williams") String composer) {

    // 使用 StringTemplate 引擎的自定义分隔符
    var promptTemplate = PromptTemplate.builder()
        .renderer(
            StTemplateRenderer.builder()
                .startDelimiterToken('<')
                .endDelimiterToken('>')
                .build())
        .template("""
            告诉我由 <composer> 创作配乐的5部电影的名字。
            对于每部电影，请注明其上映年份。
            """)
        .build();

    Map<String, Object> variables = Map.of("composer", composer);
    String renderedPrompt = promptTemplate.render(variables);

    String response = openAiChatClient.prompt().user(renderedPrompt).call().content();

    return String.format("渲染后的提示词:%n%s%n%nAI 回复:%n%s", renderedPrompt, response);
}
```

#### 资源文件模板
从资源文件加载模板内容，便于管理和维护：

```java
@GetMapping("/resource")
public String resourceTemplate(@RequestParam(defaultValue = "Java") String language) {
    try {
        // 加载系统消息模板资源
        ClassPathResource systemResource = new ClassPathResource("templates/system-message.st");
        if (!systemResource.exists()) {
            throw new IllegalStateException("模板文件不存在: templates/system-message.st");
        }

        // 使用 Spring 的 StreamUtils 读取为字符串（自动处理编码，默认 UTF-8）
        String templateContent = StreamUtils.copyToString(
            systemResource.getInputStream(),
            StandardCharsets.UTF_8);

        Map<String, Object> systemVariables = Map.of(
            "language", language,
            "name", "AI编程助手",
            "voice", "专业且友好");

        PromptTemplate systemPromptTemplate = PromptTemplate.builder()
            .renderer(StTemplateRenderer.builder()
                          .startDelimiterToken('<')
                          .endDelimiterToken('>')
                          .build())
            .template(templateContent)
            .build();

        String systemPrompt = systemPromptTemplate.render(systemVariables);
        String userMessage = "请介绍" + language + "的主要特性和应用场景";

        String response = openAiChatClient
            .prompt()
            .system(systemPrompt)
            .user(userMessage)
            .call()
            .content();

        return String.format("系统模板变量: %s%n%nAI 回复:%n%s", systemVariables, response);

    } catch (Exception e) {
        return "模板加载失败: " + e.getMessage();
    }
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

    String systemText =
        """
            你是一个有帮助的AI助手，帮助人们找到信息。
            你的名字是：{name}
            你应该用{voice}的语调来回应用户的请求。
            回答应准确、全面且易于理解。
            """;

    PromptTemplate systemPromptTemplate = new PromptTemplate(systemText);
    Message systemMessage =
        systemPromptTemplate.createMessage(
            Map.of(
                "name", assistantName,
                "voice", voice));

    Message userMessage = new UserMessage(question);

    Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
    var response = openAiChatClient.prompt(prompt).call().content();

    return String.format("""
        系统角色设置:
        %s
        助手名字: %s
        回应风格: %s

        用户问题: %s

        AI 回复:
        %s
        """, systemText, assistantName, voice, question, response);
}
```

### 提示词工程 (PromptEngineeringController)

#### Zero-shot 学习技术
让 AI 模型在没有先前示例的情况下完成特定任务：

```java
@GetMapping("/zero-shot")
public String zeroShotPrompt(
    @RequestParam(defaultValue = "情感分类") String task,
    @RequestParam(defaultValue = "这个产品的质量真的很棒！") String input) {

    String zeroShotTemplate =
        """
            你是一个专业的{task}专家。
            请直接对以下内容进行{task}，无需额外解释。

            输入内容：{input}

            请以 JSON 格式输出结果：
            {{"result": "你的分类结果", "confidence": "置信度"}}
            """;

    PromptTemplate promptTemplate = new PromptTemplate(zeroShotTemplate);
    Map<String, Object> variables = Map.of("task", task, "input", input);

    String renderedPrompt = promptTemplate.render(variables);
    String response = openAiChatClient.prompt().user(renderedPrompt).call().content();

    return String.format("""
                         === Zero-shot 学习技术演示 ===
                         任务类型: %s
                         输入内容: %s
                         技术说明: 模型在没有先前示例的情况下完成特定任务

                         AI 处理结果:
                         %s
                         """, task, input, response);
}
```

#### Few-shot 学习技术
通过在提示词中提供少量示例来帮助 AI 模型理解任务模式：

```java
@GetMapping("/few-shot")
public String fewShotPrompt(
    @RequestParam(defaultValue = "文本风格转换") String task,
    @RequestParam(defaultValue = "请把这句话改为正式商务风格：我们明天开会讨论") String input) {

    String fewShotTemplate =
        """
            你是一个专业的{task}专家。

            以下是一些{task}的示例：

            示例1:
            输入: "这个想法不错，我们试试看"
            输出: "此建议具有可行性，建议予以采纳并试行"

            示例2:
            输入: "我搞不懂这个怎么回事"
            输出: "对该问题存在理解困难，需要进一步说明"

            示例3:
            输入: "弄快点，没时间了"
            输出: "请加快处理进度，时间较为紧迫"

            现在请对以下内容进行{task}：
            输入: "{input}"

            输出:
            """;

    PromptTemplate promptTemplate = new PromptTemplate(fewShotTemplate);
    Map<String, Object> variables =
        Map.of(
            "task", task,
            "input", input);

    String renderedPrompt = promptTemplate.render(variables);
    String response = openAiChatClient.prompt().user(renderedPrompt).call().content();

    return String.format("""
                         === Few-shot 学习技术演示 ===
                         任务类型: %s
                         输入内容: %s
                         技术说明: 通过提供少量示例帮助模型理解任务模式
                         提供的示例数: 3个

                         AI 处理结果:
                         %s
                         """, task, input, response);
}
```

## 动手操练

- 1. 将 `controller` 包下的所有接口改造为流式输出
- 2. 在 `controller` 包下新增一个用于多模态的 `Prompt` 示例

## 最佳实践指南

### 提示词四大基础

#### 1. 把话说清楚
##### 1.1. 指令要"无歧义"

确保指令明确具体，避免模糊表达

```text
错误示例："分析一下这个"
正确示例："总结这篇论文的关键发现，并用项目符号列表展示""
```

##### 1.2. "要"什么，别"不要"什么

明确表达你想要的内容，而非不想要的

```text
错误示例："语气别太随意"
正确示例："请使用正式、学术的语调写作。"
```

##### 1.3. 什么时候用"不要"

"不要"适用于"控制边界"，而非定义核心任务。
使用"不要"来限定主题范围或排除不相关内容，帮助AI更精准地理解你的需求

```text
示例："解释什么是'经济计量学'，但不要使用复杂的数学公式或行话。"
```


#### 2. 给够上下文
为AI提供充足的背景信息，有助于其准确理解任务并调用相关知识。

##### 2.1. 喂饱背景信息

提供任务相关的完整背景资料，避免信息碎片化

##### 2.2. 指定"读者"是谁

明确受众能帮助AI调整语言风格、术语和内容深度
```text
示例："给一个5岁小孩解释什么是量子计算"与"给一个物理学博士写量子计算的综述"
```

#### 3. 用好脚手架
通过结构化的提示词，引导AI生成更可靠、更专业的答案

##### 3.1. 给它一个"角色"

赋予AI特定"人设"，使其以该角色的风格、术语和思维模式进行输出
```text
示例："你是一名资深前端开发工程师，精通现代 Web 技术栈，注重代码质量、性能与用户体验。"
```

##### 3.2. 用好"分隔符"

使用清晰的分隔符区分提示词的不同部分，帮助AI理解结构
```text
示例：使用三重反引号或XML标签<example>来区分指令和背景资料
```

##### 3.3. 规定"输出格式"

明确指定输出格式，便于程序调用或内容渲染
```text
示例："请用JSON格式回答"或"以Markdown表格形式展示结果"
```

#### 4. 学会"迭代"
Prompt编写是一个持续测试、调整和优化的科学实验过程。目前有多种协助写好一份提示词的开源工具，可选择性使用

##### 4.1. 迭代优化

遵循 `"测试 -> 看哪儿不对 -> 调整 -> 再测试"` 的循环，不断优化提示词。

##### 4.2. 应对常见"毛病"

针对AI常见的幻觉、重复、模糊、前后不一等问题，采取不同策略，如使用RAG解决幻觉问题。

##### 4.3. 像"写代码"一样管提示词

对提示词进行版本控制、使用占位符动态填充数据、进行A/B测试，将其视为代码进行管理。


### 提示词进阶指引

#### 1. In-Context Learning（上下文学习）
In-Context Learning（上下文学习）是大语言模型（Large Language Models, LLMs）中一种重要的推理能力，
指的是模型在不更新自身参数的情况下，仅通过在输入提示（prompt）中提供若干示例（demonstrations），就能学会执行新任务的能力。

```text
提示：

将文本分类为中性、负面或正面。
文本：我认为这次假期还可以。
情感：
```

```text
输出：

中性
```

在上面的提示词中，我们没有向模型提供任何示例——这就是 `Zero-Shot Learning` 零样本学习

有时候我们发现在更复杂的任务上 `Zero-Shot Learning` 仍然表现不佳。模型可能没有按照我们的预期进行输出，
这时候，`Few-Shot Learning` 可以作为一种技术，以启用上下文学习，让模型学习我们给定的示例用以引导模型，以实现更好的性能。

`Few-Shot Learning` 适用于复杂任务，需要特定格式输出

#### 2. 思维链

要求AI"一步一步地思考"，将思考过程外化，从而提高复杂问题的准确率。适用于多步骤、有清晰顺序的问题

```text
问题 --> 逐步思考 --> 最终答案
```

示例：
在数学题中，要求AI列出每一步计算过程


#### 3. Self-Consistency（自洽性）

CoT的增强版，通过多次独立计算，选择出现次数最多的答案，提高复杂推理的准确性。适用于有多种解法的问题（如数学）和开放性问题

```text
多次独立计算 --> 多数投票 --> 最终答案
```

示例：
- 调高temperature参数，让AI进行多次（如10次）独立计算。
- 选择出现次数最多的答案（少数服从多数）

#### 4. 推理技术对比

不同推理技术适用于不同类型的复杂任务，选择合适的技术可以提高AI解决问题的效率和准确性。

| 技术          | 适用问题                       | 成本           | 复杂度 | 主要优势     |
| ------------- | ------------------------------ | -------------- | ------ | ------------ |
| 思维链 (CoT)  | 多步骤、有清晰顺序的问题       | 低             | 低     | 简单、可解释 |
| 自洽性        | 有多种解法的问题（如数学）     | 中（N次调用）  | 低     | 准确、鲁棒性 |
| 思维树 (ToT)  | 需要规划、探索、回溯的复杂问题 | 高（大量调用） | 高     | 解决超难题   |
| 验证链 (CoVe) | 容易产生"幻觉"的事实性问题     | 中             | 较高   | 减少胡说八道 |


#### 5. 从"思考"到"行动"：AI智能体技术

##### ReAct (推理+行动)

通过"思考-行动-观察"的循环，使AI能够：

- 调用外部工具（如搜索引擎）
- 获取实时信息
- 解决仅凭静态知识无法回答的问题


##### Reflexion（反思）

ReAct的升级版，通过：

- 生成"失败总结"并存储经验
- 下次执行任务时避免犯同样的错误
- 实现"吃一堑长一智"的学习能力

##### PAL (程序辅助语言模型)

将AI的"思考过程"以"代码"形式呈现：

- AI负责理解问题和编写逻辑
- 系统执行代码进行精确计算
- 将精确计算交给擅长此任务的解释器

#### 6. Meta-Prompting（元提示词）
元提示词 是“用来生成提示词的提示词”。换句话说，它是一条指令，让 AI 帮你写出另一条更具体、更高质量的提示词。

传统提示的局限性
- 传统的提示通常由人类手动设计。
- 手动设计提示可能需要大量的专业知识和反复试验，耗时耗力。
- 手动设计的提示可能难以适应不同的任务或数据集。
- 对于复杂的任务，手动设计提示可能难以充分发挥LLM 的潜力。

##### 6.1. 元提示词的特点
- 定义解决某类问题的通用框架
- 让AI充当"提示工程师"角色
- 生成可重用的提示词模板
- 提高提示词编写的效率和一致性

##### 6.2. 元提示词的应用场景
- 创建领域通用提示模板
- 构建特定任务的提示词框架
- 让AI生成即使不懂领域知识也能写出80分提示词的模板
- 减少重复编写相似提示词的工作量


#### 7. Context Engineering（上下文工程）

`Context Engineering` 的概念主要源自 Tobi Lütke 和 Andrej Karpathy 的推特：“It describes the core skill better: the art of providing all the context for the task to be plausibly solvable by the LLM.” （提供所有上下文的艺术，以使任务能够被 LLM 合理地解决） && "When in every industrial-strength LLM app, context engineering is the delicate art and science of filling the context window with just the right information for the next step." （上下文工程是一门微妙的艺术与科学，旨在在上下文窗口中填入恰到好处的信息，为下一步推理做准备）

首先，什么是上下文（Context）？它是一种能提供给 LLM 的、用于完成下一步推理或生成任务的全部**信息**集合。引用一张图来进行概括：

![上下文集合](https://pic3.zhimg.com/v2-3812bc36dc1eb599bbdec581d388ee02_1440w.jpg)

为了更好的理解，我们可以将上下文信息按照以下分类：

- 指导性上下文。这类上下文的核心功能是指导模型该做什么以及如何做，主要为模型行为设定框架，目标和规则。`Prompt Engineering` 一般囊括在这一层分类内

- 信息性上下文。这类上下文的核心功能是告诉模型需要知道什么知识，为模型提供解决问题所必备的事实，数据与知识

- 行动性上下文。这类上下文的核心功能是告诉模型能做什么以及做了之后的结果，为模型提供与外部世界交互的能力。它包括：Tool Definition；Tool Calls & Results / Tool Traces

![上下文信息分类](https://picx.zhimg.com/v2-915e587da008d07dc35d7323070a4f39_1440w.jpg)

上下文信息我们知道了，那么什么是上下文工程？

`Context Engineering` 是一门系统性学科，专注于设计、构建并维护一个动态系统，该系统负责在 `Agent` 执行任务的每一步，为其智能地组装出最优的上下文组合，以确保任务能够被可靠、高效地完成。


## 参考资料
- [Spring AI 官方文档](https://docs.spring.io/spring-ai/reference/api/prompt.html)
- [提示词工程指南](https://www.promptingguide.ai/zh)
- [Gemini 提示词指南 101](https://services.google.com/fh/files/misc/gemini-for-google-workspace-prompting-guide-101.pdf)
- [火山提示词优化项目 PromptPilot](https://promptpilot.volcengine.com/workbench)
- [开源提示词优化项目 YPrompt](https://github.com/fish2018/YPrompt)
- [Language Models are Few-Shot Learners](https://arxiv.org/abs/2005.14165)
