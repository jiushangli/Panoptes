package com.panoptes.model;

import burp.api.montoya.persistence.PersistedObject;

/**
 * Plugin configuration — persisted across Burp restarts.
 */
public class AppConfig
{
    private static final String KEY_ENDPOINT = "panoptes.endpoint";
    private static final String KEY_API_KEY  = "panoptes.apiKey";
    private static final String KEY_MODEL    = "panoptes.model";
    private static final String KEY_SANITIZE = "panoptes.sanitizeEnabled";
    private static final String KEY_EXTRA    = "panoptes.sanitizeExtraFields";
    private static final String KEY_SHOW_RAW = "panoptes.showSanitizedRequest";

    private String  endpoint;
    private String  apiKey;
    private String  model;
    private boolean sanitizeEnabled;
    private String  sanitizeExtraFields;
    private boolean showSanitizedRequest;

    public static final String DEFAULT_ENDPOINT = "https://api.deepseek.com";
    public static final String DEFAULT_MODEL    = "deepseek-chat";

    public AppConfig()
    {
        this.endpoint        = DEFAULT_ENDPOINT;
        this.apiKey          = "";
        this.model           = DEFAULT_MODEL;
        this.sanitizeEnabled      = true;
        this.sanitizeExtraFields   = "";
        this.showSanitizedRequest  = false;
    }

    // ── Load from Burp persistent storage ──

    public static AppConfig load(PersistedObject store)
    {
        AppConfig cfg = new AppConfig();
        if (store.getString(KEY_ENDPOINT) != null)
            cfg.endpoint = store.getString(KEY_ENDPOINT);
        if (store.getString(KEY_API_KEY) != null)
            cfg.apiKey = store.getString(KEY_API_KEY);
        if (store.getString(KEY_MODEL) != null)
            cfg.model = store.getString(KEY_MODEL);
        if (store.getBoolean(KEY_SANITIZE) != null)
            cfg.sanitizeEnabled = store.getBoolean(KEY_SANITIZE);
        if (store.getString(KEY_EXTRA) != null)
            cfg.sanitizeExtraFields = store.getString(KEY_EXTRA);
        if (store.getBoolean(KEY_SHOW_RAW) != null)
            cfg.showSanitizedRequest = store.getBoolean(KEY_SHOW_RAW);
        return cfg;
    }

    // ── Save to Burp persistent storage ──

    public void save(PersistedObject store)
    {
        store.setString(KEY_ENDPOINT, endpoint);
        store.setString(KEY_API_KEY, apiKey);
        store.setString(KEY_MODEL, model);
        store.setBoolean(KEY_SANITIZE, sanitizeEnabled);
        store.setString(KEY_EXTRA, sanitizeExtraFields);
        store.setBoolean(KEY_SHOW_RAW, showSanitizedRequest);
    }

    // ── Getters / Setters ──

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public boolean isSanitizeEnabled() { return sanitizeEnabled; }
    public void setSanitizeEnabled(boolean sanitizeEnabled) { this.sanitizeEnabled = sanitizeEnabled; }

    public String getSanitizeExtraFields() { return sanitizeExtraFields; }
    public void setSanitizeExtraFields(String sanitizeExtraFields) { this.sanitizeExtraFields = sanitizeExtraFields; }

    public boolean isShowSanitizedRequest() { return showSanitizedRequest; }
    public void setShowSanitizedRequest(boolean showSanitizedRequest) { this.showSanitizedRequest = showSanitizedRequest; }

    public boolean isValid()
    {
        return endpoint != null && !endpoint.isEmpty()
                && apiKey != null && !apiKey.isEmpty()
                && model != null && !model.isEmpty();
    }
}
