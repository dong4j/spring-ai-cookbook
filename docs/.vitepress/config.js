import {defineConfig} from 'vitepress'
import fs from 'fs'
import path from 'path'
import {fileURLToPath} from 'url'
import dayjs from 'dayjs'
import {InlineLinkPreviewElementTransform} from '@nolebase/vitepress-plugin-inline-link-preview/markdown-it'
import {GitChangelog, GitChangelogMarkdownSection,} from '@nolebase/vitepress-plugin-git-changelog/vite'
import {BiDirectionalLinks} from '@nolebase/markdown-it-bi-directional-links'
import timeline from "vitepress-markdown-timeline";
import {groupIconMdPlugin, groupIconVitePlugin} from 'vitepress-plugin-group-icons'
import {vitepressPluginLegend} from 'vitepress-plugin-legend'
import {EXTERNAL_SERVICES, GITHUB_CONFIG} from './theme/config/constants.ts'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)
const docsDir = path.resolve(__dirname, '..')

/**
 * 获取模块的显示名称
 */
function getModuleDisplayName(modulePath) {
  const indexPath = path.join(modulePath, 'index.md')
  if (fs.existsSync(indexPath)) {
    const content = fs.readFileSync(indexPath, 'utf-8')
    const match = content.match(/^#\s+(.+)$/m)
    if (match) {
      return match[1].trim()
    }
  }
  return path.basename(modulePath)
}

/**
 * 获取模块 docs 目录下的额外文档
 * @param {string} modulePath - docs 目录中的模块路径
 * @param {string} relativePath - 相对 docs 的路径（用于生成链接）
 */
function getModuleDocItems(modulePath, relativePath) {
  const items = []

  if (!fs.existsSync(modulePath)) {
    return items
  }

  const files = fs.readdirSync(modulePath, {withFileTypes: true})
  for (const file of files) {
    if (!file.isFile() || !file.name.endsWith('.md') || file.name === 'index.md') {
      continue
    }

    const docPath = path.join(modulePath, file.name)
    const title = getDocumentTitle(docPath)
    if (!title) {
      continue
    }

    const link = `/${relativePath}/${file.name.replace(/\.md$/, '')}`
    items.push({
                 text: title,
                 link: link
               })
  }

  return items.sort((a, b) => a.text.localeCompare(b.text, 'zh-CN'))
}

/**
 * 递归查找所有模块（从 docs 目录）
 */
function findModules(dir, basePath = '') {
  const modules = []

  if (!fs.existsSync(dir)) {
    return modules
  }

  const items = fs.readdirSync(dir, {withFileTypes: true})

  for (const item of items) {
    // 跳过隐藏文件和特殊目录
    if (item.name.startsWith('.') ||
        item.name === 'node_modules' ||
        item.name === '.vitepress') {
      continue
    }

    if (item.isDirectory() && /^\d/.test(item.name)) {
      const modulePath = path.join(dir, item.name)
      const relativePath = basePath ? `${basePath}/${item.name}` : item.name
      const indexPath = path.join(modulePath, 'index.md')

      if (fs.existsSync(indexPath)) {
        const displayName = getModuleDisplayName(modulePath)
        const link = `/${relativePath}/`

        const moduleInfo = {
          text: displayName,
          link: link
        }

        // 递归查找子模块
        const subModules = findModules(modulePath, relativePath)
        const docItems = getModuleDocItems(modulePath, relativePath)

        if (subModules.length > 0 || docItems.length > 0) {
          moduleInfo.items = []
          if (subModules.length > 0) {
            moduleInfo.items.push(...subModules)
          }
          if (docItems.length > 0) {
            moduleInfo.items.push(...docItems)
          }
        }

        modules.push(moduleInfo)
      } else {
        // 即使没有 index.md，也继续查找子模块
        const subModules = findModules(modulePath, relativePath)
        modules.push(...subModules)
      }
    }
  }

  // 按目录名排序
  return modules.sort((a, b) => {
    const aMatch = a.link.match(/(\d+(?:\.\d+)?)/)
    const bMatch = b.link.match(/(\d+(?:\.\d+)?)/)
    if (!aMatch || !bMatch) {
      return 0
    }

    const aNum = aMatch[1].split('.').map(n => parseInt(n.padStart(3, '0')))
    const bNum = bMatch[1].split('.').map(n => parseInt(n.padStart(3, '0')))

    for (let i = 0; i < Math.max(aNum.length, bNum.length); i++) {
      const aVal = aNum[i] || 0
      const bVal = bNum[i] || 0
      if (aVal !== bVal) {
        return aVal - bVal
      }
    }
    return 0
  })
}

/**
 * 模块分类配置
 * key: 分类名称
 * value: 最大模块编号（包含），用于判断模块属于哪个分类
 */
const CATEGORY_CONFIG = {
  // <= 2 的都属于"入门"
  '入门': 2,
  // <= 5 的都属于"核心功能"
  '核心功能': 5,
  // <= 13 的都属于"Model API"
  'Model API': 13,
  // <= 18 的都属于"高级功能"
  '高级功能': 18,
  // 大于 18 的都属于"部署与测试"
  '部署与测试': Infinity
}

/**
 * 根据模块编号获取分类名称
 * @param {number} num - 模块编号
 * @returns {string} 分类名称
 */
function getCategoryByModuleNum(num) {
  // 遍历配置，找到第一个匹配的分类
  for (const [categoryName, maxNum] of Object.entries(CATEGORY_CONFIG)) {
    if (num <= maxNum) {
      return categoryName
    }
  }
  // 理论上不会到达这里，因为最后一个分类是 Infinity
  return Object.keys(CATEGORY_CONFIG).slice(-1)[0]
}

/**
 * 获取分类顺序数组（用于菜单显示顺序）
 * @returns {Array<string>} 分类名称数组
 */
function getCategoryOrder() {
  return Object.keys(CATEGORY_CONFIG)
}

/**
 * 基于模块编号进行分类, 用于自动生成菜单
 */
function categorizeModules(modules) {
  const categorized = {}

  for (const module of modules) {
    const moduleNum = module.link.match(/\/(\d+)/)?.[1]
    if (!moduleNum) {
      continue
    }

    const num = parseInt(moduleNum)
    // 注意：guide 等不是以数字开头的目录，不会出现在模块列表中
    const category = getCategoryByModuleNum(num)

    if (!categorized[category]) {
      categorized[category] = []
    }
    categorized[category].push(module)
  }

  return categorized
}

/**
 * 获取文档的一级标题
 */
function getDocumentTitle(filePath) {
  if (!fs.existsSync(filePath)) {
    return null
  }
  const content = fs.readFileSync(filePath, 'utf-8')
  const match = content.match(/^#\s+(.+)$/m)
  if (match) {
    return match[1].trim()
  }
  return path.basename(filePath, path.extname(filePath))
}

/**
 * 获取指定目录下的所有文档，用于生成菜单项
 * @param {string} dirName - 目录名称（如 'guide', 'about', 'action'）
 * @returns {Array} 菜单项数组
 */
function getDirectoryItems(dirName) {
  const dirPath = path.join(docsDir, dirName)
  const items = []

  if (!fs.existsSync(dirPath)) {
    return items
  }

  // 读取目录下的所有文件
  const files = fs.readdirSync(dirPath, {withFileTypes: true})

  for (const file of files) {
    // 只处理 .md 文件
    if (!file.isFile() || !file.name.endsWith('.md')) {
      continue
    }

    const filePath = path.join(dirPath, file.name)
    let link = ''
    let sortKey = 0

    if (file.name === 'index.md') {
      // index.md 作为特殊处理，链接为 /目录名/，排序为 0
      link = `/${dirName}/`
      sortKey = 0
    } else {
      // 其他文件按编号排序，如 1.introduction.md -> /目录名/1.introduction
      const match = file.name.match(/^(\d+)\.(.+)\.md$/)
      if (match) {
        const num = parseInt(match[1])
        link = `/${dirName}/${file.name.replace(/\.md$/, '')}`
        sortKey = num
      } else {
        // 如果没有编号前缀，使用文件名（去掉 .md）作为链接，排序到最后
        const baseName = file.name.replace(/\.md$/, '')
        link = `/${dirName}/${baseName}`
        sortKey = 9999 // 无编号的文件排到最后
      }
    }

    const title = getDocumentTitle(filePath)
    if (title) {
      items.push({
                   text: title,
                   link: link,
                   sortKey: sortKey
                 })
    }
  }

  // 按 sortKey 排序
  items.sort((a, b) => a.sortKey - b.sortKey)

  return items.map(item => (
      {
        text: item.text,
        link: item.link
      }))
}

/**
 * 添加目录菜单到侧边栏（辅助函数）
 * @param {Object} sidebar - 侧边栏配置对象
 * @param {string} menuText - 菜单标题（如 '简介', '关于', '实战'）
 * @param {string} dirName - 目录名称（如 'guide', 'about', 'action'）
 */
function addDirectoryMenu(sidebar, menuText, dirName) {
  const items = getDirectoryItems(dirName)
  if (items.length > 0) {
    sidebar['/'].push({
                        text: menuText,
                        items: items
                      })
  }
}

/**
 * 生成侧边栏配置
 * 1. 首先添加 guide 目录菜单
 * 2. 然后按分类添加其他目录菜单
 * 3. 最后添加"关于"菜单
 * 后续可以参考 about 目录添加其他目录菜单, 菜单的顺序就是 addDirectoryMenu 执行的顺序,
 */
function generateSidebar() {
  const allModules = findModules(docsDir)
  const categorized = categorizeModules(allModules)

  const sidebar = {
    '/': []
  }

  const categoryOrder = getCategoryOrder()

  // 单独处理 "简介" 分类
  addDirectoryMenu(sidebar, '简介', 'guide')

  // 根据源码目录中的 README.md 动态生成的菜单, 不需要动. 添加新的目录菜单时, 使用 addDirectoryMenu(sidebar, '菜单名称', '目录名')
  for (const category of categoryOrder) {
    if (categorized[category] && categorized[category].length > 0) {
      sidebar['/'].push({
                          text: category,
                          items: categorized[category]
                        })
    }
  }

  // 添加 "关于`" 菜单
  addDirectoryMenu(sidebar, '关于', 'about')
  // 添加 "实战" 菜单
  addDirectoryMenu(sidebar, '实战', 'action')

  return sidebar
}

export default defineConfig(
    {

      vite: {
        publicDir: path.resolve(__dirname, '../public'),
        plugins: [
          groupIconVitePlugin(
              {
                // 自定义图标: https://github.com/vscode-icons/vscode-icons/wiki/ListOfFiles
                customIcon: {
                  'java': 'vscode-icons:file-type-java',
                  'bash': 'vscode-icons:file-type-shell',
                  'shell': 'vscode-icons:file-type-shell',
                  'sh': 'vscode-icons:file-type-shell',
                  'xml': 'vscode-icons:file-type-xml',
                  'maven': 'vscode-icons:file-type-maven',
                  'unplugin': 'https://unplugin.unjs.io/logo_light.svg',
                },
              }),
          GitChangelog(
              {
                // Fill in your repository URL here
                repoURL: () => GITHUB_CONFIG.url,
              }),
          GitChangelogMarkdownSection(),
        ],
        optimizeDeps: {
          exclude: [
            '@nolebase/vitepress-plugin-enhanced-readabilities/client',
            'vitepress',
            '@nolebase/ui',
          ],
        },
        ssr: {
          noExternal: [
            // If there are other packages that need to be processed by Vite, you can add them here.
            '@nolebase/vitepress-plugin-enhanced-readabilities',
            '@nolebase/ui',
            '@nolebase/vitepress-plugin-highlight-targeted-heading',
            '@nolebase/vitepress-plugin-inline-link-preview',
            '@nolebase/vitepress-plugin-git-changelog',
          ],
        },
      },

      title: 'Spring AI Cookbook',
      description: 'Spring AI Cookbook',
      base: '/',
      lang: 'zh-CN',

      // 域名配置
      // 如果部署到子路径，修改 base 为 '/spring-ai-cookbook/'
      // 当前配置为根域名 springai.dong4j.site

      // 忽略死链接检查（用于开发环境的 localhost 链接等）
      ignoreDeadLinks: [
        /^http:\/\/localhost/,
        /^https:\/\/localhost/
      ],

      head: [
        ['link', {rel: 'icon', href: '/favicon.ico'}],
        ['script', {
          src: EXTERNAL_SERVICES.busuanzi.scriptUrl,
          async: true,
          'data-api': EXTERNAL_SERVICES.busuanzi.apiUrl,
          'data-prefix': EXTERNAL_SERVICES.busuanzi.prefix,
          'data-style': EXTERNAL_SERVICES.busuanzi.style,
          'data-pjax': 'true'
        }],
        ['script', {
          src: EXTERNAL_SERVICES.umami.scriptUrl,
          defer: true,
          'data-host-url': EXTERNAL_SERVICES.umami.hostUrl,
          'data-website-id': EXTERNAL_SERVICES.umami.websiteId
        }]
      ],

      // markdown配置
      markdown: {
        // 开启代码行号显示
        lineNumbers: true,
        image: {
          // 开启图片懒加载
          lazyLoading: true
        },
        config(md) {
          // other markdown-it configurations...
          md.use(InlineLinkPreviewElementTransform)
          md.use(BiDirectionalLinks())
          md.use(timeline)
          md.use(groupIconMdPlugin, {
            titleBar: {includeSnippet: true},
          })
          vitepressPluginLegend(md, {
            markmap: {showToolbar: true}, // 显示脑图工具栏
            mermaid: true // 启用 Mermaid
          })
          md.renderer.rules.heading_close = (tokens, idx, options, env, slf) => {
            let htmlResult = slf.renderToken(tokens, idx, options);
            if (tokens[idx].tag === 'h1') {
              htmlResult += `<ArticleMetadata />`;
            }
            return htmlResult;
          }
        }
      },

      themeConfig: {
        siteTitle: 'Spring AI Cookbook',
        logo: '/logo.png',

        nav: [
          {text: '🏠 首页', link: '/'},
          {text: '🚀 开始', link: '/1.spring-ai-started/'},
          {text: '📊 统计', link: 'https://umami.dong4j.site/share/o0wIhLdP1EwFcdCt/spring-ai.dong4j.site', target: '_blank'}
        ],

        sidebar: generateSidebar(),

        socialLinks: [
          {icon: 'github', link: GITHUB_CONFIG.url}
        ],

        footer: {
          message: '基于 VitePress 构建',
          copyright: 'Copyright © 2025 Spring AI Cookbook'
        },

        search: {
          provider: 'local'
        },

        editLink: {
          pattern: GITHUB_CONFIG.editUrl,
          text: '在 GitHub 上编辑此页'
        },

        lastUpdated: {
          text: '最后更新于',
          formatOptions: {
            forceLocale: true, // 保持默认 locale 处理（可选）
            dateStyle: 'short',
            timeStyle: 'medium'
          },
          transform: (timestamp) => {
            // timestamp: number | undefined
            return dayjs(timestamp).format('YYYY-MM-DD HH:mm:ss')
          }
        },

        outline: {
          level: [2, 4],
          label: '页面大纲'
        }
      }
    })
