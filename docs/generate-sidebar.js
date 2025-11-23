#!/usr/bin/env node

/**
 * 动态生成 VitePress 侧边栏配置
 * 根据项目中的模块结构自动生成侧边栏
 */

import fs from 'fs';
import path from 'path';
import {fileURLToPath} from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const rootDir = path.resolve(__dirname, '..');

// 模块分类配置
const moduleCategories = {
  '入门': {
    pattern: /^[12]\./,
    items: []
  },
  '核心功能': {
    pattern: /^[345]\./,
    items: []
  },
  'Model API': {
    pattern: /^[6-9]\.|^1[0-3]\./,
    items: []
  },
  '高级功能': {
    pattern: /^1[4-8]\./,
    items: []
  },
  '部署与测试': {
    pattern: /^1[9]\.|^20\./,
    items: []
  }
};

/**
 * 获取模块的显示名称
 */
function getModuleDisplayName(modulePath) {
  const readmePath = path.join(modulePath, 'README.md');
  if (fs.existsSync(readmePath)) {
    const content = fs.readFileSync(readmePath, 'utf-8');
    const match = content.match(/^#\s+(.+)$/m);
    if (match) {
      return match[1].trim();
    }
  }
  return path.basename(modulePath);
}

/**
 * 递归查找所有模块
 */
function findModules(dir, basePath = '') {
  const modules = [];
  const items = fs.readdirSync(dir, {withFileTypes: true});

  for (const item of items) {
    if (item.isDirectory() && /^\d/.test(item.name)) {
      const modulePath = path.join(dir, item.name);
      const relativePath = path.join(basePath, item.name);
      const readmePath = path.join(modulePath, 'README.md');

      if (fs.existsSync(readmePath)) {
        const displayName = getModuleDisplayName(modulePath);
        const link = `/${relativePath}/`;

        const moduleInfo = {
          text: displayName,
          link: link
        };

        // 查找子模块
        const subModules = findModules(modulePath, relativePath);
        if (subModules.length > 0) {
          moduleInfo.items = subModules;
        }

        modules.push(moduleInfo);
      } else {
        // 即使没有 README.md，也继续查找子模块
        const subModules = findModules(modulePath, relativePath);
        modules.push(...subModules);
      }
    }
  }

  // 按目录名排序
  return modules.sort((a, b) => {
    const aNum = parseInt(a.link.match(/\/(\d+)/)?.[1] || '0');
    const bNum = parseInt(b.link.match(/\/(\d+)/)?.[1] || '0');
    return aNum - bNum;
  });
}

/**
 * 将模块分类
 */
function categorizeModules(modules) {
  const categorized = {};

  for (const module of modules) {
    const moduleNum = module.link.match(/\/(\d+)/)?.[1];
    if (!moduleNum) {
      continue;
    }

    for (const [category, config] of Object.entries(moduleCategories)) {
      if (config.pattern.test(moduleNum.padStart(2, '0'))) {
        if (!categorized[category]) {
          categorized[category] = [];
        }
        categorized[category].push(module);
        break;
      }
    }
  }

  return categorized;
}

/**
 * 生成侧边栏配置
 */
function generateSidebar() {
  const modulesDir = rootDir;
  const allModules = findModules(modulesDir);
  const categorized = categorizeModules(allModules);

  const sidebar = {
    '/': []
  };

  for (const [category, modules] of Object.entries(categorized)) {
    if (modules.length > 0) {
      sidebar['/'].push({
                          text: category,
                          items: modules
                        });
    }
  }

  return sidebar;
}

// 生成并输出配置
const sidebar = generateSidebar();
console.log(JSON.stringify(sidebar, null, 2));

