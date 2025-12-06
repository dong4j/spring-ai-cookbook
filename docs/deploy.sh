#!/bin/bash

# 部署脚本：按顺序执行图片转换和文档同步

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}Spring AI Cookbook 部署工具${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# 步骤 1: 图片转换为 WebP
echo -e "${YELLOW}[步骤 1/2] 图片转换为 WebP 格式...${NC}"
echo ""

if [ ! -f "${SCRIPT_DIR}/convert-images-to-webp.sh" ]; then
  echo -e "${RED}错误: 找不到 convert-images-to-webp.sh 脚本${NC}"
  exit 1
fi

# 添加执行权限
chmod +x "${SCRIPT_DIR}/convert-images-to-webp.sh"

# 执行图片转换脚本
bash "${SCRIPT_DIR}/convert-images-to-webp.sh"

if [ $? -ne 0 ]; then
  echo -e "${RED}图片转换失败，终止部署${NC}"
  exit 1
fi

echo ""
echo -e "${GREEN}✓${NC} 图片转换完成"
echo ""

# 步骤 2: 同步文档
echo -e "${YELLOW}[步骤 2/2] 同步文档到 docs 目录...${NC}"
echo ""

if [ ! -f "${SCRIPT_DIR}/sync-docs.sh" ]; then
  echo -e "${RED}错误: 找不到 sync-docs.sh 脚本${NC}"
  exit 1
fi

# 添加执行权限
chmod +x "${SCRIPT_DIR}/sync-docs.sh"

# 执行文档同步脚本
bash "${SCRIPT_DIR}/sync-docs.sh"

if [ $? -ne 0 ]; then
  echo -e "${RED}文档同步失败${NC}"
  exit 1
fi

echo ""
echo -e "${GREEN}✓${NC} 文档同步完成"
echo ""

# 完成
echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}部署完成！${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "${YELLOW}提示:${NC}"
echo -e "  - 运行 ${BLUE}pnpm run dev${NC} 启动开发服务器"
echo -e "  - 运行 ${BLUE}pnpm run build${NC} 构建文档"
echo ""

