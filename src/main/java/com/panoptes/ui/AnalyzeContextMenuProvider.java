package com.panoptes.ui;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Right-click context menu provider for Panoptes.
 * Adds "Send to Panoptes" option when HTTP requests are selected.
 */
public class AnalyzeContextMenuProvider implements ContextMenuItemsProvider
{
    private final MontoyaApi api;
    private final MainTab mainTab;

    public AnalyzeContextMenuProvider(MontoyaApi api, MainTab mainTab)
    {
        this.api = api;
        this.mainTab = mainTab;
    }

    @Override
    public List<Component> provideMenuItems(ContextMenuEvent event)
    {
        List<Component> menuItems = new ArrayList<>();

        // Only show menu when there are HTTP request/response pairs selected
        if (event.selectedRequestResponses().isEmpty())
        {
            return menuItems;
        }

        JMenuItem sendToPanoptes = new JMenuItem("Send to Panoptes");
        sendToPanoptes.setIcon(null);

        sendToPanoptes.addActionListener(e ->
                analyzeInBackground(event.selectedRequestResponses()));

        menuItems.add(sendToPanoptes);
        return menuItems;
    }

    /**
     * Run analysis in a background thread to avoid blocking Burp's UI.
     */
    private void analyzeInBackground(List<HttpRequestResponse> requestResponses)
    {
        new SwingWorker<Void, Void>()
        {
            @Override
            protected Void doInBackground()
            {
                for (HttpRequestResponse requestResponse : requestResponses)
                {
                    analyzeRequest(requestResponse);
                }
                return null;
            }
        }.execute();
    }

    /**
     * Analyze a single request/response pair.
     * MVP version: just shows request details. AI analysis will be added later.
     */
    private void analyzeRequest(HttpRequestResponse requestResponse)
    {
        HttpRequest request = requestResponse.request();
        String url = request.url();
        String method = request.method();
        String headers = String.join("\n", request.headers().stream()
                .map(h -> h.name() + ": " + h.value())
                .toList());
        String body = request.body() != null ? request.body().toString() : "";

        // Show "analyzing" status
        mainTab.setStatus("Analyzing: " + method + " " + url);

        // For MVP: just echo the request info (AI will replace this)
        StringBuilder result = new StringBuilder();
        result.append("───────────────────────────────────────────\n");
        result.append("  [" + method + "] ").append(url).append("\n");
        result.append("───────────────────────────────────────────\n");
        result.append("  Headers:\n");
        for (String h : request.headers().stream()
                .map(hdr -> "    " + hdr.name() + ": " + hdr.value())
                .toList())
        {
            result.append(h).append("\n");
        }
        result.append("\n  Body:\n").append("    ").append(
                body.isEmpty() ? "(empty)" : body.replace("\n", "\n    ")
        ).append("\n\n");
        result.append("  ⚡ AI analysis will be available in the next version.\n");
        result.append("\n");

        mainTab.appendResult(result.toString());
        mainTab.setStatus("Ready");
    }
}
