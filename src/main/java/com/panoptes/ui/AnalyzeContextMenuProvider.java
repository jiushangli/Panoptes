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
 * Right-click context menu with submenu support and AI analysis integration.
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

        // ── Main menu: "Send to Panoptes" ──
        JMenu mainMenu = new JMenu("Send to Panoptes");

        // 🎯 Precision Analysis submenu
        JMenu precisionMenu = new JMenu("🎯 Precision Analysis");

        addModeItem(precisionMenu, PromptManager.AnalysisMode.AUTO, event);
        precisionMenu.addSeparator();
        addModeItem(precisionMenu, PromptManager.AnalysisMode.IDOR, event);
        addModeItem(precisionMenu, PromptManager.AnalysisMode.PARAM_TAMPERING, event);
        addModeItem(precisionMenu, PromptManager.AnalysisMode.STATE_MACHINE, event);
        addModeItem(precisionMenu, PromptManager.AnalysisMode.RACE_CONDITION, event);
        addModeItem(precisionMenu, PromptManager.AnalysisMode.RATE_LIMIT, event);
        addModeItem(precisionMenu, PromptManager.AnalysisMode.AUTH, event);

        // 🧠 Free Exploration submenu
        JMenuItem freeExploreItem = new JMenuItem("🧠 Free Exploration");
        freeExploreItem.addActionListener(e ->
                analyze(event.selectedRequestResponses(), PromptManager.AnalysisMode.FREE_EXPLORE));

        mainMenu.add(precisionMenu);
        mainMenu.addSeparator();
        mainMenu.add(freeExploreItem);

        menuItems.add(mainMenu);
        return menuItems;
    }

    private void addModeItem(JMenu parent, PromptManager.AnalysisMode mode, ContextMenuEvent event)
    {
        JMenuItem item = new JMenuItem(mode.getDisplayName());
        item.addActionListener(e ->
                analyze(event.selectedRequestResponses(), mode));
        parent.add(item);
    }

    /**
     * Run analysis in a background thread.
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
                    mainTab.appendResult("⚠ Configuration incomplete. Please set API Endpoint, Key, and Model in the Panoptes tab.\n\n");
                    mainTab.setStatus("Config required");
                    return null;
                }

                RequestSanitizer sanitizer = new RequestSanitizer(
                        config.isSanitizeEnabled() ? config.getSanitizeExtraFields() : "");

                String systemPrompt = promptManager.buildSystemPrompt(mode);

                for (HttpRequestResponse requestResponse : requestResponses)
                {
                    analyzeSingle(requestResponse, config, sanitizer, systemPrompt, mode);
                }

                mainTab.setStatus("Ready");
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
            mainTab.setStatus("Analyzing: " + method + " " + url + " [" + mode.getDisplayName() + "]");

            // 1. Sanitize the request
            RequestSanitizer.SanitizedRequest safe;
            if (config.isSanitizeEnabled())
            {
                safe = sanitizer.sanitize(requestResponse.request());
            }
            else
            {
                // Bypass sanitization (use with caution!)
                String raw = method + " " + url + "\n" +
                        requestResponse.request().headers().stream()
                                .map(h -> h.name() + ": " + h.value())
                                .reduce((a, b) -> a + "\n" + b).orElse("") +
                        (requestResponse.request().body() != null ?
                                "\n\n" + requestResponse.request().body().toString() : "");
                safe = new RequestSanitizer.SanitizedRequest(raw, url, method);
            }

            // 2. Call AI
            String analysis = aiService.analyze(config, systemPrompt, safe.getSafeText());

            // 3. Display result
            StringBuilder result = new StringBuilder();
            result.append("═══════════════════════════════════════════\n");
            result.append("  [").append(mode.getDisplayName()).append("]\n");
            result.append("  ").append(method).append(" ").append(url).append("\n");
            result.append("═══════════════════════════════════════════\n");
            result.append(analysis).append("\n\n");

            mainTab.appendResult(result.toString());
        }
        catch (Exception e)
        {
            String errorMsg = "✗ Analysis failed for " + url + ": " + e.getMessage();
            logging.logToError("[Panoptes] " + errorMsg);
            mainTab.appendResult("═══════════════════════════════════════════\n");
            mainTab.appendResult("  [ERROR] " + url + "\n");
            mainTab.appendResult("  " + e.getMessage() + "\n\n");
        }
    }
}
