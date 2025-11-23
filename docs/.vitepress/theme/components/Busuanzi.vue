<template>
  <div class="busuanzi">
    <span class="busuanzi-item">
      <span class="busuanzi-emoji">👥</span>
      <span class="busuanzi-label">本站访客数</span>
      <span class="busuanzi-value" id="busuanzi_site_uv">--</span>
      <span class="busuanzi-unit">人次</span>
    </span>
    <span class="busuanzi-separator">·</span>
    <span class="busuanzi-item">
      <span class="busuanzi-emoji">👀</span>
      <span class="busuanzi-label">本站总访问量</span>
      <span class="busuanzi-value" id="busuanzi_site_pv">--</span>
      <span class="busuanzi-unit">次</span>
    </span>
  </div>
</template>

<style scoped>
.busuanzi {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  padding: 16px 0;
  margin: 20px 0;
  font-size: 14px;
  color: var(--vp-c-text-2);
  line-height: 1.6;
  border-top: 1px solid var(--vp-c-divider);
}

.busuanzi-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.busuanzi-emoji {
  font-size: 16px;
  line-height: 1;
  flex-shrink: 0;
}

.busuanzi-label {
  color: var(--vp-c-text-2);
}

.busuanzi-value {
  color: var(--vp-c-brand);
  font-weight: 600;
  min-width: 2ch;
  text-align: center;
}

.busuanzi-unit {
  color: var(--vp-c-text-3);
  font-size: 13px;
}

.busuanzi-separator {
  color: var(--vp-c-divider);
  margin: 0 4px;
  user-select: none;
}

@media (max-width: 640px) {
  .busuanzi {
    font-size: 13px;
    padding: 12px 0;
    gap: 6px;
  }

  .busuanzi-item {
    gap: 3px;
  }

  .busuanzi-unit {
    font-size: 12px;
  }

  .busuanzi-separator {
    margin: 0 2px;
  }
}
</style>