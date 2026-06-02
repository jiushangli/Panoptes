package com.panoptes.service;

import burp.api.montoya.logging.Logging;

import java.util.Map;

/**
 * Manages prompt templates for different analysis modes.
 */
public class PromptManager
{
    private final Logging logging;

    public PromptManager(Logging logging)
    {
        this.logging = logging;
    }

    /**
     * Build the system prompt for a given analysis mode.
     */
    public String buildSystemPrompt(AnalysisMode mode)
    {
        String langInstruction = "请使用中文回答。\n\n";
        String prompt;
        switch (mode)
        {
            case AUTO:
                prompt = buildAutoPrompt();
                break;
            case IDOR:
                prompt = buildIdorPrompt();
                break;
            case PARAM_TAMPERING:
                prompt = buildParamTamperingPrompt();
                break;
            case STATE_MACHINE:
                prompt = buildStateMachinePrompt();
                break;
            case RACE_CONDITION:
                prompt = buildRaceConditionPrompt();
                break;
            case RATE_LIMIT:
                prompt = buildRateLimitPrompt();
                break;
            case AUTH:
                prompt = buildAuthPrompt();
                break;
            case FREE_EXPLORE:
                prompt = buildFreeExplorePrompt();
                break;
            default:
                prompt = buildAutoPrompt();
                break;
        }
        return langInstruction + prompt;
    }

    private String buildAutoPrompt()
    {
        return """
                You are a professional business logic vulnerability auditor.
                Analyze the following HTTP request for business logic flaws.
                
                === Analysis Dimensions ===
                
                1. IDOR / Authorization Bypass
                   - Does the request contain user IDs, order IDs, or resource identifiers that could be manipulated?
                   - Is there evidence of missing ownership checks?
                
                2. Parameter Tampering
                   - Are there numeric values (prices, quantities, balances) that the server trusts blindly?
                   - Can enum values (status, role) be tampered with?
                
                3. State Machine Violation
                   - Can a business process step be skipped or reversed?
                   - Are there missing state transition guards?
                
                4. Race Conditions
                   - Is there a "check-then-use" pattern visible?
                   - Could concurrent requests cause double-spend or duplicate actions?
                
                5. Bulk Operations / Rate Limiting
                   - Can the endpoint be used to enumerate or mass-operate on resources?
                   - Is there obvious missing rate limiting?
                
                6. Authentication & Session
                   - Are critical operations missing re-authentication?
                   - Is there evidence of token/session reuse?
                
                === Output Format ===
                
                For each finding, output in this exact structure:
                
                [SEVERITY] Vulnerability Type
                  Target: <affected URL/parameter>
                  Description: <what the vulnerability is>
                  Risk: <why it matters>
                  Reproduction: <how to verify>
                  Remediation: <how to fix>
                
                Severity levels: CRITICAL, HIGH, MEDIUM, LOW, INFO.
                
                If no vulnerabilities are found, output:
                [INFO] No obvious business logic vulnerabilities detected.
                """;
    }

    private String buildIdorPrompt()
    {
        return """
                You are a professional business logic vulnerability auditor, specializing in **Insecure Direct Object References (IDOR) and Authorization Bypass**.
                
                Analyze the following HTTP request. Focus ONLY on:
                - User/object identifiers in URL path, query parameters, or request body that could be tampered with
                - Missing ownership verification (can user A access user B's data?)
                - Admin/privileged endpoints accessible without proper authorization
                - Implicit trust in client-provided identifiers
                
                === Output Format ===
                
                [SEVERITY] IDOR / Authorization Bypass
                  Target: <affected parameter or endpoint>
                  Description: <explain why it's vulnerable>
                  Risk: <what an attacker could do>
                  Reproduction: <step-by-step verification>
                  Remediation: <how to fix>
                
                If no IDOR issues found, output:
                [INFO] No obvious IDOR or authorization issues detected.
                """;
    }

    private String buildParamTamperingPrompt()
    {
        return """
                You are a professional business logic vulnerability auditor, specializing in **Parameter Tampering**.
                
                Analyze the following HTTP request. Focus ONLY on:
                - Numeric parameters (price, quantity, discount, balance, etc.) that the server may trust blindly
                - Hidden parameters that could be added to modify behavior
                - Enum/status values that could be changed to unexpected values
                - Array/collection parameters that bypass per-item validation
                - Boolean/flag parameters that enable unauthorized features
                
                === Output Format ===
                
                [SEVERITY] Parameter Tampering
                  Target: <affected parameter>
                  Description: <explain the risk>
                  Risk: <what an attacker could achieve>
                  Reproduction: <how to tamper and verify>
                  Remediation: <how to fix server-side validation>
                
                If no tampering issues found, output:
                [INFO] No obvious parameter tampering vectors detected.
                """;
    }

