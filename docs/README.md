# Spring AI Cookbook 文档

基于 VitePress 的 Spring AI 教程文档站点。

## 快速开始

### 安装依赖

```bash
npm install
```

### 开发模式

```bash
npm run dev
```

访问 http://localhost:5173 查看文档。

### 构建文档

```bash
npm run build
```

构建后的文件在 `.vitepress/dist` 目录。

### 预览构建结果

```bash
npm run preview
```

## 同步文档

当子模块的 README.md 更新后，需要同步到 docs 目录：

```bash
npm run sync
# 或者
bash sync-docs.sh
```

## 文档结构

- `index.md` - 首页
- `*.spring-ai-*/index.md` - 各模块文档（从子模块 README.md 同步）

## 多层级模块支持

脚本支持多层级子模块结构，例如：

```
7.spring-ai-model-chat/
  ├── README.md
  └── 7.1.spring-ai-model-chat-openai/
      └── README.md
```

会自动同步为：

```
docs/
  └── 7.spring-ai-model-chat/
      ├── index.md
      └── 7.1.spring-ai-model-chat-openai/
          └── index.md
```

## 部署

文档部署到 `springai.dong4j.site` 域名。

### 部署步骤

1. 构建文档：`npm run docs:build`
2. 将 `docs/.vitepress/dist` 目录内容部署到服务器
3. 配置 Nginx 或其他 Web 服务器指向该目录

## 配置说明

- `.vitepress/config.js` - VitePress 配置文件
- `sync-docs.sh` - 文档同步脚本
- `generate-sidebar.js` - 侧边栏生成脚本（可选）

