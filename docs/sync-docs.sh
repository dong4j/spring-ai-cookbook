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
IMGS_COUNT=0

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

        # 同步 imgs 目录（完全同步，包括删除）
        local imgs_dir="${module_dir}/imgs"
        local target_imgs_dir="${target_dir}/imgs"

        if [[ -d "${imgs_dir}" ]]; then
            # 使用 rsync 或 cp -r 复制目录
            if command -v rsync &> /dev/null; then
                # 使用 --delete 选项确保完全同步（删除源目录中不存在的文件）
                rsync -aq --delete "${imgs_dir}/" "${target_imgs_dir}/"
            else
                # 如果目标目录存在，先删除再复制
                if [[ -d "${target_imgs_dir}" ]]; then
                    rm -rf "${target_imgs_dir}"
                fi
                cp -r "${imgs_dir}" "${target_dir}/"
            fi

            # 统计图片数量
            local img_count=$(find "${imgs_dir}" -type f \( -iname "*.png" -o -iname "*.jpg" -o -iname "*.jpeg" -o -iname "*.gif" -o -iname "*.webp" -o -iname "*.svg" \) 2>/dev/null | wc -l | tr -d ' ')
            if [[ ${img_count} -gt 0 ]]; then
                echo -e "  ${BLUE}→${NC} 已同步 ${img_count} 个图片文件到 imgs/ 目录"
                ((IMGS_COUNT+=img_count))
            fi
        else
            # 如果源目录不存在 imgs，但目标目录存在，则删除目标目录
            if [[ -d "${target_imgs_dir}" ]]; then
                rm -rf "${target_imgs_dir}"
                echo -e "  ${YELLOW}→${NC} 已删除目标目录中的 imgs/ 目录（源目录中不存在）"
            fi
        fi
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
               [[ "${dir_name}" == "src" ]] || \
               [[ "${dir_name}" == "0.spring-ai-introduction" ]]; then
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

# 函数：清理 docs 目录中已不存在的模块
cleanup_orphaned_modules() {
    echo -e "${YELLOW}正在清理已删除的模块...${NC}"
    local deleted_count=0

    # 遍历 docs 目录下的所有目录
    if [[ -d "${DOCS_DIR}" ]]; then
        for item in "${DOCS_DIR}"/*; do
            if [[ ! -e "${item}" ]]; then
                continue
            fi

            if [[ -d "${item}" ]]; then
                local dir_name=$(basename "${item}")

                # 跳过隐藏目录和特殊目录
                if [[ "${dir_name}" == .* ]] || \
                   [[ "${dir_name}" == "node_modules" ]] || \
                   [[ "${dir_name}" == ".vitepress" ]] || \
                   [[ "${dir_name}" == "0.spring-ai-introduction" ]]; then
                    continue
                fi

                # 检查是否是模块目录（以数字开头）
                if [[ "${dir_name}" =~ ^[0-9] ]]; then
                    local relative_path="${dir_name}"
                    local source_module="${ROOT_DIR}/${relative_path}"

                    # 如果源模块不存在，删除 docs 中的对应目录
                    if [[ ! -d "${source_module}" ]]; then
                        rm -rf "${item}"
                        echo -e "  ${RED}✗${NC} 已删除不存在的模块: ${relative_path}"
                        ((deleted_count++))
                    else
                        # 递归检查子模块
                        cleanup_submodules "${item}" "${relative_path}"
                    fi
                fi
            fi
        done
    fi

    if [[ ${deleted_count} -gt 0 ]]; then
        echo -e "已清理: ${RED}${deleted_count}${NC} 个不存在的模块"
    fi
}

# 函数：递归清理子模块
cleanup_submodules() {
    local docs_subdir="$1"
    local relative_path="$2"

    if [[ ! -d "${docs_subdir}" ]]; then
        return
    fi

    for item in "${docs_subdir}"/*; do
        if [[ ! -e "${item}" ]]; then
            continue
        fi

        if [[ -d "${item}" ]]; then
            local dir_name=$(basename "${item}")

            # 跳过 imgs 等特殊目录
            if [[ "${dir_name}" == "imgs" ]]; then
                continue
            fi

            # 检查是否是子模块目录（以数字开头）
            if [[ "${dir_name}" =~ ^[0-9] ]]; then
                local sub_relative_path="${relative_path}/${dir_name}"
                local source_submodule="${ROOT_DIR}/${sub_relative_path}"

                # 如果源子模块不存在，删除 docs 中的对应目录
                if [[ ! -d "${source_submodule}" ]]; then
                    rm -rf "${item}"
                    echo -e "  ${RED}✗${NC} 已删除不存在的子模块: ${sub_relative_path}"
                else
                    # 递归检查更深层的子模块
                    cleanup_submodules "${item}" "${sub_relative_path}"
                fi
            fi
        fi
    done
}

# 开始处理
echo -e "${YELLOW}正在扫描模块目录...${NC}"
echo ""

find_modules "${ROOT_DIR}"

echo ""
cleanup_orphaned_modules

echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}同步完成！${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "已同步: ${GREEN}${SYNCED_COUNT}${NC} 个模块"
if [[ ${IMGS_COUNT} -gt 0 ]]; then
    echo -e "已同步: ${GREEN}${IMGS_COUNT}${NC} 个图片文件"
fi
if [[ ${SKIPPED_COUNT} -gt 0 ]]; then
    echo -e "已跳过: ${YELLOW}${SKIPPED_COUNT}${NC} 个模块（无 README.md）"
fi
echo ""
echo -e "${YELLOW}提示:${NC}"
echo -e "  - 运行 ${BLUE}npm run dev${NC} 启动开发服务器"
echo -e "  - 运行 ${BLUE}npm run build${NC} 构建文档"
echo -e "  - 运行 ${BLUE}npm run sync${NC} 重新同步文档"
echo ""
