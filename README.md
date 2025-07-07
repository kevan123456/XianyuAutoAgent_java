# 🚀 Xianyu AutoAgent - 智能闲鱼客服机器人系统

[![Java Version](https://img.shields.io/badge/java21%2B-blue)](https://www.python.org/) [![LLM Powered](https://img.shields.io/badge/LLM-powered-FF6F61)](https://platform.openai.com/)

专为闲鱼平台打造的AI值守解决方案，实现闲鱼平台7×24小时自动化值守，支持多专家协同决策、智能议价和上下文感知对话。


## 🌟 核心特性

### 智能对话引擎
| 功能模块   | 技术实现            | 关键特性                                                     |
| ---------- | ------------------- | ------------------------------------------------------------ |
| 上下文感知 | 会话历史存储        | 轻量级对话记忆管理，完整对话历史作为LLM上下文输入            |
| 专家路由   | LLM prompt+规则路由 | 基于提示工程的意图识别 → 专家Agent动态分发，支持议价/技术/客服多场景切换 |

### 业务功能矩阵
| 模块     | 已实现                        | 规划中                       |
| -------- | ----------------------------- | ---------------------------- |
| 核心引擎 | ✅ LLM自动回复<br>✅ 上下文管理 | 🔄 情感分析增强               |
| 议价系统 | ✅ 阶梯降价策略                | 🔄 市场比价功能               |
| 技术支持 | ✅ 网络搜索整合                | 🔄 RAG知识库增强              |

## 🚴 快速开始

### 环境要求
- JDK 21+
- Maven 3.9.10+

### 安装步骤
```bash
1. 克隆仓库
git clone https://github.com/qOeOp/goofish-ai-cs.git
cd goofish-ai-cs

2. 配置环境变量
- AI_DASHSCOPE_API_KEY : 阿里云大模型API-KEY
- COOKIES_STR : 闲鱼网页端Cookie

3. 运行脚本
run.ps1
注意：默认使用的模型是通义千问，如需使用其他API，请自行修改application.yml文件中的模型地址和模型名称；
COOKIES_STR自行在闲鱼网页端获取cookies(网页端F12打开控制台，选择Network，点击Fetch/XHR,点击一个请求，查看cookies)

4. (可选)创建提示词文件src/main/resources/prompt-config.txt
```

## 🤝 参与贡献

欢迎通过 Issue 提交建议或 PR 贡献代码，请遵循 [贡献指南](https://contributing.md/)



## 🛡 注意事项

⚠️ 注意：**本项目仅供学习与交流，如有侵权联系作者删除。**

鉴于项目的特殊性，开发团队可能在任何时间**停止更新**或**删除项目**。
