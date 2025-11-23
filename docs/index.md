---
layout: home

hero:
  name: Spring AI Cookbook
  text:
  tagline: 从入门到精通，全面掌握 Spring AI 开发
  image:
    src: /spring-ai-integration-diagram-3.svg
    alt: Spring AI Cookbook
  actions:
    - theme: brand
      text: 🔥 项目简介
      link: /0.spring-ai-introduction/
    - theme: alt
      text: 🚀 快速开始
      link: /1.spring-ai-started/
    - theme: alt
      text: ⭐ GitHub
      link: https://github.com/dong4j/spring-ai-cookbook

features:
  - icon: 🚀
    title: 快速入门
    details: 5 分钟快速搭建 Spring AI 项目，零基础也能轻松上手
  - icon: 💡
    title: 核心功能
    details: 深入理解 Prompts、Structured Output、Multimodality 等核心功能
  - icon: 🎯
    title: Model API
    details: 全面掌握 Chat、Embedding、Image、Audio 等各种模型 API
  - icon: 🔧
    title: 高级特性
    details: 学习 RAG、MCP、Vector Database、Tool Calling 等高级特性
  - icon: 🐳
    title: 部署实践
    details: Docker、Testcontainers 等生产级部署和测试实践
  - icon: 📚
    title: 完整示例
    details: 每个模块都包含完整的代码示例和最佳实践指南

---

<div class="home-content">

## 🎯 项目概览

Spring AI Cookbook 是一个全面的 Spring AI 学习教程项目，包含 **20+ 个模块**，从基础入门到高级应用，循序渐进地帮助你掌握 Spring AI 开发。

<div class="stats-grid">
  <div class="stat-card">
    <div class="stat-number">20+</div>
    <div class="stat-label">学习模块</div>
  </div>
  <div class="stat-card">
    <div class="stat-number">100+</div>
    <div class="stat-label">代码示例</div>
  </div>
  <div class="stat-card">
    <div class="stat-number">∞</div>
    <div class="stat-label">学习路径</div>
  </div>
</div>

## 📚 学习路径

### 🟢 入门篇

- **[快速开始](./1.spring-ai-started/)** - 5 分钟搭建你的第一个 Spring AI 项目
- **[Chat Client API](./2.spring-ai-chat-client/)** - 了解 Spring AI 的核心 API 接口

### 🔵 核心功能

- **[Prompts](./3.spring-ai-prompts/)** - 提示词工程与模板管理
- **[Structured Output](./4.spring-ai-structured/)** - 结构化输出转换器
- **[Multimodality](./5.spring-ai-multimodality/)** - 多模态 API 应用

### 🟡 Model API

- **[Chat Model](./7.spring-ai-model-chat/)** - 聊天模型详解
    - [OpenAI 集成](./7.spring-ai-model-chat/7.1.spring-ai-model-chat-openai/)
- **[Embedding Model](./8.spring-ai-model-embedding/)** - 嵌入模型
- **[Image Model](./9.spring-ai-model-image/)** - 图像生成模型
- **[Audio Model](./10.spring-ai-model-audio/)** - 音频处理模型
- **[Tool Calling](./13.spring-ai-model-tool-calling/)** - 工具调用

### 🟣 高级功能

- **[RAG](./15.spring-ai-rag/)** - 检索增强生成
- **[MCP](./14.spring-ai-mcp/)** - 模型上下文协议
- **[Vector Database](./17.spring-ai-vector-database/)** - 向量数据库集成

## 🛠️ 技术栈

<div class="tech-stack">
  <div class="tech-item">
    <strong>Spring Boot</strong>
    <span>3.5.8</span>
  </div>
  <div class="tech-item">
    <strong>Spring AI</strong>
    <span>1.1.0</span>
  </div>
  <div class="tech-item">
    <strong>Java</strong>
    <span>25</span>
  </div>
</div>

## 🚀 快速开始

```bash
# 1. 克隆项目
git clone https://github.com/dong4j/spring-ai-cookbook.git

# 2. 进入子模块目录
cd 1.spring-ai-started

# 3. 运行项目
mvn spring-boot:run
```

## 📖 参考资源

