package com.example.slackpoc.service.impl;

import com.example.slackpoc.Exceptions.FeedbackProcessingException;
import com.example.slackpoc.model.FeedbackSubmission;
import com.example.slackpoc.repository.FeedbackRepository;
import com.example.slackpoc.service.FeedbackService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slack.api.bolt.App;
import com.slack.api.bolt.request.Request;
import com.slack.api.bolt.request.RequestHeaders;
import com.slack.api.bolt.request.builtin.BlockActionRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.bolt.util.SlackRequestParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class FeedbackServiceImpl implements FeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final ObjectMapper objectMapper;
    private final App slackApp;

    public FeedbackServiceImpl(FeedbackRepository feedbackRepository, ObjectMapper objectMapper, App slackApp) {
        this.feedbackRepository = feedbackRepository;
        this.objectMapper = objectMapper;
        this.slackApp = slackApp;
        initializeBlockActionHandler();
    }

    private void initializeBlockActionHandler() {
        slackApp.blockAction("submit_form", (req, ctx) -> {
            try {
                FeedbackSubmission submission = processFeedbackSubmission(req);
                feedbackRepository.save(submission);
                ctx.ack();
                return Response.ok(ctx.respond("Thank you for your feedback!"));
            } catch (Exception e) {
                log.error("Error processing feedback submission", e);
                ctx.ack();
                throw new FeedbackProcessingException("Failed to process feedback", e);
            }
        });
    }

    @Override
    public void saveFeedback(String payload, Map<String, List<String>> headers) throws Exception {
        try {


            if (payload == null || payload.isEmpty()) {
                throw new IllegalArgumentException("Payload cannot be null or empty");
            }

            // Create headers map with all required Slack headers
            Map<String, List<String>> headersMap = new HashMap<>();
            headersMap.put("Content-Type", Arrays.asList("application/json"));

            // Add important Slack verification headers
            if (headers.containsKey("x-slack-signature")) {
                Object headerValue = headers.get("x-slack-signature");
                if (headerValue instanceof List) {
                    headersMap.put("x-slack-signature", Collections.singletonList(((List<String>) headerValue).get(0)));
                } else if (headerValue instanceof String) {
                    headersMap.put("x-slack-signature", Collections.singletonList((String) headerValue));
                }
            }

            if (headers.containsKey("x-slack-request-timestamp")) {
                Object headerValue = headers.get("x-slack-request-timestamp");
                if (headerValue instanceof List) {
                    headersMap.put("x-slack-request-timestamp", Collections.singletonList(((List<String>) headerValue).get(0)));
                } else if (headerValue instanceof String) {
                    headersMap.put("x-slack-request-timestamp", Collections.singletonList((String) headerValue));
                }
            }


            // Construct request headers
            RequestHeaders requestHeaders = new RequestHeaders(headersMap);

            // Build HTTP request
            SlackRequestParser.HttpRequest httpRequest = SlackRequestParser.HttpRequest.builder()
                    .headers(requestHeaders)
                    .requestBody(payload)
                    .build();

            log.info("Constructed Slack HTTP request: {}", httpRequest);

            // Initialize parser with config validation
            if (slackApp.config() == null || slackApp.config().getSigningSecret() == null) {
                throw new IllegalStateException("Slack app configuration is not properly initialized");
            }

            SlackRequestParser requestParser = new SlackRequestParser(slackApp.config());

            // Parse the request
            Request slackRequest;
            try {
                slackRequest = requestParser.parse(httpRequest);
                log.info("Parsed Slack request type: {}", slackRequest.getRequestType());
                log.info("Parsed Slack request body: {}", slackRequest.getRequestBodyAsString());
            } catch (Exception e) {
                log.error("Failed to parse Slack request", e);
                throw new FeedbackProcessingException("Failed to parse Slack request: " + e.getMessage());
            }

            // Process the request
            Response response;
            try {
                response = slackApp.run(slackRequest);
                log.info("Raw Slack API response: {}", response.getBody());
            } catch (Exception e) {
                log.error("Error during Slack API call", e);
                throw new FeedbackProcessingException("Failed to process request through Slack API", e);
            }

            if (response.getStatusCode() != 200) {
                log.error("Slack API error - Status: {}, Body: {}", response.getStatusCode(), response.getBody());
                throw new FeedbackProcessingException("Failed to process Slack request: " + response.getBody());
            }

            log.info("Feedback processed successfully");

        } catch (Exception e) {
            log.error("Error processing Slack payload", e);
            throw new FeedbackProcessingException("Failed to process Slack payload", e);
        }
    }

    private FeedbackSubmission processFeedbackSubmission(BlockActionRequest req) {
        String userId = req.getPayload().getUser().getId();
        var stateValues = req.getPayload().getState().getValues();

        String support = stateValues.get("support_block")
                .get("support_select")
                .getSelectedOption()
                .getText()
                .getText();

        String feedback = stateValues.get("feedback_block")
                .get("feedback_input")
                .getValue();

        String expectedDate = stateValues.get("date_required_block")
                .get("date_picker").getSelectedDate();

        String priority = stateValues.get("priority_block")
                .get("priority_select")
                .getSelectedOption()
                .getText()
                .getText();

        FeedbackSubmission submission = new FeedbackSubmission();
        submission.setUserId(userId);
        submission.setDepartment(support);
        submission.setFeedback(feedback);
        submission.setExpectedDate(LocalDate.parse(expectedDate));
        submission.setPriority(priority);
        submission.setSubmissionTime(LocalDateTime.now());

        return submission;
    }
    @Override
    public List<FeedbackSubmission> getAllFeedback() {
        return feedbackRepository.findAll();
    }
}

