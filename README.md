# Panoptes

AI-powered Burp Suite plugin for business logic vulnerability auditing.

**Panoptes** (Πανόπτης) — the all-seeing giant of Greek mythology, also known as Argus Panoptes. This plugin brings the "hundred eyes" of AI to security auditing.

---

## For AI Agents

If you are an AI agent (Claude, Copilot, etc.) reading this repository, please **load the project skill file** before working on this codebase:

```
Load the skill file: PANOPTES_AGENT_SKILL.md
```

This skill contains the full architecture, class responsibilities, data flow, conventions, and task instructions needed to work effectively with this project.

---

## Features

- 🕵️ **AI-powered business logic analysis** — Right-click any HTTP request to analyze for IDOR, parameter tampering, state machine violations, race conditions, and more
- 🧠 **8 analysis modes** — From focused checks (IDOR, auth, params) to open-ended "free exploration"
- 🔒 **Privacy-first** — Automatic request sanitization redacts cookies, tokens, and sensitive parameters before sending to AI
- ⚙️ **Multi-provider support** — Works with DeepSeek, OpenAI, Ollama, and any OpenAI-compatible API
- 🌏 **全中文界面** — All UI text and AI analysis in Chinese

## Quick Start

### Prerequisites

- Java 17+
- Burp Suite (Professional or Community)

### Build

```bash
git clone https://github.com/jiushangli/Panoptes.git
cd Panoptes
./gradlew build
```

The compiled JAR will be at `build/libs/Panoptes-0.1.0.jar`.

### Load into Burp Suite

1. Open Burp Suite
2. **Extender** → **Extensions** → **Add**
3. Extension Type: **Java**
4. Select `build/libs/Panoptes-0.1.0.jar`
5. Click **Next**

### Configure

1. Go to **Panoptes** tab → **Configuration**
2. Fill in:
   - **API 地址**: e.g., `https://api.deepseek.com`
   - **API 密钥**: your API key
   - **模型**: e.g., `deepseek-chat`
3. Click **保存配置**
4. Click **测试连接** to verify

### Usage

1. Select any HTTP request/response in Burp (Proxy, Repeater, Target)
2. Right-click → **Extensions** → **发送到 Panoptes**
3. Choose **🎯 精准分析** (structured) or **🧠 自由探索** (creative)
4. View analysis results in the **Panoptes** → **Results** tab

## Analysis Modes

| Mode | Description |
|------|-------------|
| 🎯 Auto Detect | AI checks all 6 vulnerability dimensions |
| IDOR / 越权 | Insecure Direct Object References |
| 参数篡改 | Price, quantity, status manipulation |
| 状态机绕过 | Process bypass, state rollback |
| 竞态条件 | Concurrent request vulnerabilities |
| 批量 / 频率控制 | Enumeration, mass operations |
| 认证 / 会话 | Authentication & session issues |
| 🧠 自由探索 | Creative, unconstrained analysis |

## Privacy & Security

All requests are sanitized before being sent to the AI API:

- **Headers redacted**: Cookie, Authorization, X-API-Key, X-Token, etc.
- **Body params redacted**: token, password, secret, session, signature, etc.
- **Custom fields**: Add extra field names in Configuration tab
- **Toggle**: Enable/disable sanitization in settings

Enable **"显示清洗后的请求内容"** in Configuration for debug mode to see exactly what's sent to the AI.

## Tech Stack

- Java 17+
- Burp Suite Montoya API
- Gradle 8.10
- OkHttp 4.12
- Gson

## Project Status

**MVP 阶段** — 功能基本可用，正在迭代优化。欢迎提 Issue 和 PR。

## License

MIT
