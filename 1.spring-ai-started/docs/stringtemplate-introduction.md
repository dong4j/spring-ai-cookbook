# 简单了解一下 StringTemplate

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

## 在 Java 里怎么直接玩 StringTemplate？

前面一直在讲 Spring AI 如何帮我们兜底，其实 StringTemplate 本身也可以直接在 Java 里用。我参考了官方文档（<https://github.com/antlr/stringtemplate4/blob/master/doc/java.md>），自己动手试了几段代码，大概是下面这种感觉。

### 🧪 最小可运行示例

```java
import org.stringtemplate.v4.ST;

public class QuickStart {

    public static void main(String[] args) {
        ST template = new ST("请用 <language> 一句话解释 <topic>");
        template.add("language", "中文");
        template.add("topic", "Spring AI");

        String result = template.render();
        System.out.println(result);
    }
}
```

- 模板里的变量用 `<language>`、`<topic>` 这种写法（默认分隔符是 `<` 和 `>`）。
- `add` 方法一旦多次调用同名变量，就是往列表里追加；如果只想替换一次，就只调用一次。
- `render()` 返回的就是最终字符串，没有额外依赖，非常轻量。

### ⚙️ 自定义分隔符 & 批量模板

官方文档里还提到 `STGroup`，用来集中定义模板并自定义分隔符。比如我不想用默认的 `< >`，改用 `{ }`，就可以这样写（注意分隔符只能是单个字符，我第一次写 `{{code}}` 直接报错 😅）：

**方法一：直接使用 ST 构造函数（最简单）**

```java
import org.stringtemplate.v4.ST;

// 直接指定分隔符
ST template = new ST("请审查 {code}，并用 {language} 给出结果。", '{', '}');
template.add("code", "public class Test {}");
template.add("language", "中文");
String result = template.render();
```

> 📝 **完整测试用例**：参见 [StringTemplateDemoTest.method1_directSTConstructor()](https://github.com/dong4j/spring-ai-cookbook/blob/main/1.spring-ai-started/src/test/java/dev/dong4j/ai/spring/prompt/StringTemplateDemoTest.java)

**方法二：使用 STGroup + ST 构造函数（推荐，Spring AI 的实现方式）**

这是 Spring AI 内部 `StTemplateRenderer` 使用的方式，最可靠：

```java
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

// 使用 STGroup 构造函数传入分隔符
STGroup group = new STGroup('{', '}');
// 然后使用 ST 构造函数传入 group 和模板字符串
ST template = new ST(group, "请审查 {code}，并用 {language} 给出结果。");

template.add("code", "public class Test {}");
template.add("language", "中文");
String result = template.render();
```

> 📝 **完整测试用例**：参见 [StringTemplateDemoTest.method2_stGroupWithSTConstructor()](https://github.com/dong4j/spring-ai-cookbook/blob/main/1.spring-ai-started/src/test/java/dev/dong4j/ai/spring/prompt/StringTemplateDemoTest.java)

**方法三：使用 STGroup.defineTemplate（不推荐，有坑）**

```java
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

// ⚠️ 注意：这种方式有问题！
// defineTemplate 在解析模板字符串时使用的是默认分隔符 < >
// 所以即使设置了自定义分隔符，模板字符串中的 {code} 也无法被识别
STGroup group = new STGroup();
group.delimiterStartChar = '{';
group.delimiterStopChar = '}';
group.defineTemplate("audit", "请审查 {code}，并用 {language} 给出结果。"); // ❌ 这样不行
```

> 📝 **完整测试用例**：参见 [StringTemplateDemoTest.method3_defineTemplateWithCustomDelimiter_fails()](https://github.com/dong4j/spring-ai-cookbook/blob/main/1.spring-ai-started/src/test/java/dev/dong4j/ai/spring/prompt/StringTemplateDemoTest.java) - 演示为什么不行

**总结**：`defineTemplate` 方法在解析模板字符串时总是使用默认分隔符 `< >`，无法配合自定义分隔符使用，因此不推荐使用这种方式。建议使用方法一或方法二。

方法一和方法二在两个场景特别好用：

1. **提示词里有大量 `{}`、`<>`**：我可以换成单字符的其他分隔符（例如 `{ }`、`[ ]`、`^ ^`），避免和正文冲突。
2. **多人协作**：把模板集中放在 `STGroup` 或 `.stg` 文件里，大家只要记得模板名称和参数就能复用。

### 🤔 我踩到的小坑

- **STGroup 分隔符设置**：
    - ✅ **正确方式**：`STGroup group = new STGroup('{', '}'); ST st = new ST(group, template);`（这是 Spring AI 的实现方式）
    - ❌ **错误方式**：`STGroup group = new STGroup(); group.delimiterStartChar = '{'; group.defineTemplate("name", "{code}");` - `defineTemplate` 在解析模板字符串时使用的是默认分隔符 `< >`，所以模板字符串中的 `{code}` 无法被识别

- **对象属性访问**：
    - ❌ **不能使用 record**：StringTemplate 通过反射访问对象的 getter 方法，而 Java record 的访问器方法名是 `name()` 而不是 `getName()`，导致模板无法正确访问属性

    - ✅ **推荐使用 Lombok**：使用 `@Data` 或 `@Getter` 注解自动生成标准的 getter 方法（如 `getName()`），StringTemplate 可以正常访问

      ```java [java]
      // ❌ 这样不行 - record 的访问器方法名不匹配
      record User(String name, int age) {}
      ST template = new ST("用户：<user.name>"); // 无法访问，因为 record 是 name() 而不是 getName()
      
      // ✅ 推荐使用 Lombok
      @Data
      @NoArgsConstructor
      @AllArgsConstructor
      class User {
          private String name;
          private Integer age;
      }
      ST template = new ST("用户：<user.name>"); // 可以正常访问，Lombok 生成了 getName()
      ```

  > 📝 **完整测试用例**：参见 [StringTemplateDemoTest.renderPropertyAccess()](https://github.com/dong4j/spring-ai-cookbook/blob/main/1.spring-ai-started/src/test/java/dev/dong4j/ai/spring/prompt/StringTemplateDemoTest.java)

- **列表参数**：同一个变量 `add` 多次就会生成列表，渲染时会自动拼成 `a,b,c`；需要其他格式要记得自定义 renderer。

- **模板校验**：变量名拼写错了不会爆炸，而是直接原样输出，所以我现在习惯写完先加个单元测试跑一遍。

总体来说，这套 API 非常符合“提示词就该简单”的理念：没有控制流、没有复杂语法，但该有的转义、分隔符、自定义 renderer 全都能满足。难怪 Spring AI 直接把它当默认实现。

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
