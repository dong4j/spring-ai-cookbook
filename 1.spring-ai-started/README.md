# Spring AI 特性概览

在 [[guide/2.quick-start|快速开始]] 的终端示例里，我们搭建了一个最小化的 Spring Boot 应用并调用 Spring AI 返回了第一条聊天结果。

虽然流程极简，但完全依赖终端操作：前置环境多、脱离日常开发场景、对非 Linux/Unix 用户也不够友好，而且仅覆盖了 Chat Client API 的最基础能力，对理解 Spring AI 帮助有限。

因此本章节改用贴近真实项目的最小案例，逐个演示 Spring AI 的核心特性，先建立整体认知，再在后续章节深入每个功能点。

## 前置准备

此项目基于 Maven 多模块构建, 已在父 pom.xml 中添加了必要的依赖以及版本信息, 比如 `spring-ai-bom`, `Spring Boot` 的版本, JDK 的版本等, 所以在各个子模块中只会添加必要的依赖.

接下来你应该准备一下环境:

1. 安装 JDK25
2. 通过各种途径获取至少一个 AI 服务商的 API_KEY 并设置到环境变量中
3. 保持网络畅通 🥲

## Chat Client API

Chat Client API 是 Spring AI 提供的高级 API,它简化了与 AI 模型的交互流程。相比于直接使用底层的 `ChatModel` 接口,`ChatClient` 提供了更加流畅和易用的 API 设计。

### 核心特性

- **流式 API 设计**: 使用链式调用构建对话请求,代码更加简洁易读
- **自动装配**: Spring Boot 会根据配置自动创建相应的 `ChatModel` 实例
- **灵活配置**: 支持通过配置文件灵活切换不同的 AI 模型提供商
- **统一接口**: 屏蔽了不同 AI 服务商的差异,提供统一的编程体验

### 基本用法

