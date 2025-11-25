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
import {inBrowser, useData, useRoute} from 'vitepress'
import mediumZoom from 'medium-zoom'
import {NProgress} from "nprogress-v2/dist/index.js"; // 进度条组件
import "nprogress-v2/dist/index.css"; // 进度条样式
import {initComponent} from 'vitepress-plugin-legend/component'
import 'vitepress-plugin-legend/dist/index.css'

import codeblocksFold from 'vitepress-plugin-codeblocks-fold';
import 'vitepress-plugin-codeblocks-fold/style/index.css';

import ArticleMetadata from "./components/ArticleMetadata.vue"
import BackToTop from "./components/BackToTop.vue"

import Footer from "./components/Footer.vue";
import giscusTalk from 'vitepress-plugin-comment-with-giscus';
import {fetchBusuanzi} from './utils/functions'

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

    // Get frontmatter and route
    const {frontmatter} = useData();

    // 代码折叠
    codeblocksFold({route, frontmatter}, true, 300);

    // giscus配置
    giscusTalk({
                 repo: 'dong4j/spring-ai-cookbook', //仓库
                 repoId: 'R_kgDOQbLo3g', //仓库ID
                 category: 'General', // 讨论分类
                 categoryId: 'DIC_kwDOQbLo3s4CyHoV', //讨论分类ID
                 mapping: 'pathname',
                 inputPosition: 'bottom',
                 lang: 'zh-CN',
               },
               {
                 frontmatter, route
               },
               // 默认值为true，表示已启用，此参数可以忽略；
               // 如果为false，则表示未启用
               // 可以使用“comment:true”序言在页面上单独启用它
               true
    );
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
      // Back to top button and footer
      'layout-bottom': () => [
        h(BackToTop),
        h(Footer),
      ],
    })
  },
  enhanceApp({app, router}) {
    app.component('ArticleMetadata', ArticleMetadata)
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

        if (inBrowser) {
          // 触发 Umami 页面浏览跟踪（SPA 路由切换）
          setTimeout(() => {
            try {
              // Umami 自动跟踪，但为了确保 SPA 路由切换也能跟踪，手动触发
              if (window.umami && typeof window.umami.track === 'function') {
                window.umami.track()
              } else if (window.umami && typeof window.umami === 'function') {
                window.umami()
              }
            } catch (error) {
              console.warn('Error tracking umami pageview:', error)
            }
          }, 100)

          // 路由切换后重新加载 busuanzi 统计数据
          setTimeout(() => {
            fetchBusuanzi({
                            updatePagePv: true,
                            updateSiteStats: true
                          })
          }, 300)
        }
      };
    }
  },
}

export default Theme

