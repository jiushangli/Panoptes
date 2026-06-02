---
name: panoptes-burp-plugin
description: AI-powered Burp Suite plugin for business logic vulnerability auditing
---

# Panoptes — AI Business Logic Auditor

**Panoptes** is a Burp Suite plugin that uses AI (LLMs via OpenAI-compatible API) to analyze HTTP requests for business logic vulnerabilities.

## Architecture Overview

```
Panoptes (BurpExtension entry point)
├── ConfigPanel         — UI tab for API configuration (endpoint, key, model, sanitization)
├── MainTab             — Results display tab (monospaced text area)
├── AnalyzeContextMenuProvider — Right-click context menu with submenu
├── service/
│   ├── PromptManager   — System prompt templates (8 analysis modes, Chinese)
│   ├── AiService       — Interface for AI API calls
│   ├── OpenAiCompatService — OpenAI-compatible API client (DeepSeek, OpenAI, Ollama)
│   └── RequestSanitizer — Redacts cookies/tokens/sensitive params before AI
└── model/
    └── AppConfig       — Configuration model with Burp persistence
```

### Analysis Modes

All prompts are in Chinese. Modes available via right-click menu:

| Mode | Enum | Description |
|------|------|-------------|
| 🎯 Auto Detect | AUTO | Checks all 6 dimensions |
| IDOR / Authorization | IDOR | Insecure Direct Object References |
| Parameter Tampering | PARAM_TAMPERING | Price, quantity, status manipulation |
| State Machine | STATE_MACHINE | Process bypass, state rollback |
| Race Condition | RACE_CONDITION | Concurrent request vulnerabilities |
| Bulk Ops / Rate Limit | RATE_LIMIT | Enumeration, mass operations |
| Auth / Session | AUTH | Authentication & session issues |
| 🧠 Free Exploration | FREE_EXPLORE | Creative, unconstrained analysis |

### Data Flow

```
User right-clicks a request → selects analysis mode
    ↓
AnalyzeContextMenuProvider.analyzeSingle()
    ↓
1. RequestSanitizer sanitizes the request (redacts sensitive fields)
2. PromptManager builds the system prompt for the selected mode
3. OpenAiCompatService calls the LLM API (OpenAI-compatible /v1/chat/completions)
4. Result is displayed in MainTab
```

### Configuration (persisted via Burp extensionData)

- API Endpoint (e.g., `https://api.deepseek.com`)
- API Key (stored locally only)
- Model name (e.g., `deepseek-chat`)
- Sanitization toggle (default: on)
- Extra fields to redact (comma-separated)
- Show sanitized request debug output

### Request Sanitization

Before sending to the AI API, sensitive fields are redacted:

**Headers redacted:** Cookie, Authorization, X-API-Key, X-Token, X-CSRF-Token, etc.
**Body params redacted:** token, password, secret, session, signature, api_key, etc.
**Custom fields:** User can add extra field names in config.

### Key Technical Details

- **Java 17+**, Gradle 8.10, fat JAR packaging
- **Montoya API** (`net.portswigger.burp.extensions:montoya-api:2024.12`)
- **OkHttp 4.12** for HTTP calls
- **Gson** for JSON parsing
- Plugin entry: `META-INF/services/net.portswigger.burp.extensions.BurpExtension` → `com.panoptes.Panoptes`

## Common Tasks

### Adding a new analysis mode
1. Add enum value in `PromptManager.AnalysisMode`
2. Add prompt method in `PromptManager` (e.g., `buildNewModePrompt()`)
3. Update `buildSystemPrompt()` switch
4. Add menu item in `AnalyzeContextMenuProvider.provideMenuItems()`

### Adding a custom redaction field
Users add comma-separated field names in Configuration tab. To add a built-in rule:
1. Edit `RequestSanitizer.SENSITIVE_HEADERS` or `SENSITIVE_PARAM_NAMES`

### Debugging
Enable "显示清洗后的请求内容" in Configuration to see exactly what's sent to the AI.

## Build & Run

```bash
./gradlew build
# Output: build/libs/Panoptes-0.1.0.jar
```

Load JAR in Burp Suite: Extender → Extensions → Add (Type: Java)

## Important Conventions

- All user-facing UI text is in **Chinese**
- All AI prompts are in **Chinese**
- Code comments and variable names are in **English**
- Configuration is persisted via `api.persistence().extensionData()`
- AI calls run in `SwingWorker` background threads
- LLM API must be OpenAI-compatible (`/v1/chat/completions` endpoint)

## Project Structure

```
Panoptes/
├── build.gradle
├── settings.gradle
├── gradlew / gradlew.bat / gradle/wrapper/
├── src/main/java/com/panoptes/
│   ├── Panoptes.java                     ← BurpExtension entry point
│   ├── model/AppConfig.java              ← Config model + persistence
│   ├── service/
│   │   ├── AiService.java                ← AI interface
│   │   ├── OpenAiCompatService.java      ← API client
│   │   ├── PromptManager.java            ← 8 prompts in Chinese
│   │   └── RequestSanitizer.java         ← Sensitive data redaction
│   └── ui/
│       ├── AnalyzeContextMenuProvider.java ← Right-click submenu
│       ├── ConfigPanel.java              ← Configuration tab
│       └── MainTab.java                  ← Results tab
└── src/main/resources/META-INF/services/
    └── net.portswigger.burp.extensions.BurpExtension
```
