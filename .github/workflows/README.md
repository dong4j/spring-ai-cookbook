# GitHub Actions 部署配置说明

## 概述

此 workflow 会在代码推送到 `main` 或 `master` 分支时，自动构建文档并部署到 ECS 服务器。

## 配置步骤

### 1. 生成 SSH 密钥对（如果还没有）

在本地执行：

```bash
ssh-keygen -t rsa -b 4096 -C "github-actions-deploy" -f ~/.ssh/github_actions_deploy
```

这会生成两个文件：

- `~/.ssh/github_actions_deploy` (私钥)
- `~/.ssh/github_actions_deploy.pub` (公钥)

### 2. 将公钥添加到 ECS 服务器

将公钥内容添加到 ECS 服务器的 `~/.ssh/authorized_keys` 文件中：

```bash
# 在本地执行，将公钥复制到 ECS 服务器
ssh-copy-id -i ~/.ssh/github_actions_deploy.pub user@your-ecs-ip

# 或者手动添加
cat ~/.ssh/github_actions_deploy.pub | ssh user@your-ecs-ip "mkdir -p ~/.ssh && cat >> ~/.ssh/authorized_keys"
```

### 3. 配置 GitHub Secrets

在 GitHub 仓库中，进入 **Settings** → **Secrets and variables** → **Actions**，添加以下 Secrets：

| Secret 名称             | 说明                                            | 示例值                                   |
|-----------------------|-----------------------------------------------|---------------------------------------|
| `ECS_SSH_PRIVATE_KEY` | SSH 私钥内容（整个文件内容，包括 `-----BEGIN` 和 `-----END`） | 从 `~/.ssh/github_actions_deploy` 文件复制 |
| `ECS_HOST`            | ECS 服务器 IP 地址或域名                              | `47.21.21.21` 或 `docs.example.com`    |
| `ECS_USER`            | SSH 登录用户名                                     | `root` 或 `ubuntu`                     |
| `ECS_DEPLOY_PATH`     | 部署路径（文档将部署到此目录）                               | `/var/www/html` 或 `/home/user/docs`   |

### 4. 获取私钥内容

```bash
# 在本地执行，复制私钥内容
cat ~/.ssh/github_actions_deploy
```

将输出的完整内容（包括 `-----BEGIN RSA PRIVATE KEY-----` 和 `-----END RSA PRIVATE KEY-----`）复制到 GitHub Secrets 的 `ECS_SSH_PRIVATE_KEY` 中。

## 工作流程

### 触发方式

**默认配置：仅手动触发（推荐）**

Workflow 默认配置为**仅手动触发**，不会在每次提交时自动执行。这样可以：

- 节省 GitHub Actions 运行时间
- 避免不必要的部署
- 完全控制部署时机

**手动触发步骤**：

1. 进入 GitHub 仓库页面
2. 点击 **Actions** 标签
3. 选择 **Deploy Docs to ECS** workflow
4. 点击 **Run workflow** 按钮
5. 选择分支（通常是 `main` 或 `master`）
6. （可选）输入部署说明
7. 点击 **Run workflow** 开始执行

**自动触发（条件触发）**：

如果启用了自动触发（`push` 已取消注释），workflow 的触发和执行分为两个阶段：

### 触发阶段（workflow 会被触发，但可能不执行）

以下条件满足时，workflow 会被触发：

- 推送到 `main` 或 `master` 分支
- **任意文件变更**都会触发（不限制文件路径）

### 执行阶段（workflow 真正执行部署）

即使 workflow 被触发了，还需要满足以下条件才会**真正执行部署**：

- **提交信息中包含 `[deploy-docs]` 关键词**

### 完整流程示例

