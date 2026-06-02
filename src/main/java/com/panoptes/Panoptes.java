package com.panoptes;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.logging.Logging;
import com.panoptes.ui.MainTab;
import com.panoptes.ui.AnalyzeContextMenuProvider;

/**
 * Panoptes — AI-powered Burp Suite plugin for business logic vulnerability auditing.
 * <p>
 * Named after Panoptes (Πανόπτης), the all-seeing giant of Greek mythology,
 * also known as Argus Panoptes (the hundred-eyed).
 */
public class Panoptes implements BurpExtension
{
    private MontoyaApi api;
    private MainTab mainTab;

    @Override
    public void initialize(MontoyaApi api)
    {
        this.api = api;
        Logging logging = api.logging();

        // Set extension name shown in Burp's Extensions tab
        api.extension().setName("Panoptes - AI Business Logic Auditor");

        try
        {
            // Create the main result display tab
            mainTab = new MainTab();

            // Register the Panoptes tab in Burp's UI
            api.userInterface().registerSuiteTab("Panoptes", mainTab.getUi());

            // Register the right-click context menu
            api.userInterface().registerContextMenuItemsProvider(
                    new AnalyzeContextMenuProvider(api, mainTab));

            logging.logToOutput("[Panoptes] v" + getClass().getPackage().getImplementationVersion() + " loaded ✓");
            logging.logToOutput("[Panoptes] Right-click any HTTP request → Extensions → Panoptes → Send to Panoptes");
        }
        catch (Exception e)
        {
            logging.logToError("[Panoptes] Initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
