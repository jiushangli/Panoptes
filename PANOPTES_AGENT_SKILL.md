---
name: panoptes-burp-plugin
description: 基于 AI 的 Burp Suite 业务逻辑漏洞审计插件
---

# Panoptes — AI 业务逻辑审计插件

Panoptes 是一个 Burp Suite 插件，利用 AI（LLM，通过 OpenAI 兼容 API）分析 HTTP 请求中的业务逻辑漏洞。

## 项目架构

```
Panoptes (BurpExtension 入口)
├── ConfigPanel              — 配置面板（API 地址、密钥、模型、清洗设置）
├── MainTab                  — 结果展示面板（等宽字体文本区）
├── AnalyzeContextMenuProvider — 右键菜单（分层子菜单）
├── service/
│   ├── PromptManager        — System Prompt 模板（8 种分析模式，中文）
│   ├── AiService            — AI 调用接口
│   ├── OpenAiCompatService  — OpenAI 兼容 API 客户端（DeepSeek/OpenAI/Ollama）
│   └── RequestSanitizer     — 请求清洗器（脱敏 Cookie/Token 等敏感信息）
└── model/
    └── AppConfig            — 配置模型（通过 Burp extensionData 持久化）
```

## 分析模式

所有 Prompt 均为中文。右键菜单可选：

| 模式 | 枚举值 | 说明 |
|------|--------|------|
| 🎯 自动分析 | AUTO | 开放式分析，不预设漏洞维度，由 AI 自主发现 |
| 🧠 自由探索 | FREE_EXPLORE | 不受约束的创造性分析 |

> 原有的 6 个专项模式（IDOR、参数篡改等）已移除，改为让 AI 自主发现漏洞模式。知识库系统将在后续迭代中加入。

## 数据流

```
用户右键请求 → 选择分析模式
    ↓
AnalyzeContextMenuProvider.analyzeSingle()
    ↓
1. RequestSanitizer 清洗请求（脱敏敏感字段）
2. PromptManager 构建对应模式的 System Prompt
3. OpenAiCompatService 调用 LLM API（OpenAI 兼容 /v1/chat/completions）
4. 结果展示在 MainTab 中
```

## 配置项（通过 Burp extensionData 持久化）

- API 地址（如 `https://api.deepseek.com`）
- API 密钥（仅本地存储）
- 模型名（如 `deepseek-chat`）
- 启用请求清洗（默认开启）
- 额外脱敏字段（逗号分隔）
- 显示清洗后的请求内容（调试用）

## 请求清洗机制

发送给 AI 前自动脱敏以下字段：

**Header 脱敏：** Cookie, Authorization, X-API-Key, X-Token, X-CSRF-Token 等
**Body 参数脱敏：** token, password, secret, session, signature, api_key 等
**自定义字段：** 用户可在配置中额外添加字段名

## 关键技术细节

- **Java 17+**，Gradle 8.10，fat JAR 打包
- **Montoya API**（`net.portswigger.burp.extensions:montoya-api:2024.12`）
- **OkHttp 4.12** 处理 HTTP 调用
- **Gson** 处理 JSON 解析
- 插件入口：`META-INF/services/net.portswigger.burp.extensions.BurpExtension` → `com.panoptes.Panoptes`

## 常见开发任务

### 修改 Prompt
编辑 `PromptManager.buildAutoPrompt()` 或 `buildFreeExplorePrompt()`

### 新增分析模式
1. 在 `PromptManager.AnalysisMode` 添加枚举值
2. 在 `PromptManager` 中添加对应 Prompt 方法
3. 更新 `buildSystemPrompt()` 的 switch 分支
4. 在 `AnalyzeContextMenuProvider.provideMenuItems()` 添加菜单项

### 新增内置脱敏规则
编辑 `RequestSanitizer.SENSITIVE_HEADERS` 或 `SENSITIVE_PARAM_NAMES`

### 调试
在 Configuration 中开启"显示清洗后的请求内容"查看实际发送给 AI 的内容

## 构建与运行

```bash
./gradlew build
# 产物：build/libs/Panoptes-0.1.0.jar
```

在 Burp Suite 中加载：Extender → Extensions → Add（Type: Java）

## 重要约定

- 所有用户可见的 UI 文本：**中文**
- 所有 AI Prompt：**中文**
- 代码注释和变量名：**英文**
- 配置持久化：`api.persistence().extensionData()`
- AI 调用在 `SwingWorker` 后台线程中执行
- LLM API 必须兼容 OpenAI 格式（`/v1/chat/completions` 接口）

## 目录结构

```
Panoptes/
├── build.gradle
├── settings.gradle
├── gradlew / gradlew.bat / gradle/wrapper/
├── PANOPTES_AGENT_SKILL.md         ← 本文件，AI Agent 项目理解用
├── src/main/java/com/panoptes/
│   ├── Panoptes.java               ← BurpExtension 入口
│   ├── model/AppConfig.java        ← 配置模型 + 持久化
│   ├── service/
│   │   ├── AiService.java          ← AI 调用接口
│   │   ├── OpenAiCompatService.java ← API 客户端
│   │   ├── PromptManager.java      ← 8 种中文 Prompt
│   │   └── RequestSanitizer.java   ← 敏感数据脱敏
│   └── ui/
│       ├── AnalyzeContextMenuProvider.java ← 右键分层菜单
│       ├── ConfigPanel.java        ← 配置页面
│       └── MainTab.java            ← 结果展示页面
└── src/main/resources/META-INF/services/
    └── net.portswigger.burp.extensions.BurpExtension
```
