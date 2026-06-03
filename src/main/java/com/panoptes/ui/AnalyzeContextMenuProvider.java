package com.panoptes.ui;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import com.panoptes.model.AppConfig;
import com.panoptes.service.AiService;
import com.panoptes.service.KnowledgeBaseService;
import com.panoptes.service.PromptManager;
import com.panoptes.service.RequestSanitizer;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 右键菜单 — 发送到 Panoptes 分析。
 */
public class AnalyzeContextMenuProvider implements ContextMenuItemsProvider
{
    private final MontoyaApi api;
    private final Logging logging;
    private final MainTab mainTab;
    private final PromptManager promptManager;
    private final AiService aiService;
    private final KnowledgeBaseService kbService;
    private static final Pattern THINK_TAG_PATTERN = Pattern.compile(
            "<think>.*?</think>", Pattern.DOTALL);

    public AnalyzeContextMenuProvider(MontoyaApi api, Logging logging, MainTab mainTab,
                                      PromptManager promptManager, AiService aiService,
                                      KnowledgeBaseService kbService)
    {
        this.api = api;
        this.logging = logging;
        this.mainTab = mainTab;
        this.promptManager = promptManager;
        this.aiService = aiService;
        this.kbService = kbService;
    }

    @Override
    public List<Component> provideMenuItems(ContextMenuEvent event)
    {
        List<Component> menuItems = new ArrayList<>();

        if (event.selectedRequestResponses().isEmpty())
        {
            return menuItems;
        }

        JMenuItem sendItem = new JMenuItem("发送到 Panoptes 分析");
        sendItem.addActionListener(e ->
                analyze(event.selectedRequestResponses()));

        menuItems.add(sendItem);
        return menuItems;
    }

    private void analyze(List<HttpRequestResponse> requestResponses)
    {
        new SwingWorker<Void, Void>()
        {
            @Override
            protected Void doInBackground()
            {
                AppConfig config = AppConfig.load(api.persistence().extensionData());

                if (!config.isValid())
                {
                    mainTab.appendResult("⚠ 配置不完整。请在 Panoptes 标签页的 Configuration 中设置 API 地址、密钥和模型。\n\n");
                    mainTab.setStatus("需要配置");
                    return null;
                }

                RequestSanitizer sanitizer = new RequestSanitizer(
                        config.isSanitizeEnabled() ? config.getSanitizeExtraFields() : "");

                // 加载当前 SRC 规则
                String kbContent = kbService.getActiveSrcContent();
                String srcName = kbService.getActiveSrc();
                String systemPrompt = promptManager.buildSystemPrompt(kbContent);

                for (HttpRequestResponse requestResponse : requestResponses)
                {
                    analyzeSingle(requestResponse, config, sanitizer, systemPrompt, srcName);
                }

                mainTab.setStatus("分析完成");
                return null;
            }
        }.execute();
    }

    private void analyzeSingle(HttpRequestResponse requestResponse, AppConfig config,
                                RequestSanitizer sanitizer, String systemPrompt,
                                String srcName)
    {
        String url = requestResponse.request().url();
        String method = requestResponse.request().method();

        try
        {
            mainTab.setStatus("正在分析: " + method + " " + url);

            // 1. 清洗请求
            String requestText;
            if (config.isSanitizeEnabled())
            {
                requestText = sanitizer.sanitize(requestResponse.request()).getSafeText();
            }
            else
            {
                requestText = method + " " + url + "\n" +
                        requestResponse.request().headers().stream()
                                .map(h -> h.name() + ": " + h.value())
                                .reduce((a, b) -> a + "\n" + b).orElse("") +
                        (requestResponse.request().body() != null ?
                                "\n\n" + requestResponse.request().body().toString() : "");
            }

            // 2. 拼接响应
            String fullText = requestText;
            if (requestResponse.response() != null)
            {
                int statusCode = requestResponse.response().statusCode();
                String responseBody = requestResponse.response().body() != null
                        ? requestResponse.response().body().toString() : "";
                if (config.isSanitizeEnabled())
                {
                    fullText += sanitizer.sanitizeResponseText(statusCode, responseBody);
                }
                else
                {
                    fullText += "\n=== HTTP Response ===\n"
                            + "Status: " + statusCode + "\n"
                            + (responseBody.isEmpty() ? "" : "\n" + responseBody);
                }
            }

            // 3. 调用 AI
            String analysis = aiService.analyze(config, systemPrompt, fullText);
            analysis = stripThinkTags(analysis);

            // 4. 展示结果
            StringBuilder result = new StringBuilder();
            result.append("═══════════════════════════════════════════\n");
            result.append("  ").append(method).append(" ").append(url).append("\n");
            result.append("  SRC 规则: ").append(srcName).append("\n");
            result.append("═══════════════════════════════════════════\n");
            result.append(analysis).append("\n");

            // 调试模式
            if (config.isShowSanitizedRequest())
            {
                result.append("  ── 实际发送给 AI 的内容 ──\n");
                result.append(fullText).append("\n");
                result.append("  ── 以上为 AI 收到的内容 ──\n");
            }

            result.append("\n");
            mainTab.appendResult(result.toString());
        }
        catch (Exception e)
        {
            String errorMsg = "✗ 分析失败 " + url + ": " + e.getMessage();
            logging.logToError("[Panoptes] " + errorMsg);
            mainTab.appendResult("═══════════════════════════════════════════\n");
            mainTab.appendResult("  [错误] " + url + "\n");
            mainTab.appendResult("  " + e.getMessage() + "\n\n");
        }
    }

    private static String stripThinkTags(String text)
    {
        if (text == null) return "";
        return THINK_TAG_PATTERN.matcher(text).replaceAll("").trim();
    }
}
