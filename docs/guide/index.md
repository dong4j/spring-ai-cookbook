---
published: 2025.12.01
---

# 引言

Spring AI 教程项目，包含多个子模块，每个子模块演示 Spring AI 的不同功能特性。

## 子模块列表

- ✅ [[1.spring-ai-started/index|快速搭建 Spring AI 项目]]
- ✅ [[2.spring-ai-chat-client/index|Chat Client API]]
- ⭕ [[3.spring-ai-prompts/index|提示词管理]]
- ⭕ [[4.spring-ai-structured/index|结构化输出]]
- ⭕ [[5.spring-ai-multimodality/index|多模态 API]]
- ⭕ [[6.spring-ai-model/index|模型 API]]
- ⭕ [[7.spring-ai-model-chat/index|聊天模型]]
- ⭕ [[8.spring-ai-model-embedding/index|嵌入模型]]
- ⭕ [[9.spring-ai-model-image/index|图像模型]]
- ⭕ [[10.spring-ai-model-audio/index|音频模型]]
- ⭕ [[11.spring-ai-model-moderation/index|内容审核]]
- ⭕ [[12.spring-ai-model-memory/index|聊天记忆]]
- ⭕ [[13.spring-ai-model-tool-calling/index|工具调用]]
- ⭕ [[14.spring-ai-mcp/index|模型上下文协议]]
- ⭕ [[15.spring-ai-rag/index|检索增强生成]]
- ⭕ [[16.spring-ai-model-evaluation/index|模型评估]]
- ⭕ [[17.spring-ai-vector-database/index|向量数据库]]
- ⭕ [[18.spring-ai-observability/index|可观测性]]
- ⭕ [[19.spring-ai-docker/index|编排]]
- ⭕ [[20.spring-ai-testcontainer/index|测试容器]]

## 项目结构

```
spring-ai-cookbook/
├── 1.spring-ai-started/                # 快速搭建 Spring AI 项目
├── 2.spring-ai-chat-client/            # Chat Client API
├── 3.spring-ai-prompts/                # 提示词管理
├── 4.spring-ai-structured/             # 结构化输出
├── 5.spring-ai-multimodality/          # 多模态 API
├── 6.spring-ai-model/                  # 模型 API
├── 7.spring-ai-model-chat/             # 聊天模型
├── 8.spring-ai-model-embedding/        # 嵌入模型
├── 9.spring-ai-model-image/            # 图像模型
├── 10.spring-ai-model-audio/           # 音频模型
├── 11.spring-ai-model-moderation/      # 内容审核
├── 12.spring-ai-model-memory/          # 聊天记忆
├── 13.spring-ai-model-tool-calling/    # 工具调用
├── 14.spring-ai-mcp/                   # 模型上下文协议
├── 15.spring-ai-rag/                   # 检索增强生成
├── 16.spring-ai-model-evaluation/      # 模型评估
├── 17.spring-ai-vector-database/       # 向量数据库
├── 18.spring-ai-observability/         # 可观测性
├── 19.spring-ai-docker/                # 编排
├── 20.spring-ai-testcontainer/         # 测试容器
└── docs
    ├── .vitepress/                     # VitePress 配置目录
    ├── guide/                          # 快速指引
    ├── action/                         # 实战
    ├── about/                          # 关于
    ├── convert-images-to-webp.sh       # 图片转 WebP 格式脚本
    ├── deploy.sh                       # 部署脚本
    └── sync-docs.sh                    # 文档同步脚本
```

## 技术栈

- Spring Boot 3.5.8
- Spring AI 1.1.0
- Java 25

## 快速开始

1. 克隆项目
2. 进入对应的子模块目录
3. 查看子模块的 README.md 了解详细使用说明
4. 或者按照下面的方式本地部署教程文档服务:

    ```bash [bash]
    cd docs
    npm run sync && npm run build && npm run preview 
    ```

## 参考资源

以下是一些优秀的 Spring AI 学习资源和参考项目：

- [Awesome Spring AI](https://github.com/spring-ai-community/awesome-spring-ai) - Spring AI 社区整理的优秀资源、工具、教程和项目集合
- [Anthropic Spring AI Tutorial](https://github.com/joshlong-attic/2025-05-16-anthropic) - Josh Long 的 Anthropic Spring AI 教程
- [Spring AI Tutorial](https://github.com/GTyingzi/spring-ai-tutorial) - Spring AI 教程项目
- [Spring AI 中文教程](https://github.com/NingNing0111/spring-ai-zh-tutorial) - Spring AI 中文教程项目

## 许可证

本项目采用 [MIT License](https://github.com/dong4j/spring-ai-cookbook/blob/main/LICENSE) 许可证。

Copyright (c) 2025 Spring AI Cookbook Contributors

