package com.panoptes.service;

import com.panoptes.model.AppConfig;

/**
 * Interface for AI analysis services.
 */
public interface AiService
{
    /**
     * Analyze a sanitized request with the given system prompt.
     *
     * @param config      the plugin configuration (endpoint, key, model)
     * @param systemPrompt the system prompt guiding the AI
     * @param requestText the sanitized HTTP request text
     * @return the AI's analysis text
     * @throws Exception if the API call fails
     */
    String analyze(AppConfig config, String systemPrompt, String requestText) throws Exception;
}
