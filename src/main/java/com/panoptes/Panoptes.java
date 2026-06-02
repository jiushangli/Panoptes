package com.panoptes;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.logging.Logging;
import com.panoptes.service.AiService;
import com.panoptes.service.OpenAiCompatService;
import com.panoptes.service.PromptManager;
import com.panoptes.ui.AnalyzeContextMenuProvider;
import com.panoptes.ui.ConfigPanel;
import com.panoptes.ui.MainTab;

import javax.swing.*;

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
            // ── Core services ──
            PromptManager promptManager = new PromptManager(logging);
            AiService aiService = new OpenAiCompatService();

            // ── UI Components ──
            MainTab mainTab = new MainTab();
            ConfigPanel configPanel = new ConfigPanel(api, logging);

            // ── Tabbed pane: Results + Configuration ──
            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.addTab("Results", mainTab.getUi());
            tabbedPane.addTab("Configuration", configPanel.getUi());

            api.userInterface().registerSuiteTab("Panoptes", tabbedPane);

            // ── Right-click menu ──
            api.userInterface().registerContextMenuItemsProvider(
                    new AnalyzeContextMenuProvider(api, logging, mainTab, promptManager, aiService));

            logging.logToOutput("[Panoptes] v" + getVersion() + " loaded ✓");
            logging.logToOutput("[Panoptes] Go to Panoptes > Configuration to set up your API");
        }
        catch (Exception e)
        {
            logging.logToError("[Panoptes] Initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getVersion()
    {
        String v = getClass().getPackage().getImplementationVersion();
        return v != null ? v : "0.1.0";
    }
}
