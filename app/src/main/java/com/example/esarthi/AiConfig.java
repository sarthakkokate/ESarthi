package com.example.esarthi;

public class AiConfig {
    /**
     * Active AI model name configured via BuildConfig / local.properties.
     * Default: openai/gpt-oss-20b (verified active on Groq API)
     */
    public static final String MODEL_NAME = BuildConfig.AI_MODEL;

    /**
     * Groq OpenAI-compatible Chat Completions API endpoint.
     */
    public static final String GROQ_URL = BuildConfig.GROQ_API_URL;

    /**
     * Groq API Key configured via BuildConfig / local.properties.
     */
    public static final String API_KEY = BuildConfig.GROQ_API_KEY;

    /**
     * System instructions for the E-Sarthi AI assistant.
     */
    public static final String SYSTEM_PROMPT = "You are E-Sarthi, a helpful, polite, and knowledgeable AI assistant. Provide clear, accurate, and concise responses.";

    /**
     * Friendly error messages displayed to users instead of raw API stack traces.
     */
    public static final String ERROR_SERVICE_UNAVAILABLE = "AI service is temporarily unavailable. Please try again.";
    public static final String ERROR_NETWORK_FAILURE = "Unable to connect to the AI service. Please check your internet connection and try again.";
}
