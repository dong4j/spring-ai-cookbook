# 入门篇：快速搭建 Spring AI 项目

---

## 📦 代码示例

查看完整代码示例：

[1.spring-ai-started](https://github.com/dong4j/spring-ai-cookbook/tree/main/1.spring-ai-started)

<!-- 代码链接 -->

[[0.spring-ai-introduction/index|Spring AI 简介]]

::: timeline 2023-05-24

- **do some thing1**
- do some thing2
  :::

::: timeline 2023-05-23
do some thing3
do some thing4
:::

::: code-group

```sh [npm]
npm install vitepress-plugin-group-icons
```

```sh [yarn]
yarn add vitepress-plugin-group-icons
```

```sh [pnpm]
pnpm add vitepress-plugin-group-icons
```

```sh [bun]
bun add vitepress-plugin-group-icons
```

:::

```js [vite.config.js]
import legacy from '@vitejs/plugin-legacy'
import { defineConfig } from 'vite'

export default defineConfig({
  plugins: [
    legacy({
      targets: ['defaults', 'not IE 11'],
    }),
  ],
})
```

```json [.json]
{
  "name": "publishBlog",
  "parameters": {
    "type": "object",
    "properties": {
      "title": {"type": "string"},
      "content": {"type": "string"},
      "tags": {"type": "array", "items": {"type": "string"}}
    },
    "required": ["title", "content"]
  }
}
```

```java [java]
public class HelloWorld {}
```

```xml [xml]
```

```markmap
# 前端面试
## HTML
- 语义化标签
- SEO 优化
## CSS
- Flex 布局
- Grid 布局
## JavaScript
- 闭包
- 事件循环
```

```mermaid
sequenceDiagram
    participant U as 用户
    participant S as 服务器
    U->>S: 请求登录
    S-->>U: 返回 Token
    U->>S: 携带 Token 请求数据
    S-->>U: 返回用户数据
```