<div class="resources-grid">
  <a href="https://github.com/spring-ai-community/awesome-spring-ai" class="resource-card" target="_blank">
    <div class="resource-icon">⭐</div>
    <div class="resource-title">Awesome Spring AI</div>
    <div class="resource-desc">Spring AI 社区整理的优秀资源集合</div>
  </a>
  <a href="https://github.com/joshlong-attic/2025-05-16-anthropic" class="resource-card" target="_blank">
    <div class="resource-icon">📚</div>
    <div class="resource-title">Anthropic 教程</div>
    <div class="resource-desc">Josh Long 的 Anthropic Spring AI 教程</div>
  </a>
  <a href="https://github.com/GTyingzi/spring-ai-tutorial" class="resource-card" target="_blank">
    <div class="resource-icon">💡</div>
    <div class="resource-title">Spring AI 教程</div>
    <div class="resource-desc">Spring AI 教程项目</div>
  </a>
  <a href="https://github.com/NingNing0111/spring-ai-zh-tutorial" class="resource-card" target="_blank">
    <div class="resource-icon">🇨🇳</div>
    <div class="resource-title">中文教程</div>
    <div class="resource-desc">Spring AI 中文教程项目</div>
  </a>
</div>

## 👤 关于作者

<div class="author-links">
  <div class="author-section">
    <h3>个人站点</h3>
    <ul>
      <li><a href="https://blog.dong4j.site" target="_blank">📝 博客</a></li>
      <li><a href="https://home.dong4j.site" target="_blank">🏠 主页</a></li>
    </ul>
  </div>

  <div class="author-section">
    <h3>个人项目</h3>
    <ul>
      <li><a href="https://plugins.jetbrains.com/plugin/12192-markdown-image-kit" target="_blank">🖼️ MIK 插件</a> - Markdown Image Kit</li>
      <li><a href="https://plugins.jetbrains.com/plugin/28835-ai-javadoc" target="_blank">🤖 AI Javadoc 插件</a> - AI Javadoc Generator</li>
    </ul>
  </div>
</div>

</div>

<style>
.home-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem 1rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1.5rem;
  margin: 2rem 0;
}

.stat-card {
  background: linear-gradient(135deg, var(--vp-c-bg-soft) 0%, var(--vp-c-bg) 100%);
  border: 1px solid var(--vp-c-divider);
  border-radius: 12px;
  padding: 2rem 1.5rem;
  text-align: center;
  transition: all 0.3s ease;
  overflow: visible;
  min-height: 160px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  border-color: var(--vp-c-brand);
}

.stat-number {
  font-size: 3rem;
  font-weight: 700;
  line-height: 1.3;
  background: linear-gradient(135deg, var(--vp-c-brand) 0%, var(--vp-c-brand-light) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 0.5rem;
  display: block;
  overflow: visible;
  white-space: nowrap;
  word-break: keep-all;
}

.stat-label {
  color: var(--vp-c-text-2);
  font-size: 0.9rem;
  line-height: 1.5;
  margin-top: 0.5rem;
}

.tech-stack {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
  margin: 1.5rem 0;
}

.tech-item {
  background: var(--vp-c-bg-soft);
  border: 1px solid var(--vp-c-divider);
  border-radius: 8px;
  padding: 1rem 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  transition: all 0.3s ease;
}

.tech-item:hover {
  border-color: var(--vp-c-brand);
  transform: translateY(-2px);
}

.tech-item strong {
  color: var(--vp-c-text-1);
  font-size: 1rem;
}

.tech-item span {
  color: var(--vp-c-text-2);
  font-size: 0.85rem;
}

.resources-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin: 2rem 0;
}

.resource-card {
  background: var(--vp-c-bg-soft);
  border: 1px solid var(--vp-c-divider);
  border-radius: 12px;
  padding: 1.5rem;
  text-decoration: none;
  color: inherit;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.resource-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  border-color: var(--vp-c-brand);
  text-decoration: none;
}

.resource-icon {
  font-size: 2.5rem;
  line-height: 1;
}

.resource-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--vp-c-text-1);
}

.resource-desc {
  font-size: 0.9rem;
  color: var(--vp-c-text-2);
  line-height: 1.5;
}

.author-links {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
  margin: 2rem 0;
}

.author-section {
  background: var(--vp-c-bg-soft);
  border: 1px solid var(--vp-c-divider);
  border-radius: 12px;
  padding: 1.5rem;
}

.author-section h3 {
  margin: 0 0 1rem 0;
  font-size: 1.2rem;
  color: var(--vp-c-text-1);
  font-weight: 600;
}

.author-section ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.author-section li {
  margin: 0.75rem 0;
}

.author-section a {
  color: var(--vp-c-brand);
  text-decoration: none;
  transition: color 0.2s ease;
  display: inline-block;
}

.author-section a:hover {
  color: var(--vp-c-brand-light);
  text-decoration: underline;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .resources-grid {
    grid-template-columns: 1fr;
  }
  
  .author-links {
    grid-template-columns: 1fr;
  }
}
</style>
