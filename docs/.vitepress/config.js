import {defineConfig} from 'vitepress'

export default defineConfig({
                              title: 'Spring AI Cookbook',
                              description: 'Spring AI 教程文档',
                              base: '/',
                              lang: 'zh-CN',

                              // 域名配置
                              // 如果部署到子路径，修改 base 为 '/spring-ai-cookbook/'
                              // 当前配置为根域名 springai.dong4j.site

                              head: [
                                ['link', {rel: 'icon', href: '/favicon.ico'}]
                              ],

                              themeConfig: {
                                siteTitle: 'Spring AI Cookbook',
                                logo: '/logo.png',

                                nav: [
                                  {text: '首页', link: '/'},
                                  {text: '开始', link: '/1.spring-ai-started/'}
                                ],

                                sidebar: {
                                  '/': [
                                    {
                                      text: '入门',
                                      items: [
                                        {text: '快速开始', link: '/1.spring-ai-started/'},
                                        {text: 'Chat Client API', link: '/2.spring-ai-chat-client/'}
                                      ]
                                    },
                                    {
                                      text: '核心功能',
                                      items: [
                                        {text: 'Prompts', link: '/3.spring-ai-prompts/'},
                                        {text: 'Structured Output', link: '/4.spring-ai-structured/'},
                                        {text: 'Multimodality', link: '/5.spring-ai-multimodality/'}
                                      ]
                                    },
                                    {
                                      text: 'Model API',
                                      items: [
                                        {text: 'Model API', link: '/6.spring-ai-model/'},
                                        {text: 'Chat Model', link: '/7.spring-ai-model-chat/'},
                                        {text: 'Embedding Model', link: '/8.spring-ai-model-embedding/'},
                                        {text: 'Image Model', link: '/9.spring-ai-model-image/'},
                                        {text: 'Audio Model', link: '/10.spring-ai-model-audio/'},
                                        {text: 'Moderation Models', link: '/11.spring-ai-model-moderation/'},
                                        {text: 'Chat Memory', link: '/12.spring-ai-model-memory/'},
                                        {text: 'Tool Calling', link: '/13.spring-ai-model-tool-calling/'}
                                      ]
                                    },
                                    {
                                      text: '高级功能',
                                      items: [
                                        {text: 'MCP', link: '/14.spring-ai-mcp/'},
                                        {text: 'RAG', link: '/15.spring-ai-rag/'},
                                        {text: 'Model Evaluation', link: '/16.spring-ai-model-evaluation/'},
                                        {text: 'Vector Database', link: '/17.spring-ai-vector-database/'},
                                        {text: 'Observability', link: '/18.spring-ai-observability/'}
                                      ]
                                    },
                                    {
                                      text: '部署与测试',
                                      items: [
                                        {text: 'Docker Compose', link: '/19.spring-ai-docker/'},
                                        {text: 'Testcontainers', link: '/20.spring-ai-testcontainer/'}
                                      ]
                                    }
                                  ]
                                },

                                socialLinks: [
                                  {icon: 'github', link: 'https://github.com/dong4j/spring-ai-cookbook'}
                                ],

                                footer: {
                                  message: '基于 Spring AI 构建',
                                  copyright: 'Copyright © 2025 Spring AI Cookbook'
                                },

                                search: {
                                  provider: 'local'
                                },

                                editLink: {
                                  pattern: 'https://github.com/your-repo/edit/main/docs/:path',
                                  text: '在 GitHub 上编辑此页'
                                },

                                lastUpdated: {
                                  text: '最后更新于',
                                  formatOptions: {
                                    dateStyle: 'short',
                                    timeStyle: 'medium'
                                  }
                                }
                              }
                            })

