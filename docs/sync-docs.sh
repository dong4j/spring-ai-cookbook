#!/bin/bash

# 同步子模块 README.md 到 docs 目录的脚本
# 支持多层级子模块结构（如 7.spring-ai-model-chat/7.1.spring-ai-model-chat-openai）

# 不使用 set -e，以便更好地处理错误
set +e

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 项目根目录（docs 的父目录）
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
# docs 目录（当前脚本所在目录）
DOCS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}Spring AI Cookbook 文档同步工具${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# 创建 docs 目录（如果不存在）
mkdir -p "${DOCS_DIR}"

# 统计变量
SYNCED_COUNT=0
SKIPPED_COUNT=0

# 函数：处理单个模块目录
process_module() {
    local module_dir="$1"
    local relative_path="${module_dir#${ROOT_DIR}/}"
    local readme_file="${module_dir}/README.md"
    
    # 跳过 docs 目录本身和隐藏目录
    if [[ "${relative_path}" == "docs"* ]] || [[ "${relative_path}" == .* ]]; then
        return
    fi
    
    # 检查是否是模块目录（包含数字开头的目录名）
    local dir_name=$(basename "${module_dir}")
    if [[ ! "${dir_name}" =~ ^[0-9] ]]; then
        return
    fi
    
    # 如果存在 README.md
    if [[ -f "${readme_file}" ]]; then
        # 计算目标路径
        # 将相对路径转换为 docs 目录下的路径
        local target_dir="${DOCS_DIR}/${relative_path}"
        local target_file="${target_dir}/index.md"
        
        # 创建目标目录
        mkdir -p "${target_dir}"
        
        # 复制 README.md 到 index.md
        cp "${readme_file}" "${target_file}"
        
        echo -e "${GREEN}✓${NC} ${relative_path}/README.md -> docs/${relative_path}/index.md"
        ((SYNCED_COUNT++))
    else
        echo -e "${YELLOW}⚠${NC} ${relative_path}/README.md 不存在，跳过"
        ((SKIPPED_COUNT++))
    fi
}

# 函数：递归查找所有模块目录
find_modules() {
    local dir="$1"
    
    # 检查目录是否存在
    if [[ ! -d "${dir}" ]]; then
        return
    fi
    
    # 遍历当前目录
    for item in "${dir}"/*; do
        # 检查文件是否存在（处理通配符扩展失败的情况）
        if [[ ! -e "${item}" ]]; then
            continue
        fi
        
        if [[ -d "${item}" ]]; then
            local dir_name=$(basename "${item}")
            
            # 跳过隐藏目录和特殊目录
            if [[ "${dir_name}" == .* ]] || \
               [[ "${dir_name}" == "docs" ]] || \
               [[ "${dir_name}" == "node_modules" ]] || \
               [[ "${dir_name}" == "target" ]] || \
               [[ "${dir_name}" == ".git" ]] || \
               [[ "${dir_name}" == "src" ]]; then
                continue
            fi
            
            # 如果是模块目录（以数字开头），处理它
            if [[ "${dir_name}" =~ ^[0-9] ]]; then
                process_module "${item}"
            fi
            
            # 递归处理子目录（支持多层级）
            find_modules "${item}"
        fi
    done
}

# 开始处理
echo -e "${YELLOW}正在扫描模块目录...${NC}"
echo ""

find_modules "${ROOT_DIR}"

echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}同步完成！${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "已同步: ${GREEN}${SYNCED_COUNT}${NC} 个模块"
if [[ ${SKIPPED_COUNT} -gt 0 ]]; then
    echo -e "已跳过: ${YELLOW}${SKIPPED_COUNT}${NC} 个模块（无 README.md）"
fi
echo ""
echo -e "${YELLOW}提示:${NC}"
echo -e "  - 运行 ${BLUE}npm run dev${NC} 启动开发服务器"
echo -e "  - 运行 ${BLUE}npm run build${NC} 构建文档"
echo -e "  - 运行 ${BLUE}npm run sync${NC} 重新同步文档"
echo ""
