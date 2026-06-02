# Panoptes

AI-powered Burp Suite plugin for business logic vulnerability auditing.

**Panoptes** (Πανόπτης) — the all-seeing giant of Greek mythology, also known as Argus Panoptes. This plugin brings the "hundred eyes" of AI to security auditing.

## Features

- 🕵️ **Right-click analysis** — Send any HTTP request/response to AI for business logic vulnerability analysis
- 🧠 **AI-powered** — Currently in development, DeepSeek integration coming soon

## Quick Start

### Prerequisites

- Java 17+
- Burp Suite (Professional or Community)
- Gradle (or use the Gradle wrapper)

### Build

```bash
git clone https://github.com/jiushangli/Panoptes.git
cd Panoptes
gradle build
```

The compiled JAR will be at `build/libs/Panoptes-0.1.0.jar`.

### Load into Burp Suite

1. Open Burp Suite
2. Go to **Extender** → **Extensions**
3. Click **Add**
4. Set **Extension Type** to **Java**
5. Select the `Panoptes-0.1.0.jar` file
6. Click **Next**

### Usage

1. Select any HTTP request/response in Burp (Proxy, Repeater, Target, etc.)
2. Right-click → **Extensions** → **Panoptes** → **Send to Panoptes**
3. View analysis results in the **Panoptes** tab

## Project Status

**MVP阶段** — 当前版本为骨架，仅展示请求信息。AI 分析功能正在开发中。

## Tech Stack

- Java 17
- Burp Suite Montoya API
- Gradle 8.10

## License

MIT
