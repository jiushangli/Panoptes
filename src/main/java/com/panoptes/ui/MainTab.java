package com.panoptes.ui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Main result display for Panoptes.
 * Shows analysis results from the AI scan with a clean, monospaced layout.
 */
public class MainTab
{
    private final JPanel rootPanel;
    private final JTextArea resultArea;
    private final JLabel statusLabel;

    public MainTab()
    {
        rootPanel = new JPanel(new BorderLayout());

        // ── Top: Status bar ──
        JPanel topPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel("就绪。右键请求 → 发送到 Panoptes 开始分析");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(245, 245, 250));
        topPanel.add(statusLabel, BorderLayout.CENTER);

        // ── Center: Results text area ──
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        resultArea.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // ── Bottom: Clear ──
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> resultArea.setText(""));

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(clearButton);

        // Assemble
        rootPanel.add(topPanel, BorderLayout.NORTH);
        rootPanel.add(scrollPane, BorderLayout.CENTER);
        rootPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Welcome
        appendResult("═══════════════════════════════════════════\n");
        appendResult("  Panoptes — AI 业务逻辑审计插件\n");
        appendResult("═══════════════════════════════════════════\n");
        appendResult("  请先在 Configuration 标签页配置 API 信息。\n");
        appendResult("  然后右键任意 HTTP 请求 → Extensions →\n");
        appendResult("  发送到 Panoptes → 选择分析模式。\n");
        appendResult("───────────────────────────────────────────\n");
    }

    public Component getUi()
    {
        return rootPanel;
    }

    public void setStatus(String text)
    {
        SwingUtilities.invokeLater(() -> statusLabel.setText(text));
    }

    public void appendResult(String text)
    {
        SwingUtilities.invokeLater(() -> resultArea.append(text));
    }

    public void clearResults()
    {
        SwingUtilities.invokeLater(() -> resultArea.setText(""));
    }
}
