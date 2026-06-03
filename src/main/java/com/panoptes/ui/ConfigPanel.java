package com.panoptes.ui;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.logging.Logging;
import com.panoptes.model.AppConfig;
import com.panoptes.service.KnowledgeBaseService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * 配置面板 — API 设置 + SRC 知识库选择。
 */
public class ConfigPanel
{
    private final JPanel rootPanel;
    private final JTextField endpointField;
    private final JPasswordField apiKeyField;
    private final JTextField modelField;
    private final JCheckBox sanitizeCheckbox;
    private final JTextField extraFieldsField;
    private final JCheckBox showRawCheckbox;
    private final JComboBox<String> srcComboBox;
    private final JLabel statusLabel;

    private final MontoyaApi api;
    private final Logging logging;
    private final AppConfig config;
    private final KnowledgeBaseService kbService;

    public ConfigPanel(MontoyaApi api, Logging logging, KnowledgeBaseService kbService)
    {
        this.api = api;
        this.logging = logging;
        this.config = AppConfig.load(api.persistence().extensionData());
        this.kbService = kbService;

        // ── Build UI ──
        rootPanel = new JPanel(new GridBagLayout());
        rootPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        int row = 0;

        // ── Title ──
        JLabel title = new JLabel("Panoptes 配置");
        title.setFont(new Font(title.getFont().getName(), Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 3;
        rootPanel.add(title, gbc);
        gbc.gridwidth = 1;

        // Separator
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 3;
        rootPanel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1;

        // ── API Endpoint ──
        gbc.gridx = 0; gbc.gridy = row;
        rootPanel.add(new JLabel("API 地址:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        endpointField = new JTextField(config.getEndpoint(), 40);
        endpointField.setToolTipText("例如 https://api.deepseek.com 或 https://api.openai.com");
        rootPanel.add(endpointField, gbc);
        gbc.gridwidth = 1;
        row++;

        // ── API Key ──
        gbc.gridx = 0; gbc.gridy = row;
        rootPanel.add(new JLabel("API 密钥:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        apiKeyField = new JPasswordField(config.getApiKey(), 40);
        apiKeyField.setToolTipText("你的 API Key（仅本地存储，仅发送到配置的地址）");
        rootPanel.add(apiKeyField, gbc);
        gbc.gridwidth = 1;
        row++;

        // ── Model ──
        gbc.gridx = 0; gbc.gridy = row;
        rootPanel.add(new JLabel("模型:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        modelField = new JTextField(config.getModel(), 40);
        modelField.setToolTipText("例如 deepseek-chat, gpt-4o, 或本地模型名");
        rootPanel.add(modelField, gbc);
        gbc.gridwidth = 1;
        row++;

        // ── Separator ──
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 3;
        rootPanel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1;

        // ── SRC Knowledge Base ──
        List<String> srcList = kbService.listAvailableSrcs();
        String activeSrc = kbService.getActiveSrc();

        gbc.gridx = 0; gbc.gridy = row;
        rootPanel.add(new JLabel("当前 SRC 规则:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        srcComboBox = new JComboBox<>(srcList.toArray(new String[0]));
        srcComboBox.setSelectedItem(activeSrc);
        srcComboBox.setToolTipText("选择当前挖掘的 SRC，分析时 AI 会参考对应的漏洞规则");
        rootPanel.add(srcComboBox, gbc);
        gbc.gridwidth = 1;
        row++;

        JLabel srcHint = new JLabel("在 knowledge/ 目录下添加 .md 文件来管理更多 SRC 规则");
        srcHint.setFont(new Font(srcHint.getFont().getName(), Font.PLAIN, 11));
        srcHint.setForeground(Color.GRAY);
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        rootPanel.add(srcHint, gbc);
        gbc.gridwidth = 1;
        row++;

        // ── Separator ──
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 3;
        rootPanel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1;

        // ── Sanitization ──
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        sanitizeCheckbox = new JCheckBox("启用请求清洗（推荐）", config.isSanitizeEnabled());
        sanitizeCheckbox.setToolTipText("发送给 AI 前自动脱敏 Cookie、Token 等敏感信息");
        rootPanel.add(sanitizeCheckbox, gbc);
        gbc.gridwidth = 1;
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        rootPanel.add(new JLabel("额外脱敏字段:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        extraFieldsField = new JTextField(config.getSanitizeExtraFields(), 40);
        extraFieldsField.setToolTipText("逗号分隔，例如 signature,nonce,private_key");
        rootPanel.add(extraFieldsField, gbc);
        gbc.gridwidth = 1;
        row++;

        // ── Show raw sanitized request ──
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        showRawCheckbox = new JCheckBox("显示清洗后的请求内容（调试用）", config.isShowSanitizedRequest());
        showRawCheckbox.setToolTipText("开启后，每次分析结果中会展示实际发送给 AI 的请求内容");
        rootPanel.add(showRawCheckbox, gbc);
        gbc.gridwidth = 1;
        row++;

        // ── Save Button ──
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton saveButton = new JButton("保存配置");
        saveButton.addActionListener(e -> saveConfig());
        buttonPanel.add(saveButton);

        JButton testButton = new JButton("测试连接");
        testButton.addActionListener(e -> testConnection());
        buttonPanel.add(testButton);

        rootPanel.add(buttonPanel, gbc);
        row++;

        // ── Status ──
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        gbc.insets = new Insets(10, 4, 4, 4);
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font(statusLabel.getFont().getName(), Font.ITALIC, 12));
        rootPanel.add(statusLabel, gbc);

        // Fill remaining space
        gbc.gridx = 0; gbc.gridy = row + 1; gbc.weighty = 1.0;
        rootPanel.add(new JPanel(), gbc);
    }

    public Component getUi()
    {
        return rootPanel;
    }

    private void saveConfig()
    {
        config.setEndpoint(endpointField.getText().trim());
        config.setApiKey(new String(apiKeyField.getPassword()));
        config.setModel(modelField.getText().trim());
        config.setSanitizeEnabled(sanitizeCheckbox.isSelected());
        config.setSanitizeExtraFields(extraFieldsField.getText().trim());
        config.setShowSanitizedRequest(showRawCheckbox.isSelected());
        config.save(api.persistence().extensionData());

        // Save SRC selection to knowledge/config.json
        String selectedSrc = (String) srcComboBox.getSelectedItem();
        kbService.setActiveSrc(selectedSrc);

        statusLabel.setForeground(new Color(0, 128, 0));
        statusLabel.setText("✅ 配置已保存（SRC 规则: " + selectedSrc + "）");
        logging.logToOutput("[Panoptes] 配置已保存，SRC: " + selectedSrc);
    }

    private void testConnection()
    {
        saveConfig();

        if (!config.isValid())
        {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText("❌ 请填写所有必填项（API 地址、密钥、模型）");
            return;
        }

        statusLabel.setForeground(Color.BLUE);
        statusLabel.setText("正在测试连接...");

        new SwingWorker<String, Void>()
        {
            @Override
            protected String doInBackground() throws Exception
            {
                okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                        .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                        .build();

                String endpoint = config.getEndpoint().replaceAll("/+$", "") + "/v1/chat/completions";

                com.google.gson.JsonObject body = new com.google.gson.JsonObject();
                body.addProperty("model", config.getModel());
                com.google.gson.JsonArray messages = new com.google.gson.JsonArray();
                com.google.gson.JsonObject msg = new com.google.gson.JsonObject();
                msg.addProperty("role", "user");
                msg.addProperty("content", "Hello, respond with exactly: OK");
                messages.add(msg);
                body.add("messages", messages);
                body.addProperty("max_tokens", 10);

                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(endpoint)
                        .addHeader("Authorization", "Bearer " + config.getApiKey())
                        .post(okhttp3.RequestBody.create(
                                new com.google.gson.Gson().toJson(body),
                                okhttp3.MediaType.get("application/json")))
                        .build();

                try (okhttp3.Response response = client.newCall(request).execute())
                {
                    if (response.isSuccessful())
                    {
                        return "✅ 连接成功！API 可用。";
                    }
                    else
                    {
                        String errorBody = response.body() != null ? response.body().string() : "";
                        return "❌ API 返回 " + response.code() + "：" + errorBody;
                    }
                }
            }

            @Override
            protected void done()
            {
                try
                {
                    String result = get();
                    if (result.startsWith("✅"))
                    {
                        statusLabel.setForeground(new Color(0, 128, 0));
                    }
                    else
                    {
                        statusLabel.setForeground(Color.RED);
                    }
                    statusLabel.setText(result);
                }
                catch (Exception e)
                {
                    statusLabel.setForeground(Color.RED);
                    statusLabel.setText("❌ 连接失败：" + e.getMessage());
                }
            }
        }.execute();
    }
}
