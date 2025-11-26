#!/bin/bash

# 图片转换为 WebP 格式的脚本
# 遍历所有模块目录，将 imgs/ 目录下的非 WebP 图片转换为 WebP，并更新 README.md 中的图片链接

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

echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}Spring AI Cookbook 图片转换工具${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# WebP 转换函数
webp() {
  local input="$1"
  local output="$2"
  
  if [ -z "$input" ]; then
    echo "Usage: webp <input_file> [output_file]"
    return 1
  fi

  # 如果没有传第二个参数，自动替换扩展名为 .webp
  if [ -z "$output" ]; then
    local filename="${input%.*}"   # 去掉扩展名
    output="${filename}.webp"
  fi

  # 检查是否已安装 exiftool
  if command -v exiftool &> /dev/null; then
    # 删除元数据
    exiftool -overwrite_original -all= "$input" > /dev/null 2>&1
  fi

  # 检查是否已安装 cwebp
  if ! command -v cwebp &> /dev/null; then
    echo -e "${RED}错误: 未安装 cwebp${NC}"
    echo -e "${YELLOW}  macOS: brew install webp${NC}"
    echo -e "${YELLOW}  Linux: sudo apt-get install webp 或 sudo yum install libwebp-tools${NC}"
    return 1
  fi

  # 转为 WebP
  cwebp -q 50 "$input" -o "$output" > /dev/null 2>&1
  
  if [ $? -eq 0 ] && [ -f "$output" ]; then
      # 验证输出文件大小（确保转换成功）
      if [ -s "$output" ]; then
        echo -e "  ${GREEN}✓${NC} convert '$(basename "$input")' to '$(basename "$output")'"
        # 删除源文件
        if rm -f "$input" 2>/dev/null; then
          echo -e "  ${BLUE}→${NC} 已删除源文件: '$(basename "$input")'"
          DELETED_COUNT=$((DELETED_COUNT + 1))
        fi
        return 0
    else
      echo -e "  ${RED}✗${NC} 转换失败: 输出文件为空"
      rm -f "$output"
      return 1
    fi
  else
    echo -e "  ${RED}✗${NC} failed to convert '$(basename "$input")'"
    rm -f "$output"
    return 1
  fi
}

# 统计变量
CONVERTED_COUNT=0
UPDATED_MD_COUNT=0
DELETED_COUNT=0

# 函数：更新 README.md 中的图片链接
update_image_links() {
  local readme_file="$1"
  local img_filename="$2"  # 完整的图片文件名，如 "image.png"
  local old_ext="$3"       # 旧扩展名，如 ".png"
  
  if [ ! -f "$readme_file" ]; then
    return
  fi

  # 构建旧的和新的图片文件名
  # img_filename 已经是完整的文件名（包含扩展名），如 "image.png"
  # 我们需要替换为 "image.webp"
  local img_base="${img_filename%.*}"  # 去掉最后一个扩展名，得到 "image"
  local old_pattern="${img_base}${old_ext}"  # "image.png"
  local new_pattern="${img_base}.webp"       # "image.webp"
  local changed=0
  
  # 检查文件是否包含需要替换的内容
  if grep -q "$old_pattern" "$readme_file" 2>/dev/null; then
    # 使用 perl 进行替换（跨平台兼容）
    # 直接替换文件名：image.png -> image.webp
    if command -v perl &> /dev/null; then
      # 转义点号用于 perl（文件名中的点需要转义）
      local escaped_img_base=$(printf '%s\n' "$img_base" | perl -pe 's/\./\\./g')
      local escaped_old_ext=$(printf '%s\n' "$old_ext" | perl -pe 's/\./\\./g')
      
      # 创建临时文件（跨平台兼容）
      local temp_file=$(mktemp 2>/dev/null || mktemp -t tmp 2>/dev/null)
      if [ -z "$temp_file" ]; then
        temp_file="${readme_file}.tmp"
      fi
      
      # 使用 perl 替换：直接替换文件名
      # 模式：匹配 ${img_base}.${old_ext} 替换为 ${img_base}.webp
      perl -pe "s|${escaped_img_base}${escaped_old_ext}|${escaped_img_base}.webp|g" "$readme_file" > "$temp_file" 2>/dev/null
      
      # 检查文件是否被修改
      if ! cmp -s "$readme_file" "$temp_file" 2>/dev/null; then
        mv "$temp_file" "$readme_file"
        changed=1
      else
        rm -f "$temp_file"
      fi
    else
      # 使用 sed 进行替换（跨平台兼容）
      # 转义点号用于 sed
      local escaped_img_base=$(printf '%s\n' "$img_base" | sed 's/\./\\./g')
      local escaped_old_ext=$(printf '%s\n' "$old_ext" | sed 's/\./\\./g')
      
      # 创建临时文件（跨平台兼容）
      local temp_file=$(mktemp 2>/dev/null || mktemp -t tmp 2>/dev/null)
      if [ -z "$temp_file" ]; then
        temp_file="${readme_file}.tmp"
      fi
      
      # sed 替换：直接替换文件名
      # macOS 和 Linux 的 sed 行为略有不同，使用通用方式
      if sed "s|${escaped_img_base}${escaped_old_ext}|${escaped_img_base}\\.webp|g" "$readme_file" > "$temp_file" 2>/dev/null; then
        # 检查文件是否被修改
        if ! cmp -s "$readme_file" "$temp_file" 2>/dev/null; then
          mv "$temp_file" "$readme_file"
          changed=1
        else
          rm -f "$temp_file"
        fi
      else
        rm -f "$temp_file"
      fi
    fi
    
    # 如果文件被修改，统计
    if [ $changed -eq 1 ]; then
      echo -e "  ${BLUE}→${NC} 已更新 README.md 中的图片链接: ${old_pattern} -> ${new_pattern}"
      ((UPDATED_MD_COUNT++))
    fi
  fi
}

# 函数：更新模块 docs 目录下所有 .md 文件的图片链接
update_docs_markdown_links() {
  local docs_dir="$1"
  local img_filename="$2"
  local old_ext="$3"

  if [[ ! -d "$docs_dir" ]]; then
    return
  fi

  if compgen -G "${docs_dir}/*.md" > /dev/null; then
    for doc_file in "${docs_dir}"/*.md; do
      [[ -f "$doc_file" ]] || continue
      update_image_links "$doc_file" "$img_filename" "$old_ext"
    done
  fi
}

# 函数：处理单个模块的图片转换
process_module_images() {
  local module_dir="$1"
  local relative_path="${module_dir#${ROOT_DIR}/}"
  local imgs_dir="${module_dir}/imgs"
  local readme_file="${module_dir}/README.md"
  local docs_dir="${module_dir}/docs"

  # 跳过 docs 目录本身和隐藏目录
  if [[ "${relative_path}" == "docs"* ]] || [[ "${relative_path}" == .* ]]; then
    return
  fi

  # 检查是否是模块目录（包含数字开头的目录名）
  local dir_name=$(basename "${module_dir}")
  if [[ ! "${dir_name}" =~ ^[0-9] ]]; then
    return
  fi

  # 检查是否存在 imgs 目录
  if [ ! -d "$imgs_dir" ]; then
    return
  fi

  echo -e "${YELLOW}处理模块: ${relative_path}${NC}"

  # 支持的图片格式（排除 .webp 和 .svg）
  local image_extensions=("png" "jpg" "jpeg" "PNG" "JPG" "JPEG")

  # 遍历 imgs 目录下的所有图片文件
  local found_images=0
  for ext in "${image_extensions[@]}"; do
    while IFS= read -r -d '' image_file; do
      found_images=1
      local img_name="${image_file%.*}"
      local img_base=$(basename "$img_name")
      local webp_file="${img_name}.webp"

      # 如果对应的 webp 文件已存在，跳过
      if [ -f "$webp_file" ]; then
        echo -e "  ${BLUE}⊘${NC} 跳过（已存在）: $(basename "$image_file")"
        continue
      fi

      # 转换为 WebP
      if webp "$image_file" "$webp_file"; then
        ((CONVERTED_COUNT++))
        local img_filename=$(basename "$image_file")
        
        # 更新 README.md 中的图片链接
        if [ -f "$readme_file" ]; then
          update_image_links "$readme_file" "$img_filename" ".${ext}"
        fi
        # 更新模块 docs 目录中的文档
        update_docs_markdown_links "$docs_dir" "$img_filename" ".${ext}"
      fi
    done < <(find "$imgs_dir" -type f -iname "*.${ext}" -print0 2>/dev/null)
  done

  # 如果没有找到图片
  if [ $found_images -eq 0 ]; then
    echo -e "  ${BLUE}→${NC} 未找到需要转换的图片"
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
        process_module_images "${item}"
      fi

      # 递归处理子目录（支持多层级）
      find_modules "${item}"
    fi
  done
}

# 检查依赖
if ! command -v cwebp &> /dev/null; then
  echo -e "${RED}错误: 未安装 cwebp${NC}"
  echo -e "${YELLOW}  macOS: brew install webp${NC}"
  echo -e "${YELLOW}  Linux: sudo apt-get install webp 或 sudo yum install libwebp-tools${NC}"
  exit 1
fi

# 开始处理
echo -e "${YELLOW}正在扫描模块目录...${NC}"
echo ""

find_modules "${ROOT_DIR}"

echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}转换完成！${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "已转换: ${GREEN}${CONVERTED_COUNT}${NC} 个图片文件为 WebP 格式"
if [ $DELETED_COUNT -gt 0 ]; then
  echo -e "已删除: ${GREEN}${DELETED_COUNT}${NC} 个源图片文件"
fi
if [ $UPDATED_MD_COUNT -gt 0 ]; then
  echo -e "已更新: ${GREEN}${UPDATED_MD_COUNT}${NC} 个 README.md 文件中的图片链接"
fi
echo ""

