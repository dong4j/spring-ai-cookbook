# StringTemplate

## 前言

最近在学习 Spring AI 的时候，看到官方文档中提到了一个很陷生的名词：[StringTemplate](https://www.stringtemplate.org/)。作为一个写了多年 Java 的开发者，说实话以前还真没用过这个模板引擎，毕竟市面上 Velocity、FreeMarker、Thymeleaf 这些更主流。

带着好奇心，我开始思考这么几个问题：

1. **StringTemplate 是什么？**
2. **为什么 Spring AI 要选择它作为默认的模板引擎？**
3. **提示词模板不就是简单的字符串替换吗？为什么需要一个专门的模板引擎？**

今天就来简单记录一下我的理解。

## StringTemplate 的来历

StringTemplate 是由 ANTLR（一个著名的语法分析器生成器）的作者 Terence Parr 开发的。他开发这个工具的初衷是为了生成代码，因为 ANTLR 需要根据语法规则生成大量的解析器代码。

这个背景很有意思：**一个用来生成代码的模板引擎**，和我们常用的那些用来渲染网页的模板引擎，设计理念是不一样的。

## 为什么不直接用字符串替换？

最开始我也觉得，提示词模板这事儿，直接 `String.format()` 或者 `String.replace()` 不就行了？比如：

```java
// 最简单的方式
String prompt = String.format("请用 %s 语言解释 %s", language, topic);

// 或者
String prompt = "请用 {language} 语言解释 {topic}"
    .replace("{language}", language)
    .replace("{topic}", topic);
```

看起来很简单对吧？但实际使用中你会遇到这些问题：

### 1. 分隔符冲突

当你的提示词中包含 JSON 或代码时：

```java
String prompt = "请分析这段 JSON: {json}";
// 如果 json = "{\"name\": \"test\"}"
// 直接替换就出问题了，{} 和 {json} 分不清！
```

### 2. 缺乏灵活性

如果你需要根据不同场景调整格式：

```java
// HTML 需要转义
String html = name.replace("<", "&lt;").replace(">", "&gt;");

// SQL 需要转义
String sql = value.replace("'", "''");

// 每种场景都要手动处理，很容易漏！
```

### 3. 代码可读性差

当提示词变复杂时：

```java
String prompt = "请为 " + product + " 写一份产品介绍，" +
                "目标用户是 " + audience + "，" +
                "重点突出 " + feature + "。";
// 字符串拼接看着就头疼...
```

现在你应该能理解，为什么需要一个专门的模板引擎了吧？

## 为什么是 StringTemplate？

Spring AI 选择 StringTemplate 而不是 Velocity 或 FreeMarker，我觉得有这么几个原因：

### 1. 严格的模型-视图分离

StringTemplate 有个很特别的设计原则：**模板里绝对不能有业务逻辑**。

这意味着什么？意味着你不能在模板里做这些事：

```velocity
## Velocity 能这么写（但 StringTemplate 不行）
#if ($user.age > 18)
    #set($discount = 0.8)
#else
    #set($discount = 1.0)
#end
```

StringTemplate 认为，**计算折扣是业务逻辑，应该在 Java 代码里做，不应该放在模板里**。

对于提示词管理来说，这点特别好：

- 提示词就是提示词，干干净净
- 不会因为模板里写了奇奇怪怪的逻辑而变得复杂
- AI 模型接收的就是纯粹的文本，没有其他干扰

### 2. 语法简单

StringTemplate 的语法非常简单，就是 `$name$` 或 `{name}` 这种形式：

```st
请用 {language} 语言解释 {topic} 的核心概念
```

看到这个模板，任何人都能一眼看出哪里是变量，不需要学习复杂的语法。

### 3. 自动转义

这个功能在 Web 开发中很常见，但在提示词管理中其实也很有用。比如你的提示词里可能包含用户输入的内容，自动转义能避免一些意外情况。

### 4. 自定义分隔符

这个功能解决了我前面说的“分隔符冲突”问题。当你的提示词里有 JSON 或代码时，可以换个分隔符：

```java
// 默认使用 {}
String template = "请用 {language} 解释 {topic}";

// 当内容有 {} 时，改用 <>
String template = "请审查以下代码：<code>";
// code = "public class Test { }" // 现在 {} 不会冲突了！
```

## Spring AI 中的实际使用

说了这么多理论，来看看实际怎么用：

```java
@Service
public class PromptExample {

    private final ChatClient chatClient;

    public PromptExample(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    // 最简单的用法
    public String simplePrompt(String userMessage) {
        return chatClient.prompt(userMessage).call().content();
    }

    // 使用模板变量
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

    // 自定义分隔符
    public String customDelimiterTemplate(String code) {
        return chatClient
            .prompt()
            .user(u -> u
                .text("请用一句话审查以下代码: <code>")
                .param("code", code))
            .call()
            .content();
    }
}
```

看起来是不是比字符串拼接清爽多了？

## 与其他模板引擎的对比

这里简单对比一下，帮助理解 Spring AI 为什么选 StringTemplate：

| 特性    | StringTemplate | Velocity  | FreeMarker  |
|-------|----------------|-----------|-------------|
| 业务逻辑  | ❌ 禁止（强制分离）     | ✅ 允许      | ✅ 允许        |
| 学习成本  | ⭐️ 5 分钟上手      | ⭐️⭐️ 需要学习 | ⭐️⭐️⭐️ 比较复杂 |
| 模板复杂度 | ✅ 简单（只能替换变量）   | ⚠️ 中等     | ⚠️ 较复杂      |
| 自动转义  | ✅ 支持           | ❌ 需要手动    | ⭐️ 部分支持     |
| 适用场景  | 提示词、代码生成       | Web 模板    | Web 模板      |

可以看到，对于 **提示词管理** 这个场景：StringTemplate 的“简单”和“强制分离”反而是优势，因为：

- 不需要复杂的逻辑
- 提示词就应该单纯
- 易于维护和理解

---

## 参考资源

- [StringTemplate 官方网站](https://www.stringtemplate.org/)
- [StringTemplate GitHub](https://github.com/antlr/stringtemplate4)
- [Spring AI ChatClient 文档](https://docs.spring.io/spring-ai/reference/api/chatclient.html)
