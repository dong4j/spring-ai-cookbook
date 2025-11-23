<script lang="ts" setup>
import { useData } from 'vitepress'
import { computed, ref, onMounted } from 'vue'
import { countWord } from '../utils/functions'

const { page } = useData()
const date = computed(() => {
  const d = new Date(page.value.lastUpdated!)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
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
    // 初始化时执行一次
    analyze()
})
</script>


<template>
    <div class="article-metadata">
        <div class="meta-item">
            <svg class="meta-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M12 6V12L16 14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span class="meta-label">更新:</span>
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