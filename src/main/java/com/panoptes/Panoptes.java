package com.panoptes;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.logging.Logging;
import com.panoptes.service.AiService;
import com.panoptes.service.KnowledgeBaseService;
import com.panoptes.service.OpenAiCompatService;
import com.panoptes.service.PromptManager;
import com.panoptes.ui.AnalyzeContextMenuProvider;
import com.panoptes.ui.ConfigPanel;
import com.panoptes.ui.MainTab;

import javax.swing.*;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Panoptes — AI-powered Burp Suite plugin for business logic vulnerability auditing.
 * <p>
 * Named after Panoptes (Πανόπτης), the all-seeing giant of Greek mythology,
 * also known as Argus Panoptes (the hundred-eyed).
 */
public class Panoptes implements BurpExtension
{
    @Override
    public void initialize(MontoyaApi api)
    {
        api.extension().setName("Panoptes - AI Business Logic Auditor");
        Logging logging = api.logging();

        try
        {
            // ── Find knowledge directory (relative to JAR location) ──
            Path knowledgeDir = findKnowledgeDir(logging);

            // ── Core services ──
            PromptManager promptManager = new PromptManager(logging);
            AiService aiService = new OpenAiCompatService();
            KnowledgeBaseService kbService = new KnowledgeBaseService(knowledgeDir);

            // ── UI Components ──
            MainTab mainTab = new MainTab();
            ConfigPanel configPanel = new ConfigPanel(api, logging, kbService);

            // ── Tabbed pane: Results + Configuration ──
            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.addTab("Results", mainTab.getUi());
            tabbedPane.addTab("Configuration", configPanel.getUi());

            api.userInterface().registerSuiteTab("Panoptes", tabbedPane);

            // ── Right-click menu ──
            api.userInterface().registerContextMenuItemsProvider(
                    new AnalyzeContextMenuProvider(api, logging, mainTab, promptManager, aiService, kbService));

            logging.logToOutput("[Panoptes] v" + getVersion() + " 加载成功 ✓");
            logging.logToOutput("[Panoptes] 请前往 Panoptes > Configuration 配置 API 并选择 SRC 规则");
        }
        catch (Exception e)
        {
            logging.logToError("[Panoptes] Initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 查找 knowledge/ 目录。
     * 优先根据 JAR 文件路径推算，找不到则回退到当前工作目录。
     */
    private Path findKnowledgeDir(Logging logging)
    {
        try
        {
            // Try relative to JAR: Panoptes/build/libs/Panoptes.jar → Panoptes/knowledge/
            var codeSource = getClass().getProtectionDomain().getCodeSource();
            if (codeSource != null)
            {
                var jarUri = codeSource.getLocation().toURI();
                var jarPath = Paths.get(jarUri);
                var projectRoot = jarPath.getParent().getParent().getParent(); // up 3 dirs
                var knowledgeDir = projectRoot.resolve("knowledge");
                if (knowledgeDir.toFile().isDirectory())
                {
                    logging.logToOutput("[Panoptes] 知识库目录: " + knowledgeDir);
                    return knowledgeDir;
                }
            }
        }
        catch (Exception e)
        {
            // fall through
        }

        // Fallback: current working directory
        Path fallback = Paths.get("knowledge");
        logging.logToOutput("[Panoptes] 知识库目录 (fallback): " + fallback.toAbsolutePath());
        return fallback;
    }

    private String getVersion()
    {
        String v = getClass().getPackage().getImplementationVersion();
        return v != null ? v : "0.1.0";
    }
}
