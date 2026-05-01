package se.iths.lw.labb1integrationmotgooglegemini.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Service
public class GeminiService {
    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);
    private final WebClient webClient;
    private final String apiKey;

    public GeminiService(@Value("${GOOGLE_API_KEY:#{null}}") String apiKey) {
        String resolvedKey = apiKey;
        if (resolvedKey == null || resolvedKey.isBlank()) {
            resolvedKey = System.getenv("GOOGLE_API_KEY");
        }

        this.apiKey = resolvedKey;

        if (this.apiKey == null || this.apiKey.isBlank()) {
            log.error("❌ GOOGLE_API_KEY is not configured!");
        } else {
            log.info("✅ GeminiService initialized with API Key: {}...",
                    this.apiKey.substring(0, Math.min(8, this.apiKey.length())));
        }

        this.webClient = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10MB 内存限制
                .build();
    }

    public String askGemini(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            log.error("Cannot call Gemini API: No API Key configured");
            return "❌ API-nyckel saknas. Vänligen kontakta administratören.";
        }

        if (prompt == null || prompt.trim().isEmpty()) {
            return "❌ Frågan kan inte vara tom.";
        }

        log.info("Sending request to Gemini API. Prompt length: {}", prompt.length());

        Map<String, Object> body = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                }
        );

        try {
            // 添加超时控制和更好的错误处理
            Map response = webClient.post()
                    .uri(uri -> uri
                            .path("/models/gemini-3-flash-preview:generateContent")
                            .queryParam("key", apiKey)
                            .build())
                    .bodyValue(body)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
                        log.error("Gemini API error status: {}", clientResponse.statusCode());
                        return clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("Error body: {}", errorBody);
                                    return Mono.error(new RuntimeException("Gemini API returned " + clientResponse.statusCode() + ": " + errorBody));
                                });
                    })
                    .bodyToMono(Map.class)
                    .block(Duration.ofSeconds(30));

            if (response == null) {
                log.error("Received null response from Gemini API");
                return "❌ Fick inget svar från Gemini API (null response).";
            }


            return parseResponse(response);

        } catch (WebClientResponseException.Forbidden e) {
            log.error("❌ 403 Forbidden - API Key issue or billing not enabled", e);
            return "❌ Åtkomst nekad (403). Kontrollera:\n" +
                    "1. Att API-nyckeln är korrekt\n" +
                    "2. Att Generative Language API är aktiverad\n" +
                    "3. Att fakturering är konfigurerad (om nödvändigt)";
        } catch (WebClientResponseException.TooManyRequests e) {
            log.error("❌ 429 Too Many Requests - Rate limit exceeded", e);
            return "❌ För många förfrågningar. Vänta en stund och försök igen.";
        } catch (Exception e) {
            log.error("Unexpected error calling Gemini API", e);
            return "❌ Ett tekniskt fel uppstod: " + e.getClass().getSimpleName() + " - " + e.getMessage();
        }
    }

    private String parseResponse(Map response) {
        try {
            // Check candidates
            if (!response.containsKey("candidates")) {
                log.error("Response missing 'candidates' field: {}", response);
                return "❌ Ogiltigt svar från API: saknar 'candidates'.";
            }

            var candidates = (java.util.List<Map<String, Object>>) response.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                // Check if there is any erros
                if (response.containsKey("error")) {
                    Map error = (Map) response.get("error");
                    return "❌ API-fel: " + error.get("message");
                }
                return "❌ Inget svar från AI-modellen (tom lista).";
            }

            var firstCandidate = candidates.get(0);
            if (!firstCandidate.containsKey("content")) {
                // 检查是否有 finishReason 说明为什么没有内容
                String finishReason = (String) firstCandidate.get("finishReason");

                if (finishReason != null) {
                    return "❌ AI-modellen stoppades: " + finishReason;
                }
                return "❌ Ogiltigt svar: saknar 'content'.";
            }

            var content = (Map<String, Object>) firstCandidate.get("content");
            if (!content.containsKey("parts")) {
                return "❌ Ogiltigt svar: saknar 'parts'.";
            }

            var parts = (java.util.List<Map<String, Object>>) content.get("parts");
            if (parts == null || parts.isEmpty()) {
                return "❌ Tomt svar från AI-modellen.";
            }

            var text = (String) parts.get(0).get("text");
            if (text == null || text.trim().isEmpty()) {
                return "⚠️ AI-modellen returnerade ett tomt svar.";
            }

            return text;

        } catch (Exception e) {
            log.error("Error parsing Gemini response", e);
            return "❌ Kunde inte tolka API-svaret: " + e.getMessage();
        }
    }
}


