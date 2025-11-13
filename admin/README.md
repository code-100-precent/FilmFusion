# 拍在雅安管理后台

## 项目简介

拍在雅安管理后台，用于管理校友、POI、校友故事、情感标签和路线等内容。

## 技术栈

- Vue 3
- Element Plus
- Vue Router
- Pinia
- Axios
- Vite
- Tailwind CSS

## 安装依赖

```bash
cd admin
npm install
```

## 开发运行

```bash
npm run dev
```

访问 http://localhost:3000

## 构建生产版本

```bash
npm run build
```

## 功能模块

- ✅ 校友管理（添加、查询）
- ✅ POI管理（添加、编辑）
- ✅ 校友故事管理（审核）
- ✅ 情感标签管理（添加、查询）
- ✅ 路线管理（添加、编辑）
- ✅ 文件上传
- ✅ 登录验证

## 样式特性

- ✅ Tailwind CSS 现代化样式
- ✅ 页面过渡动画（fade、slide）
- ✅ 响应式设计
- ✅ 悬停效果和过渡动画
- ✅ 渐变背景和阴影效果

## API 配置

默认代理到 `http://localhost:8080`，可在 `vite.config.js` 中修改。

## Mock 模式

默认启用 Mock 模式，所有接口返回模拟数据。

**登录信息**：
- 用户名：`admin`
- 密码：`admin`

修改 `src/api/index.js` 中的 `USE_MOCK` 变量可以切换 Mock 模式。
