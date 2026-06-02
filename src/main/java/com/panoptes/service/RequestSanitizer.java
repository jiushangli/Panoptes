package com.panoptes.service;

import burp.api.montoya.http.message.requests.HttpRequest;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Sanitizes HTTP requests before sending them to the AI API.
 * Redacts sensitive headers (cookies, auth tokens) and parameters.
 */
public class RequestSanitizer
{
    private static final Set<String> SENSITIVE_HEADERS = new HashSet<>(Arrays.asList(
            "cookie", "set-cookie", "authorization", "proxy-authorization",
            "x-api-key", "x-token", "x-csrf-token", "x-xsrf-token",
            "x-auth-token", "x-access-token", "x-session-id",
            "api-key", "token",
            // Google-specific
            "x-client-data", "x-browser-validation", "x-browser-copyright",
            "x-browser-channel", "x-browser-year"
    ));

    private static final Set<String> SENSITIVE_PARAM_NAMES = new HashSet<>(Arrays.asList(
            "token", "password", "passwd", "secret", "session", "signature",
            "auth", "jwt", "api_key", "api_key", "access_token", "refresh_token",
            "csrf", "xsrf", "nonce", "private_key", "key"
    ));

    private static final Pattern JSON_SENSITIVE_VALUE = Pattern.compile(
            "\"(" + String.join("|", SENSITIVE_PARAM_NAMES) + ")\"\\s*:\\s*\"([^\"]+)\"",
            Pattern.CASE_INSENSITIVE
    );

    private final Set<String> extraSensitiveHeaders;
    private final Set<String> extraSensitiveParams;

    public RequestSanitizer(String extraFields)
    {
        this.extraSensitiveHeaders = new HashSet<>();
        this.extraSensitiveParams = new HashSet<>();
        parseExtraFields(extraFields);
    }

    public RequestSanitizer()
    {
        this("");
    }

    private void parseExtraFields(String extra)
    {
        if (extra == null || extra.isBlank()) return;
        for (String field : extra.split(","))
        {
            String trimmed = field.trim().toLowerCase();
            if (!trimmed.isEmpty())
            {
                extraSensitiveHeaders.add(trimmed);
                extraSensitiveParams.add(trimmed);
            }
        }
    }

    /**
     * Sanitize a full HTTP request into a safe-for-AI text representation.
     */
    public SanitizedRequest sanitize(HttpRequest request)
    {
        String url = request.url();
        String method = request.method();

        // Sanitize headers
        List<String> sanitizedHeaders = request.headers().stream()
                .map(h -> sanitizeHeader(h.name(), h.value()))
                .collect(Collectors.toList());

        // Sanitize body
        String body = "";
        if (request.body() != null && request.body().length() > 0)
        {
            body = request.body().toString();
            body = sanitizeBodyParameters(body);
        }

        // Build the safe representation
        StringBuilder sb = new StringBuilder();
        sb.append(method).append(" ").append(url).append("\n");
        for (String h : sanitizedHeaders)
        {
            sb.append(h).append("\n");
        }
        if (!body.isEmpty())
        {
            sb.append("\n").append(body);
        }

        return new SanitizedRequest(sb.toString(), url, method);
    }

    private String sanitizeHeader(String name, String value)
    {
        String lower = name.toLowerCase();
        if (SENSITIVE_HEADERS.contains(lower) || extraSensitiveHeaders.contains(lower))
        {
            return name + ": [REDACTED: " + lower + "]";
        }
        return name + ": " + value;
    }

    private String sanitizeBodyParameters(String body)
    {
        // Handle JSON body
        if (body.trim().startsWith("{"))
        {
            return sanitizeJsonBody(body);
        }

        // Handle URL-encoded body
        if (body.contains("="))
        {
            return sanitizeUrlEncodedBody(body);
        }

        return body;
    }

    private String sanitizeJsonBody(String json)
    {
        Matcher m = JSON_SENSITIVE_VALUE.matcher(json);
        StringBuffer sb = new StringBuffer();
        while (m.find())
        {
            m.appendReplacement(sb, "\"" + m.group(1) + "\": \"[REDACTED:" + m.group(1) + "]\"");
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private String sanitizeUrlEncodedBody(String body)
    {
        StringBuilder sb = new StringBuilder();
        for (String param : body.split("&"))
        {
            int eqIdx = param.indexOf('=');
            if (eqIdx > 0)
            {
                String name = param.substring(0, eqIdx);
                String lower = name.toLowerCase();
                if (SENSITIVE_PARAM_NAMES.contains(lower) || extraSensitiveParams.contains(lower))
                {
                    sb.append(name).append("=[REDACTED:").append(name).append("]&");
                    continue;
                }
            }
            sb.append(param).append("&");
        }
        if (sb.length() > 0) sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    /**
     * Result of sanitization.
     */
    public static class SanitizedRequest
    {
        private final String safeText;
        private final String url;
        private final String method;

        public SanitizedRequest(String safeText, String url, String method)
        {
            this.safeText = safeText;
            this.url = url;
            this.method = method;
        }

        /** The cleaned request text, safe to send to external API. */
        public String getSafeText() { return safeText; }

        public String getUrl() { return url; }
        public String getMethod() { return method; }
    }
}
