import {h, nextTick, onMounted, watch} from 'vue'
import DefaultTheme from 'vitepress/theme'
import 'virtual:group-icons.css'
import {
  NolebaseEnhancedReadabilitiesMenu,
  NolebaseEnhancedReadabilitiesScreenMenu,
} from '@nolebase/vitepress-plugin-enhanced-readabilities/client'
import '@nolebase/vitepress-plugin-enhanced-readabilities/client/style.css'
import {NolebaseInlineLinkPreviewPlugin,} from '@nolebase/vitepress-plugin-inline-link-preview/client'

import '@nolebase/vitepress-plugin-inline-link-preview/client/style.css'
import {NolebaseHighlightTargetedHeading,} from '@nolebase/vitepress-plugin-highlight-targeted-heading/client'

import '@nolebase/vitepress-plugin-highlight-targeted-heading/client/style.css' //*
import {NolebaseGitChangelogPlugin} from '@nolebase/vitepress-plugin-git-changelog/client'

import '@nolebase/vitepress-plugin-git-changelog/client/style.css'
import '@nolebase/vitepress-plugin-enhanced-mark/client/style.css'
import "vitepress-markdown-timeline/dist/theme/index.css";
import {inBrowser, useRoute} from 'vitepress'
import mediumZoom from 'medium-zoom'
import {NProgress} from "nprogress-v2/dist/index.js"; // 进度条组件
import "nprogress-v2/dist/index.css"; // 进度条样式
import {initComponent} from 'vitepress-plugin-legend/component'
import 'vitepress-plugin-legend/dist/index.css'

import './custom.css'

export const Theme = {
  extends: DefaultTheme,
  setup() {
    const route = useRoute()
    const initZoom = () => {
      // 为所有图片增加缩放功能
      mediumZoom('.main img', {background: 'var(--vp-c-bg)'})
    }
    onMounted(() => {
      initZoom()
    })
    watch(
        () => route.path,
        () => nextTick(() => initZoom())
    )
  },
  Layout: () => {
    return h(DefaultTheme.Layout, null, {
      // A enhanced readabilities menu for wider screens
      'nav-bar-content-after': () => h(NolebaseEnhancedReadabilitiesMenu),
      // A enhanced readabilities menu for narrower screens (usually smaller than iPad Mini)
      'nav-screen-content-after': () => h(NolebaseEnhancedReadabilitiesScreenMenu),
      'layout-top': () => [
        h(NolebaseHighlightTargetedHeading),
      ],
    })
  },
  enhanceApp({app, router}) {
    initComponent(app)
    app.use(NolebaseInlineLinkPreviewPlugin)
    app.use(NolebaseGitChangelogPlugin)
    // 切换路由进度条
    if (inBrowser) {
      NProgress.configure({showSpinner: false});

      router.onBeforeRouteChange = () => {
        NProgress.start(); // 开始进度条
      };
      router.onAfterRouteChange = () => {
        NProgress.done(); // 停止进度条
      };
    }
  },
}

export default Theme

