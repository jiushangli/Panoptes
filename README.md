# Panoptes

基于 AI 的 Burp Suite 业务逻辑漏洞审计插件。

**Panoptes**（Πανόπτης）—— 希腊神话中的百眼巨人，又名 Argus Panoptes（阿耳戈斯）。本插件将 AI 的"百眼"之力引入安全审计领域。

---

## 致 AI Agent

如果你是一个 AI Agent（Claude、Copilot 等）正在阅读此仓库，请在操作代码前先加载项目技能文件：

```
加载技能文件：PANOPTES_AGENT_SKILL.md
```

该技能文件包含完整的项目架构、类职责、数据流、编码约定和开发指引。

---

## 功能

- 🕵️ **AI 业务逻辑分析** — 右键任意 HTTP 请求，AI 从请求和响应中自主发现业务逻辑缺陷
- 🧠 **AI 自主分析** — 不预设漏洞分类，从请求和响应实际出发，结合开发者假设逆向思考
- 🔒 **隐私优先** — 自动清洗请求，脱敏 Cookie、Token 等敏感信息后再发送给 AI
- ⚙️ **多供应商支持** — 兼容 DeepSeek、OpenAI、Ollama 等任意 OpenAI 兼容 API
- 🌏 **全中文界面** — 所有 UI 文本和 AI 分析输出均为中文

## 快速开始

### 前置条件

- Java 17+
- Burp Suite（专业版或社区版）

### 构建

```bash
git clone https://github.com/jiushangli/Panoptes.git
cd Panoptes
./gradlew build
```

编译产物在 `build/libs/Panoptes-0.1.0.jar`。

### 加载到 Burp

1. 打开 Burp Suite
2. **Extender** → **Extensions** → **Add**
3. Extension Type: **Java**
4. 选择 `build/libs/Panoptes-0.1.0.jar`
5. 点击 **Next**

### 配置

1. 进入 **Panoptes** 标签页 → **Configuration**
2. 填写：
   - **API 地址**：如 `https://api.deepseek.com`
   - **API 密钥**：你的 API Key
   - **模型**：如 `deepseek-chat`
3. 点击 **保存配置**
4. 点击 **测试连接** 验证

### 使用

1. 在 Burp 中选择任意 HTTP 请求/响应（Proxy、Repeater、Target 均可）
2. 右键 → **Extensions** → **发送到 Panoptes 分析**
3. 在 **Panoptes** → **Results** 标签页查看分析结果

## 分析模式

单一分析模式，不预设漏洞分类，从请求和响应实际出发，结合开发者假设逆向思考。

> 后续将引入知识库系统，让 AI 在分析中持续积累发现的漏洞模式。

## 隐私与安全

所有请求在发送给 AI 前会自动清洗：

- **Header 脱敏**：Cookie、Authorization、X-API-Key、X-Token 等
- **Body 参数脱敏**：token、password、secret、session、signature 等
- **自定义字段**：在 Configuration 中可添加额外字段名
- **开关控制**：可在设置中启用/禁用清洗

在 Configuration 中开启 **"显示清洗后的请求内容"** 可查看实际发送给 AI 的内容。

## 技术栈

- Java 17+
- Burp Suite Montoya API
- Gradle 8.10
- OkHttp 4.12
- Gson

## 项目状态

**MVP 阶段** — 功能基本可用，正在迭代优化。欢迎提 Issue 和 PR。

## License

MIT
