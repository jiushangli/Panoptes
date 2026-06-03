package com.panoptes.ui;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import com.panoptes.model.AppConfig;
import com.panoptes.service.AiService;
import com.panoptes.service.OpenAiCompatService;
import com.panoptes.service.PromptManager;
import com.panoptes.service.RequestSanitizer;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

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

    public AnalyzeContextMenuProvider(MontoyaApi api, Logging logging, MainTab mainTab,
                                      PromptManager promptManager, AiService aiService)
    {
        this.api = api;
        this.logging = logging;
        this.mainTab = mainTab;
        this.promptManager = promptManager;
        this.aiService = aiService;
    }

    @Override
    public List<Component> provideMenuItems(ContextMenuEvent event)
    {
        List<Component> menuItems = new ArrayList<>();

        if (event.selectedRequestResponses().isEmpty())
        {
            return menuItems;
        }

        JMenu mainMenu = new JMenu("发送到 Panoptes");

        JMenuItem autoItem = new JMenuItem("🎯 自动分析");
        autoItem.addActionListener(e ->
                analyze(event.selectedRequestResponses(), PromptManager.AnalysisMode.AUTO));

        JMenuItem exploreItem = new JMenuItem("🧠 自由探索");
        exploreItem.addActionListener(e ->
                analyze(event.selectedRequestResponses(), PromptManager.AnalysisMode.FREE_EXPLORE));

        mainMenu.add(autoItem);
        mainMenu.add(exploreItem);

        menuItems.add(mainMenu);
        return menuItems;
    }

    /**
     * 在后台线程中执行分析。
     */
    private void analyze(List<HttpRequestResponse> requestResponses, PromptManager.AnalysisMode mode)
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

                String systemPrompt = promptManager.buildSystemPrompt(mode);

                for (HttpRequestResponse requestResponse : requestResponses)
                {
                    analyzeSingle(requestResponse, config, sanitizer, systemPrompt, mode);
                }

                mainTab.setStatus("分析完成");
                return null;
            }
        }.execute();
    }

    private void analyzeSingle(HttpRequestResponse requestResponse, AppConfig config,
                                RequestSanitizer sanitizer, String systemPrompt,
                                PromptManager.AnalysisMode mode)
    {
        String url = requestResponse.request().url();
        String method = requestResponse.request().method();

        try
        {
            mainTab.setStatus("正在分析: " + method + " " + url + " [" + mode.getDisplayName() + "]");

            // 1. 清洗请求
            RequestSanitizer.SanitizedRequest safe;
            String requestText;
            if (config.isSanitizeEnabled())
            {
                safe = sanitizer.sanitize(requestResponse.request());
                requestText = safe.getSafeText();
            }
            else
            {
                String raw = method + " " + url + "\n" +
                        requestResponse.request().headers().stream()
                                .map(h -> h.name() + ": " + h.value())
                                .reduce((a, b) -> a + "\n" + b).orElse("") +
                        (requestResponse.request().body() != null ?
                                "\n\n" + requestResponse.request().body().toString() : "");
                requestText = raw;
                safe = new RequestSanitizer.SanitizedRequest(raw, url, method);
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

            // 4. 展示结果
            StringBuilder result = new StringBuilder();
            result.append("═══════════════════════════════════════════\n");
            result.append("  [").append(mode.getDisplayName()).append("]\n");
            result.append("  ").append(method).append(" ").append(url).append("\n");
            result.append("═══════════════════════════════════════════\n");

            result.append(analysis).append("\n");

            // 调试模式：展示实际发送给 AI 的内容
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
}