[SimpleChatClientExampleTest](https://github.com/dong4j/spring-ai-cookbook/blob/main/1.spring-ai-started/src/test/java/dev/dong4j/ai/spring/chatclient/SimpleChatClientExampleTest.java)

::: code-group

```xml [xml:添加依赖]
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-openai</artifactId>
</dependency>
```

```yaml [yaml:添加配置]
spring:
  ai:
    openai:
      api-key: ${QIANWEN_API_KEY}
      base-url: https://dashscope.aliyuncs.com/compatible-mode
      chat:
        options:
          model: qwen2.5-14b-instruct
```

```java [java:修改启动类]
@Service
public class SimpleChatClientExample {

    private final ChatClient chatClient;

    public SimpleChatClientExample(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String chat(String userMessage) {
        return chatClient.prompt().user(userMessage).call().content();
    }
}

```

```java [java:测试用例]
@SpringBootTest
class SimpleChatClientExampleTest {

    @Resource
    private SimpleChatClientExample chatClientExample;

    @Test
    void testChat() {
        String response = chatClientExample.chat("你好");
        assertThat(response).isNotNull();
        assertThat(response).isNotEmpty();
        System.out.println("AI 回复: " + response);
    }
}
```

:::

### 问题

#### AI 输出后为什么没有退出应用

---

## 提示词管理

在实际应用中，我们经常需要复用提示词模板，而不是每次都硬编码提示词内容。Spring AI 提供了强大的提示词管理功能，支持模板变量替换和多种消息类型。

### 基本用法

[PromptExampleTest](https://github.com/dong4j/spring-ai-cookbook/blob/main/1.spring-ai-started/src/test/java/dev/dong4j/ai/spring/chatclient/PromptExampleTest.java)

::: code-group

```java [java:简单提示词]
@Service
public class PromptExample {

    private final ChatClient chatClient;

    public PromptExample(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String simplePrompt(String userMessage) {
        return chatClient.prompt(userMessage).call().content();
    }
}
```

```java [java:使用 System 提示词]
public String promptWithSystem(String userMessage) {
    return chatClient
        .prompt()
        .system("你是一个专业的 Java 开发工程师，擅长 Spring 框架。请用一句话回答问题。")
        .user(userMessage)
        .call()
        .content();
}
```

```java [java:提示词模板]
public String promptWithTemplate(String language, String topic) {
    return chatClient
        .prompt()
        .user(u -> u
            .text("用 {language} 一句话解释 {topic}")
            .param("language", language)
            .param("topic", topic))
        .call()
        .content();
}
```

```java [java:复杂模板]
public String complexTemplate(String product, String targetAudience, String feature) {
    return chatClient
        .prompt()
        .user(u -> u
            .text("用一句话介绍 {product}，面向 {targetAudience}，突出 {feature}")
            .param("product", product)
            .param("targetAudience", targetAudience)
            .param("feature", feature))
        .call()
        .content();
}
```

```java [java:测试用例]
@SpringBootTest
class PromptExampleTest {

    @Autowired
    private PromptExample promptExample;

    @Test
    void testSimplePrompt() {
        String response = promptExample.simplePrompt("用一句话介绍 Spring AI");
        assertThat(response).isNotNull();
        System.out.println("简单提示词 - AI 回复: " + response);
    }

    @Test
    void testPromptWithSystem() {
        String response = promptExample.promptWithSystem("Spring AI 是什么?");
        assertThat(response).contains("Spring");
        System.out.println("系统提示词 - AI 回复: " + response);
    }

    @Test
    void testPromptWithTemplate() {
        String response = promptExample.promptWithTemplate("中文", "ChatClient");
        assertThat(response).isNotEmpty();
        System.out.println("提示词模板 - AI 回复: " + response);
    }

    @Test
    void testComplexTemplate() {
        String response = promptExample.complexTemplate("ChatClient", "开发者", "简单易用");
        assertThat(response).isNotEmpty();
        System.out.println("复杂模板 - AI 回复: " + response);
    }
}
```

:::

### 提示词模板语法

Spring AI 默认使用 [StringTemplate](https://www.stringtemplate.org/) 引擎处理模板，变量使用 `{变量名}` 语法：

```java
public String complexTemplate(String product, String targetAudience, String feature) {
    return chatClient
        .prompt()
        .user(u -> u
            .text("用一句话介绍 {product}，面向 {targetAudience}，突出 {feature}")
            .param("product", product)
            .param("targetAudience", targetAudience)
            .param("feature", feature))
        .call()
        .content();
}
```

### 自定义模板分隔符

如果提示词中包含 JSON 或其他使用 `{}` 的内容，可以自定义模板分隔符：

```java
ChatClient client = ChatClient.builder(chatModel)
    .defaultSystemPrompt("你是一个专业的代码审查助手")
    .build();

String reply = client.prompt()
    .user(u -> u
        .text("请审查以下代码：<code>")
        .param("code", "public class Test { }"))
    .templateRenderer(StTemplateRenderer.builder()
        .startDelimiterToken('<')
        .endDelimiterToken('>')
        .build())
    .call()
    .content();
```

### 参考文档

- [Spring AI ChatClient 官方文档](https://docs.spring.io/spring-ai/reference/api/chatclient.html)
- [[StringTemplate|StringTemplate 引擎]]

---

## 结构化输出

在实际应用中，我们通常需要将 AI 模型的文本输出转换为结构化的 Java 对象，而不是直接处理字符串。Spring AI 提供了强大的结构化输出功能，可以将 AI 的响应自动映射到 POJO（Plain Old Java Object）。

### 基本用法

::: code-group

```java [java:使用 entity() 方法]
// 定义数据类
record ActorFilms(String actor, List<String> movies) {}

ChatClient client = ChatClient.create(chatModel);

// 直接返回 Java 对象
ActorFilms result = client.prompt()
    .user("生成一个随机演员的电影作品列表")
    .call()
    .entity(ActorFilms.class);

System.out.println("演员: " + result.actor());
System.out.println("电影: " + result.movies());
```

```java [java:返回 List 类型]
record ActorFilms(String actor, List<String> movies) {}

ChatClient client = ChatClient.create(chatModel);

// 返回 List 需要使用 ParameterizedTypeReference
List<ActorFilms> results = client.prompt()
    .user("生成 5 个演员的电影作品列表，包括 Tom Hanks 和 Bill Murray")
    .call()
    .entity(new ParameterizedTypeReference<List<ActorFilms>>() {});

results.forEach(actor -> {
    System.out.println(actor.actor() + ": " + actor.movies());
});
```

```java [java:使用 BeanOutputConverter]
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.core.ParameterizedTypeReference;

record Product(String name, String description, Double price, List<String> features) {}

ChatClient client = ChatClient.create(chatModel);
BeanOutputConverter<Product> converter = new BeanOutputConverter<>(Product.class);

// 在提示词中包含格式说明
String format = converter.getFormat();
String prompt = """
    请为 Spring AI 创建一个产品介绍。
    格式要求：
    %s
    """.formatted(format);

Product product = client.prompt()
    .user(prompt)
    .call()
    .entity(Product.class);
```

```java [java:流式响应转结构化]
import org.springframework.ai.converter.BeanOutputConverter;
import reactor.core.publisher.Flux;
import java.util.stream.Collectors;

record ActorFilms(String actor, List<String> movies) {}

ChatClient client = ChatClient.create(chatModel);
BeanOutputConverter<List<ActorFilms>> converter = 
    new BeanOutputConverter<>(new ParameterizedTypeReference<List<ActorFilms>>() {});

// 流式获取响应
Flux<String> flux = client.prompt()
    .user(u -> u
        .text("生成 3 个演员的电影作品列表。格式要求：\n{format}")
        .param("format", converter.getFormat()))
    .stream()
    .content();

// 聚合流式响应并转换
String content = flux.collectList()
    .block()
    .stream()
    .collect(Collectors.joining());

List<ActorFilms> results = converter.convert(content);
```

:::

### 数据类定义

Spring AI 支持使用 Java `record` 或普通类来定义结构化数据：

```java
// 使用 record（推荐，简洁）
record Movie(String title, Integer year, String director, List<String> genres) {}

// 或使用普通类
class Movie {
    private String title;
    private Integer year;
    private String director;
    private List<String> genres;
    
    // getters 和 setters
}
```

### 工作原理

1. **自动格式生成**：`BeanOutputConverter` 会根据 Java 类的字段自动生成 JSON Schema 格式说明
2. **提示词增强**：将格式说明添加到提示词中，引导 AI 生成符合格式的 JSON
3. **自动解析**：AI 返回 JSON 后，Spring AI 自动将其解析为 Java 对象

### 最佳实践

1. **使用 record 类型**：Java 17+ 的 record 类型更简洁，适合定义不可变的数据结构
2. **提供清晰的字段名**：使用有意义的字段名，AI 更容易理解并生成正确的数据
3. **添加格式说明**：在提示词中明确说明输出格式要求，提高准确性
4. **处理复杂嵌套**：对于复杂的嵌套结构，考虑使用 `ParameterizedTypeReference`

### 参考文档

- [Spring AI Structured Output Converter 官方文档](https://docs.spring.io/spring-ai/reference/api/structured-output-converter.html)
- [Spring AI ChatClient 文档](https://docs.spring.io/spring-ai/reference/api/chatclient.html)

## 多模态 API

多模态 API 允许在对话中同时使用文本和图像等媒体内容。这对于图像分析、视觉问答、文档理解等场景非常有用。Spring AI 的 ChatClient 支持在提示词中添加图像、音频等多媒体内容。

### 基本用法

::: code-group

```java [java:文本 + 图像输入]
import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;

ChatClient client = ChatClient.create(chatModel);

// 从类路径加载图像
Resource imageResource = new ClassPathResource("images/diagram.png");

// 同时使用文本和图像
String reply = client.prompt()
    .user(u -> u
        .text("请分析这张图片，描述其中的主要内容")
        .media(MimeTypeUtils.IMAGE_PNG, imageResource))
    .call()
    .content();
```

```java [java:多张图像输入]
import org.springframework.core.io.Resource;
import java.util.List;

ChatClient client = ChatClient.create(chatModel);

Resource image1 = new ClassPathResource("images/chart1.png");
Resource image2 = new ClassPathResource("images/chart2.png");

// 同时分析多张图片
String reply = client.prompt()
    .user(u -> u
        .text("请对比这两张图表，找出它们的差异")
        .media(MimeTypeUtils.IMAGE_PNG, image1)
        .media(MimeTypeUtils.IMAGE_PNG, image2))
    .call()
    .content();
```

```java [java:从 URL 加载图像]
import java.net.URL;
import org.springframework.core.io.UrlResource;

ChatClient client = ChatClient.create(chatModel);

// 从 URL 加载图像
Resource imageUrl = new UrlResource("https://example.com/image.jpg");

String reply = client.prompt()
    .user(u -> u
        .text("这张图片展示了什么？")
        .media(MimeTypeUtils.IMAGE_JPEG, imageUrl))
    .call()
    .content();
```

```java [java:图像 + 结构化输出]
import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;

record ImageAnalysis(String mainSubject, List<String> objects, String description) {}

ChatClient client = ChatClient.create(chatModel);
Resource image = new ClassPathResource("images/product.jpg");

// 结合结构化输出分析图像
ImageAnalysis analysis = client.prompt()
    .user(u -> u
        .text("请分析这张产品图片，提取以下信息：\n" +
              "- 主要产品名称\n" +
              "- 图片中的对象列表\n" +
              "- 产品描述")
        .media(MimeTypeUtils.IMAGE_JPEG, image))
    .call()
    .entity(ImageAnalysis.class);

System.out.println("产品: " + analysis.mainSubject());
System.out.println("对象: " + analysis.objects());
```

:::

### 支持的媒体类型

Spring AI 支持多种媒体类型，常见的有：

- **图像**：`MimeTypeUtils.IMAGE_PNG`、`MimeTypeUtils.IMAGE_JPEG`、`MimeTypeUtils.IMAGE_GIF` 等
- **音频**：`MimeTypeUtils.AUDIO_MPEG`、`MimeTypeUtils.AUDIO_WAV` 等
- **视频**：部分模型支持视频输入（取决于具体的 AI 模型）

### 使用场景

1. **图像分析**：分析图片内容、识别物体、提取文字（OCR）
2. **视觉问答**：基于图像回答问题
3. **文档理解**：分析包含图表的文档
4. **产品识别**：识别产品、提取产品信息
5. **代码截图分析**：分析代码截图并生成解释

### 注意事项

1. **模型支持**：并非所有 AI 模型都支持多模态输入，需要确认使用的模型是否支持（如 GPT-4 Vision、Claude 3 等）
2. **图像大小**：注意图像文件大小限制，某些模型对图像分辨率有要求
3. **资源加载**：确保图像资源路径正确，可以使用 `ClassPathResource`、`FileSystemResource` 或 `UrlResource`
4. **成本考虑**：多模态请求通常比纯文本请求消耗更多 token，成本更高

### 参考文档

- [Spring AI ChatClient 官方文档](https://docs.spring.io/spring-ai/reference/api/chatclient.html)
- [Spring AI 多模态支持](https://docs.spring.io/spring-ai/reference/api/chatmodel.html#_multimodal_support)

## 模型 API

Spring AI 提供了统一的模型 API 抽象，支持多种类型的 AI 模型。所有模型都遵循相同的接口设计，便于在不同模型间切换。

### 核心接口

- **`ChatModel`**：聊天模型接口，用于文本生成和对话
- **`EmbeddingModel`**：嵌入模型接口，用于文本向量化
- **`ImageModel`**：图像生成模型接口
- **`AudioModel`**：音频处理模型接口
- **`ModerationModel`**：内容审核模型接口

### 模型提供者

Spring AI 支持多种模型提供者，包括 OpenAI、Anthropic、Google、Azure、Amazon Bedrock、Ollama 等。通过统一的 API，可以轻松切换不同的模型提供者。

### 参考文档

- [Spring AI Model API 官方文档](https://docs.spring.io/spring-ai/reference/api/chatmodel.html)

## 聊天模型

聊天模型（Chat Model）是 Spring AI 最核心的模型类型，用于处理文本对话和生成任务。

::: code-group

```java [java:基本用法]
@Autowired
private ChatModel chatModel;

// 直接使用 ChatModel
ChatResponse response = chatModel.call(
    new Prompt("请介绍一下 Spring AI")
);

String content = response.getResult().getOutput().getContent();
```

```java [java:流式响应]
Flux<ChatResponse> stream = chatModel.stream(
    new Prompt("写一首关于春天的诗")
);

stream.subscribe(response -> {
    System.out.print(response.getResult().getOutput().getContent());
});
```

```yaml [yaml:模型配置]
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4
          temperature: 0.7
          max-tokens: 1000
```

:::

### 参考文档

- [Spring AI Chat Model 官方文档](https://docs.spring.io/spring-ai/reference/api/chatmodel.html)

## 嵌入模型

嵌入模型（Embedding Model）将文本转换为数值向量，用于语义搜索、相似度计算、RAG 等场景。

::: code-group

```java [java:基本用法]
@Autowired
private EmbeddingModel embeddingModel;

// 单个文本嵌入
EmbeddingResponse response = embeddingModel.embedForResponse(
    List.of("Spring AI 是一个强大的 AI 框架")
);

List<Double> vector = response.getResult().getOutput();

// 批量嵌入
List<String> texts = List.of("文本1", "文本2", "文本3");
EmbeddingResponse batchResponse = embeddingModel.embedForResponse(texts);
```

```java [java:计算相似度]
EmbeddingResponse embedding1 = embeddingModel.embedForResponse(
    List.of("Spring AI")
);
EmbeddingResponse embedding2 = embeddingModel.embedForResponse(
    List.of("Spring Framework")
);

List<Double> vector1 = embedding1.getResult().getOutput();
List<Double> vector2 = embedding2.getResult().getOutput();

// 使用余弦相似度计算
double similarity = cosineSimilarity(vector1, vector2);
```

:::

### 参考文档

- [Spring AI Embedding Model 官方文档](https://docs.spring.io/spring-ai/reference/api/embeddings.html)

## 图像模型

图像模型（Image Model）用于生成图像，支持文本到图像的转换。

::: code-group

```java [java:基本用法]
@Autowired
private ImageModel imageModel;

// 生成图像
ImageResponse response = imageModel.call(
    new ImagePrompt("一只可爱的小猫坐在窗台上")
);

// 获取生成的图像 URL 或 Base64
String imageUrl = response.getResult().getOutput().getUrl();
byte[] imageData = response.getResult().getOutput().getB64Json();
```

```java [java:图像生成选项]
ImageOptions options = ImageOptionsBuilder.builder()
    .withModel("dall-e-3")
    .withSize("1024x1024")
    .withQuality("hd")
    .withN(1)
    .build();

ImageResponse response = imageModel.call(
    new ImagePrompt("一幅未来城市的科幻画作", options)
);
```

:::

### 参考文档

- [Spring AI Image Model 官方文档](https://docs.spring.io/spring-ai/reference/api/imageclient.html)

## 音频模型

音频模型（Audio Model）支持语音转文字（STT）和文字转语音（TTS）功能。

::: code-group

```java [java:语音转文字]
@Autowired
private AudioTranscriptionModel transcriptionModel;

// 从文件转文字
Resource audioFile = new ClassPathResource("audio/speech.wav");
TranscriptionResponse response = transcriptionModel.call(
    new AudioTranscriptionPrompt(audioFile)
);

String transcript = response.getResult().getOutput();
```

```java [java:文字转语音]
@Autowired
private AudioSpeechModel speechModel;

// 文字转语音
SpeechResponse response = speechModel.call(
    new AudioSpeechPrompt("你好，欢迎使用 Spring AI")
);

// 获取音频数据
byte[] audioData = response.getResult().getOutput();
```

:::

### 参考文档

- [Spring AI Audio Model 官方文档](https://docs.spring.io/spring-ai/reference/api/audio/transcriptions.html)

## 内容审核

内容审核模型（Moderation Model）用于检测文本中的有害内容，如暴力、仇恨言论、色情内容等。

::: code-group

```java [java:基本用法]
@Autowired
private ModerationModel moderationModel;

// 审核内容
ModerationResponse response = moderationModel.call(
    new ModerationPrompt("这是一段需要审核的文本")
);

// 检查是否被标记
boolean flagged = response.getResult().isFlagged();

// 获取分类结果
Map<String, Boolean> categories = response.getResult().getCategories();
```

:::

### 参考文档

- [Spring AI Moderation Model 官方文档](https://docs.spring.io/spring-ai/reference/api/moderation.html)

## 聊天记忆

聊天记忆（Chat Memory）用于管理多轮对话的上下文，确保 AI 能够记住之前的对话内容。

::: code-group

```java [java:基本用法]
@Autowired
private ChatModel chatModel;

// 创建聊天记忆
InMemoryChatMemory chatMemory = new InMemoryChatMemory();

// 添加对话历史
chatMemory.add(new UserMessage("我的名字是张三"));
chatMemory.add(new AssistantMessage("你好，张三！"));

// 使用记忆进行对话
ChatResponse response = chatModel.call(
    new Prompt(chatMemory.getMessages(), "请记住我的名字")
);
```

```yaml [yaml:JDBC 持久化配置]
spring:
  ai:
    chat:
      memory:
        repository:
          jdbc:
            initialize-schema: always
```

```java [java:JDBC 持久化记忆]
@Autowired
private JdbcChatMemoryStore memoryStore;

// 创建带持久化的记忆
ChatMemory chatMemory = new PersistentChatMemory(
    memoryStore, 
    "conversation-id-123"
);
```

:::

### 参考文档

- [Spring AI Chat Memory 官方文档](https://docs.spring.io/spring-ai/reference/api/chat-memory.html)

## 工具调用

工具调用（Tool Calling）允许 AI 模型调用外部函数或服务，实现更强大的功能。

::: code-group

```java [java:定义工具]
@Component
public class WeatherService {
    
    @Tool("获取指定城市的天气信息")
    public String getWeather(@P("城市名称") String city) {
        // 调用天气 API
        return "北京：晴天，25°C";
    }
    
    @Tool("计算两个数字的和")
    public int add(@P("第一个数字") int a, @P("第二个数字") int b) {
        return a + b;
    }
}
```

```java [java:使用工具]
@Autowired
private ChatClient chatClient;

// 工具会自动注册到 ChatClient
String response = chatClient.prompt()
    .user("北京今天天气怎么样？")
    .call()
    .content();
// AI 会自动调用 getWeather("北京") 工具
```

:::

### 参考文档

- [Spring AI Tool Calling 官方文档](https://docs.spring.io/spring-ai/reference/api/tools.html)

## 模型上下文协议

模型上下文协议（Model Context Protocol, MCP）是一个标准化的协议，用于在 AI 应用和外部资源之间建立连接。

::: code-group

```java [java:MCP 服务器]
@Bean
public McpServer mcpServer() {
    return McpServer.builder()
        .name("my-mcp-server")
        .version("1.0.0")
        .tools(List.of(/* 工具列表 */))
        .resources(List.of(/* 资源列表 */))
        .build();
}
```

```java [java:使用 MCP 资源]
@Autowired
private ChatClient chatClient;

// MCP 资源会自动注入到对话中
String response = chatClient.prompt()
    .user("查询数据库中的用户信息")
    .call()
    .content();
```

:::

### 参考文档

- [Spring AI MCP 官方文档](https://docs.spring.io/spring-ai/reference/api/mcp.html)

## 检索增强生成

检索增强生成（RAG, Retrieval-Augmented Generation）结合了信息检索和文本生成，让 AI 能够基于外部知识库回答问题。

::: code-group

```java [java:基本配置]
@Bean
public VectorStore vectorStore(EmbeddingModel embeddingModel) {
    return new SimpleVectorStore(embeddingModel);
}

@Bean
public RetrievalAugmentationAdvisor retrievalAdvisor(
    VectorStore vectorStore, 
    EmbeddingModel embeddingModel
) {
    return new RetrievalAugmentationAdvisor(
        vectorStore, 
        embeddingModel
    );
}
```

```java [java:使用 RAG]
@Autowired
private ChatClient chatClient;

// RAG Advisor 会自动检索相关文档并增强提示词
String response = chatClient.prompt()
    .user("Spring AI 的核心特性是什么？")
    .call()
    .content();
```

:::

### 参考文档

- [Spring AI RAG 官方文档](https://docs.spring.io/spring-ai/reference/api/retrieval-augmented-generation.html)

## 模型评估

模型评估（Model Evaluation）用于评估 AI 模型的输出质量，帮助优化提示词和模型选择。

::: code-group

```java [java:基本用法]
@Autowired
private EvaluationModel evaluationModel;

// 评估模型输出
EvaluationResponse response = evaluationModel.evaluate(
    "原始问题",
    "模型回答",
    "期望答案"
);

double score = response.getScore();
```

:::

### 参考文档

- [Spring AI Model Evaluation 官方文档](https://docs.spring.io/spring-ai/reference/api/testing.html)

## 向量数据库

向量数据库（Vector Database）用于存储和检索高维向量数据，是 RAG 系统的核心组件。

Spring AI 支持 20+ 种向量数据库，包括 PostgreSQL (PGVector)、MongoDB Atlas、Redis、Pinecone、Qdrant、Milvus 等。

::: code-group

```yaml [yaml:PGVector 配置]
spring:
  ai:
    vectorstore:
      pgvector:
        dimensions: 1536
        initialize-schema: true
```

```java [java:使用向量数据库]
@Autowired
private VectorStore vectorStore;

// 添加文档
vectorStore.add(List.of(
    new Document("Spring AI 是一个强大的框架")
));

// 相似度搜索
List<Document> results = vectorStore.similaritySearch(
    SearchRequest.query("AI 框架")
);
```

:::

### 参考文档

- [Spring AI Vector Database 官方文档](https://docs.spring.io/spring-ai/reference/api/vectordbs.html)

## 可观测性

可观测性（Observability）提供了 AI 操作的监控、追踪和日志记录能力。

::: code-group

```yaml [yaml:启用可观测性]
spring:
  ai:
    observability:
      enabled: true
      tracing:
        enabled: true
```

```java [java:查看指标]
@Autowired
private MeterRegistry meterRegistry;

// 查看 AI 调用次数
Counter counter = meterRegistry.counter("spring.ai.chat.calls");
long count = counter.count();
```

:::

### 参考文档

- [Spring AI Observability 官方文档](https://docs.spring.io/spring-ai/reference/observability/index.html)

## 编排

编排（Orchestration）涉及使用 Docker Compose 等工具管理多个服务的部署和运行。

::: code-group

```yaml [yaml:Docker Compose 示例]
version: '3.8'
services:
  postgres:
    image: pgvector/pgvector:pg16
    environment:
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"
  
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
```

:::

### 参考文档

- [Docker Compose 官方文档](https://docs.docker.com/compose/)

## 测试容器

测试容器（Testcontainers）用于在测试中启动真实的容器化服务，确保测试环境的一致性。

### 基本用法

```java
@SpringBootTest
@Testcontainers
class MyApplicationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
        "pgvector/pgvector:pg16"
    )
    .withDatabaseName("testdb")
    .withUsername("test")
    .withPassword("test");
    
    @Test
    void testWithDatabase() {
        // 使用真实的 PostgreSQL 容器进行测试
    }
}
```

### 参考文档

- [Testcontainers 官方文档](https://www.testcontainers.org/)

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

#### Maven 父级覆盖

由于 Maven 的设计，元素会从父级 POM 继承到项目 POM。
虽然大部分继承都很好，但它也会从父级继承不需要的元素，如 `<license>` 和 `<developers>`。
为防止这种情况，项目 POM 包含这些元素的空覆盖。
如果手动切换到不同的父级并确实需要继承，则需要删除这些覆盖。