```bash
# 场景 1：修改了 docs/index.md，提交信息包含 [deploy-docs]
# ✅ workflow 会被触发 ✅ 会执行部署
git add docs/index.md
git commit -m "更新首页 [deploy-docs]"
git push origin main

# 场景 2：修改了 docs/index.md，提交信息不包含 [deploy-docs]
# ✅ workflow 会被触发 ❌ 但不会执行部署（会被跳过）
git add docs/index.md
git commit -m "更新首页"
git push origin main

# 场景 3：只修改了代码文件（不在 docs/ 目录下），但提交信息包含 [deploy-docs]
# ✅ workflow 会被触发 ✅ 会执行部署（因为任意文件变更都会触发）
git add src/main/java/App.java
git commit -m "修复bug [deploy-docs]"
git push origin main

# 场景 4：修改了代码文件，但提交信息不包含 [deploy-docs]
# ✅ workflow 会被触发 ❌ 但不会执行部署（会被跳过）
git add src/main/java/App.java
git commit -m "修复bug"
git push origin main
```

**触发示例**：

```bash
# ✅ 会触发部署
git commit -m "更新文档内容 [deploy-docs]"
git commit -m "[deploy-docs] 修复文档错误"
git commit -m "docs: 更新 README [deploy-docs]"

# ❌ 不会触发部署（缺少 [deploy-docs]）
git commit -m "更新文档内容"
git commit -m "修复代码bug"
```

**工作原理**：

- 如果提交信息中**包含** `[deploy-docs]`，workflow 会执行部署
- 如果提交信息中**不包含** `[deploy-docs]`，即使 `docs/` 目录有变更，workflow 也不会执行
- 手动触发（workflow_dispatch）不受此限制，随时可以执行

### 构建步骤

1. **同步文档**：执行 `sync-docs.sh` 脚本，同步子模块的 README.md 和图片资源
2. **设置环境**：设置 Node.js 环境
3. **安装依赖**：执行 `npm install`
4. **构建文档**：执行 `npm run build`

### 部署步骤

1. **配置 SSH**：设置 SSH 密钥用于免密登录
2. **验证构建**：检查构建输出目录是否存在
3. **同步文件**：使用 rsync 同步文件到 ECS 服务器
    - `--delete` 选项确保目标目录与源目录完全一致（会删除目标目录中不存在的文件）

## 注意事项

- 确保 ECS 服务器上的部署目录有写入权限
- 建议使用非 root 用户，并配置适当的权限
- 如果使用 Nginx，确保配置指向正确的部署路径
- 首次部署前，建议在 ECS 服务器上手动创建部署目录

## 故障排查

### SSH 连接失败

1. 检查 SSH 密钥是否正确配置
2. 检查 ECS 服务器的防火墙设置
3. 检查 `known_hosts` 是否正确添加

### 权限错误

1. 检查部署目录的权限
2. 确保 SSH 用户有写入权限

### rsync 同步失败

1. 检查部署路径是否存在
2. 检查磁盘空间是否充足
3. 检查网络连接

## 安全建议

- 定期轮换 SSH 密钥
- 使用最小权限原则
- 考虑使用 SSH 密钥密码保护（需要额外配置）
- 定期检查 GitHub Actions 日志

## Nginx 配置

项目根目录下的 `.github/workflows/nginx.conf` 包含了优化的 Nginx 配置文件，包括：

- HTTP 自动跳转到 HTTPS
- SSL/TLS 安全配置
- 安全头设置
- Gzip 压缩
- 静态资源缓存策略
- VitePress SPA 路由支持

### 使用说明

1. 将配置文件复制到 ECS 服务器：
   ```bash
   sudo cp .github/workflows/nginx.conf /etc/nginx/sites-available/spring-ai-cookbook
   ```

2. 创建符号链接：
   ```bash
   sudo ln -s /etc/nginx/sites-available/spring-ai-cookbook /etc/nginx/sites-enabled/
   ```

3. 测试配置：
   ```bash
   sudo nginx -t
   ```

4. 重载 Nginx：
   ```bash
   sudo systemctl reload nginx
   ```

### 注意事项

- 确保 SSL 证书路径正确：`/etc/nginx/encrypt/fullchain.pem` 和 `/etc/nginx/encrypt/privkey.pem`
- 如果使用 Let's Encrypt，证书路径可能不同
- 根据实际情况调整 `root` 路径：`/var/www/spring-ai-cookbook/dist`
- 如果安装了 Brotli 模块，可以取消注释 Brotli 压缩配置

