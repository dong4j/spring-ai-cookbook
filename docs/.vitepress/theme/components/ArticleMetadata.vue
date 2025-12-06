<script lang="ts" setup>
import { useData } from 'vitepress'
import { computed, ref, onMounted, nextTick } from 'vue'
import { inBrowser } from 'vitepress'
import { countWord, fetchBusuanzi } from '../utils/functions'

const { page } = useData()
const date = computed(() => {
  const d = new Date(page.value.lastUpdated!)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}.${month}.${day}`
})

// 格式化 published 日期
const publishedDate = computed(() => {
  const published = page.value.frontmatter?.published
  if (!published) return null
  
  try {
    // 支持多种格式：2025.12.06 或 2025-12-06 或 2025/12/06
    let dateStr = published.toString().trim()
    
    // 统一替换分隔符为 -
    dateStr = dateStr.replace(/[.\/]/g, '-')
    
    // 解析日期
    const parts = dateStr.split('-')
    if (parts.length === 3) {
      const year = parts[0]
      const month = parts[1].padStart(2, '0')
      const day = parts[2].padStart(2, '0')
      return `${year}.${month}.${day}`
    }
    
    // 如果格式不对，尝试直接解析
    const d = new Date(dateStr)
    if (!isNaN(d.getTime())) {
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    }
  } catch (e) {
    console.warn('Failed to parse published date:', published, e)
  }
  
  return null
})

const wordCount = ref(0)
const imageCount = ref(0)

const wordTime = computed(() => {
    return ((wordCount.value / 275) * 60)
})

const imageTime = computed(() => {
    const n = imageCount.value
    if (imageCount.value <= 10) {
        // 等差数列求和
        return n * 13 + (n * (n - 1)) / 2
    }
    return 175 + (n - 10) * 3
})

// 阅读时间
const readTime = computed(() => {
    return Math.ceil((wordTime.value + imageTime.value) / 60)
})

function analyze() {
    document.querySelectorAll('.meta-des').forEach(v => v.remove())
    const docDomContainer = window.document.querySelector('#VPContent')
    const imgs = docDomContainer?.querySelectorAll<HTMLImageElement>(
        '.content-container .main img'
    )
    imageCount.value = imgs?.length || 0
    const words = docDomContainer?.querySelector('.content-container .main')?.textContent || ''
    wordCount.value = countWord(words)
}

onMounted(() => {
    if (!inBrowser) return
    
    // 初始化时执行一次
    analyze()
    
    // 等待 DOM 渲染完成后，检查并加载 busuanzi 数据
    nextTick(() => {
      const pvElement = document.getElementById('busuanzi_page_pv')
      if (pvElement) {
        // 如果元素存在但内容是默认值，则手动触发加载
        if (pvElement.textContent === '--' || !pvElement.textContent || pvElement.textContent.trim() === '') {
          // 延迟执行，确保页面完全加载
          setTimeout(() => {
            fetchBusuanzi({ updatePagePv: true })
          }, 500)
        }
      } else {
        // 如果元素不存在，延迟重试
        setTimeout(() => {
          fetchBusuanzi({ updatePagePv: true })
        }, 1000)
      }
    })
})
</script>


<template>
    <div class="article-metadata">
        <template v-if="publishedDate">
            <div class="meta-item">
                <svg class="meta-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <rect x="3" y="4" width="18" height="18" rx="2" ry="2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M16 2V6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M8 2V6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M3 10H21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <span class="meta-label">发表于:</span>
                <span class="meta-value">{{ publishedDate }}</span>
            </div>
            <span class="meta-separator">·</span>
        </template>
        <div class="meta-item">
            <svg class="meta-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M12 6V12L16 14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span class="meta-label">更新于:</span>
            <span class="meta-value">{{ date }}</span>
        </div>
        <span class="meta-separator">·</span>
        <div class="meta-item">
            <svg class="meta-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M14 2H6C5.46957 2 4.96086 2.21071 4.58579 2.58579C4.21071 2.96086 4 3.46957 4 4V20C4 20.5304 4.21071 21.0391 4.58579 21.4142C4.96086 21.7893 5.46957 22 6 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V8L14 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M14 2V8H20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M16 13H8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M16 17H8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span class="meta-label">字数:</span>
            <span class="meta-value">{{ wordCount }} 字</span>
        </div>
        <span class="meta-separator">·</span>
        <div class="meta-item">
            <svg class="meta-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M12 6V12L16 14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span class="meta-label">时长:</span>
            <span class="meta-value">{{ readTime }} 分钟</span>
        </div>
        <span class="meta-separator">·</span>
        <div class="meta-item">
            <svg class="meta-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M1 12S5 4 12 4S23 12 23 12S19 20 12 20S1 12 1 12Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M12 15C13.6569 15 15 13.6569 15 12C15 10.3431 13.6569 9 12 9C10.3431 9 9 10.3431 9 12C9 13.6569 10.3431 15 12 15Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span class="meta-label">阅读量:</span>
            <span class="meta-value" id="busuanzi_page_pv">--</span>
            <span class="meta-unit">次</span>
        </div>
    </div>
</template>

<style scoped>
.article-metadata {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
  margin: 16px 0;
  padding: 12px 0;
  color: var(--vp-c-text-2);
  font-size: 14px;
  line-height: 1.6;
  border-top: 1px solid var(--vp-c-divider);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.meta-icon {
  width: 16px;
  height: 16px;
  color: var(--vp-c-text-2);
  flex-shrink: 0;
  vertical-align: middle;
}

.meta-label {
  font-weight: 500;
  color: var(--vp-c-text-2);
}

.meta-value {
  color: var(--vp-c-text-1);
}

.meta-unit {
  color: var(--vp-c-text-3);
  font-size: 13px;
}

.meta-separator {
  color: var(--vp-c-divider);
  margin: 0 2px;
  user-select: none;
}

@media (max-width: 640px) {
  .article-metadata {
    font-size: 13px;
    gap: 3px;
  }

  .meta-icon {
    width: 14px;
    height: 14px;
  }

  .meta-item {
    gap: 3px;
  }

  .meta-separator {
    margin: 0 1px;
  }
}
</style>