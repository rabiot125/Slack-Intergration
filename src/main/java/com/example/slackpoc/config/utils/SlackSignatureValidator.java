package com.example.slackpoc.config.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
@Slf4j
@Component
public class SlackSignatureValidator {
    @Value("${slack.signing.secret}")
    private String slackSigningSecret;

    public boolean verifyRequest(String payload, Map<String, List<String>> headers) {
        String slackSignature = getFirstHeaderValue(headers, "x-slack-signature");
        String slackTimestamp = getFirstHeaderValue(headers, "x-slack-request-timestamp");

        if (slackSignature == null || slackTimestamp == null) {
            log.error("Missing required Slack headers");
            return false;
        }

        if (!verifyTimestamp(slackTimestamp)) {
            return false;
        }

        return verifySignature(payload, slackSignature, slackTimestamp);
    }

    private String getFirstHeaderValue(Map<String, List<String>> headers, String headerName) {
        Object value = headers.get(headerName);
        if (value instanceof List) {
            List<String> values = (List<String>) value;
            return (values != null && !values.isEmpty()) ? values.get(0) : null;
        } else if (value instanceof String) {
            return (String) value;
        }
        return null;
    }

    private boolean verifyTimestamp(String slackTimestamp) {
        try {
            long timestamp = Long.parseLong(slackTimestamp);
            long currentTime = System.currentTimeMillis() / 1000;
            if (Math.abs(currentTime - timestamp) > 300) { // 5 min
                log.error("Request timestamp too old");
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            log.error("Invalid timestamp format", e);
            return false;
        }
    }
    private boolean verifySignature(String payload, String slackSignature, String timestamp) {
        try {

            String baseString = String.format("v0:%s:%s", timestamp, payload);

            // Create  SHA256 hash
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(slackSigningSecret.getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);

            String calculatedSignature = "v0=" + Hex.encodeHexString(mac.doFinal(baseString.getBytes()));


            return MessageDigest.isEqual(calculatedSignature.getBytes(), slackSignature.getBytes());
        } catch (Exception e) {
            log.error("Error verifying Slack signature", e);
            return false;
        }
    }
}