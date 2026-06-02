package com.panoptes.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.panoptes.model.AppConfig;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * OpenAI-compatible API service (works with DeepSeek, OpenAI, Ollama, etc.).
 */
public class OpenAiCompatService implements AiService
{
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final int TIMEOUT_SECONDS = 60;

    private final OkHttpClient httpClient;
    private final Gson gson;

    public OpenAiCompatService()
    {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    @Override
    public String analyze(AppConfig config, String systemPrompt, String requestText) throws Exception
    {
        // Build the request body
        JsonObject body = new JsonObject();
        body.addProperty("model", config.getModel());

        JsonArray messages = new JsonArray();

        // System message
        JsonObject systemMsg = new JsonObject();
        systemMsg.addProperty("role", "system");
        systemMsg.addProperty("content", systemPrompt);
        messages.add(systemMsg);

        // User message
        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", "Analyze this HTTP request:\n\n" + requestText);
        messages.add(userMsg);

        body.add("messages", messages);

        // Temperature: low for deterministic audit results
        body.addProperty("temperature", 0.3);
        body.addProperty("max_tokens", 2048);

        // Build the HTTP request
        String jsonBody = gson.toJson(body);
        String endpoint = config.getEndpoint().replaceAll("/+$", "") + "/v1/chat/completions";

        Request httpRequest = new Request.Builder()
                .url(endpoint)
                .addHeader("Authorization", "Bearer " + config.getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(jsonBody, JSON))
                .build();

        // Execute
        try (Response response = httpClient.newCall(httpRequest).execute())
        {
            if (!response.isSuccessful())
            {
                String errorBody = response.body() != null ? response.body().string() : "(empty)";
                throw new IOException("API returned " + response.code() + ": " + errorBody);
            }

            String responseBody = response.body() != null ? response.body().string() : "";
            return parseResponse(responseBody);
        }
    }

    /**
     * Parse the OpenAI-compatible response to extract the assistant's message content.
     */
    private String parseResponse(String jsonResponse)
    {
        JsonObject root = gson.fromJson(jsonResponse, JsonObject.class);

        // Handle API error responses
        if (root.has("error"))
        {
            JsonObject error = root.getAsJsonObject("error");
            String message = error.has("message") ? error.get("message").getAsString() : "Unknown API error";
            return "[ERROR] AI API error: " + message;
        }

        // Extract the content from choices[0].message.content
        JsonArray choices = root.getAsJsonArray("choices");
        if (choices == null || choices.isEmpty())
        {
            return "[ERROR] No choices returned from API";
        }

        JsonObject firstChoice = choices.get(0).getAsJsonObject();
        JsonObject message = firstChoice.getAsJsonObject("message");

        if (message == null || !message.has("content") || message.get("content").isJsonNull())
        {
            return "[INFO] AI returned empty response (possibly content filter)";
        }

        return message.get("content").getAsString();
    }
}
