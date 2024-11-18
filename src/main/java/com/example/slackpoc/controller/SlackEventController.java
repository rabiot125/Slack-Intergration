package com.example.slackpoc.controller;

import com.example.slackpoc.config.utils.SlackSignatureValidator;
import com.example.slackpoc.service.FeedbackService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/slack/events")
public class SlackEventController {
    private final FeedbackService feedbackService;
    @Value("${slack.signing.secret}")
    private String slackSigningSecret;
    private final SlackSignatureValidator signatureVerifier;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping
    public ResponseEntity<String> handleEvent(@RequestBody String payload, @RequestHeader Map<String, List<String>> headers) {
        try {

            // Verify the signature
            if (!signatureVerifier.verifyRequest(payload, headers)) {
                return ResponseEntity.status(401).body("{\"error\":\"unauthorized\"}");
            }

            // Process the verified request
            feedbackService.saveFeedback(payload, headers);

            // Return challenge value for URL verification
            if (payload.contains("\"type\":\"url_verification\"")) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(payload);
                if (node.has("challenge")) {
                    return ResponseEntity.ok(node.get("challenge").asText());
                }
            }

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Error processing feedback event", e);
            return ResponseEntity.status(500)
                    .body("{\"error\":\"internal_server_error\"}");
        }
    }

}