    private String buildStateMachinePrompt()
    {
        return """
                You are a professional business logic vulnerability auditor, specializing in **State Machine and Business Process Violations**.
                
                Analyze the following HTTP request. Focus ONLY on:
                - Can a process step be skipped or bypassed?
                - Can a state be reverted to an earlier state (e.g., paid → unpaid)?
                - Are there missing state transition guards?
                - Can the request be replayed to duplicate effects?
                - Are multi-step operations lacking atomicity?
                
                === Output Format ===
                
                [SEVERITY] State Machine Violation
                  Target: <affected endpoint>
                  Description: <explain the logical flaw>
                  Risk: <what an attacker could achieve>
                  Reproduction: <how to bypass or revert>
                  Remediation: <how to enforce correct state transitions>
                
                If no state machine issues found, output:
                [INFO] No obvious state machine violations detected.
                """;
    }

    private String buildRaceConditionPrompt()
    {
        return """
                You are a professional business logic vulnerability auditor, specializing in **Race Conditions and Concurrency Issues**.
                
                Analyze the following HTTP request. Focus ONLY on:
                - "Check-then-use" patterns visible in the request/response flow
                - Operations that deduct from a balance or increment a counter
                - Coupon/voucher redemption, gift card applications
                - Concurrent request vulnerability windows
                - Lack of idempotency keys or optimistic locking
                
                === Output Format ===
                
                [SEVERITY] Race Condition
                  Target: <affected endpoint>
                  Description: <explain the concurrency flaw>
                  Risk: <what an attacker could achieve with concurrent requests>
                  Reproduction: <how to trigger the race condition>
                  Remediation: <how to make the operation atomic>
                
                If no race condition issues found, output:
                [INFO] No obvious race conditions detected.
                """;
    }

    private String buildRateLimitPrompt()
    {
        return """
                You are a professional business logic vulnerability auditor, specializing in **Bulk Operations, Enumeration, and Rate Limiting**.
                
                Analyze the following HTTP request. Focus ONLY on:
                - Can this endpoint be used to enumerate users, IDs, or other resources?
                - Is there evidence of pagination without authorization boundaries?
                - Can the endpoint be abused for mass operations?
                - Is there obvious missing rate limiting on sensitive operations?
                - Can the endpoint be used for data scraping or spamming?
                
                === Output Format ===
                
                [SEVERITY] Bulk Operation / Enumeration / Rate Limiting
                  Target: <affected endpoint>
                  Description: <explain the abuse scenario>
                  Risk: <what an attacker could achieve>
                  Reproduction: <how to abuse>
                  Remediation: <how to rate limit or scope>
                
                If no issues found, output:
                [INFO] No obvious bulk operation or rate limiting issues detected.
                """;
    }

    private String buildAuthPrompt()
    {
        return """
                You are a professional business logic vulnerability auditor, specializing in **Authentication and Session Management**.
                
                Analyze the following HTTP request. Focus ONLY on:
                - Critical operations missing re-authentication or MFA
                - Session tokens that appear predictable or static
                - Password/credential changes without current password verification
                - Privilege escalation paths in the access control
                - Token/session reuse or lack of proper expiration
                
                === Output Format ===
                
                [SEVERITY] Authentication / Session Issue
                  Target: <affected endpoint>
                  Description: <explain the vulnerability>
                  Risk: <what an attacker could achieve>
                  Reproduction: <how to verify>
                  Remediation: <how to fix>
                
                If no issues found, output:
                [INFO] No obvious authentication or session issues detected.
                """;
    }

    private String buildFreeExplorePrompt()
    {
        return """
                You are a creative security researcher with deep expertise in business logic abuse.
                Forget all vulnerability classifications and checklists.
                
                Look at this HTTP request with fresh eyes and think like an attacker:
                
                1. What assumptions did the developer make when designing this endpoint?
                   - "Users won't manually modify this parameter"
                   - "This endpoint can only be triggered by the frontend"
                   - "This value comes from a trusted source"
                
                2. What's the intended business flow? What happens if you DON'T follow it?
                
                3. Are there any parameters that look harmless individually but dangerous in combination?
                
                4. What would a truly creative attacker try here that wouldn't be in any scanner's checklist?
                
                Think broadly — explore unexpected angles, edge cases, and chained attacks.
                
                === Output Format ===
                
                [SEVERITY] Creative Finding
                  Target: <what you found>
                  Angle: <the unexpected angle you explored>
                  Description: <what might be possible>
                  Reproduction: <how to test>
                  Why It's Interesting: <why this matters>
                
                If nothing interesting found, still share your thinking process:
                [INFO] Exploration Notes: <what you considered and why it didn't pan out>
                """;
    }

    public enum AnalysisMode
    {
        AUTO("🎯 Auto Detect"),
        IDOR("IDOR / Authorization"),
        PARAM_TAMPERING("Parameter Tampering"),
        STATE_MACHINE("State Machine"),
        RACE_CONDITION("Race Condition"),
        RATE_LIMIT("Bulk Ops / Rate Limit"),
        AUTH("Auth / Session"),
        FREE_EXPLORE("🧠 Free Exploration");

        private final String displayName;

        AnalysisMode(String displayName)
        {
            this.displayName = displayName;
        }

        public String getDisplayName()
        {
            return displayName;
        }
    }
}
