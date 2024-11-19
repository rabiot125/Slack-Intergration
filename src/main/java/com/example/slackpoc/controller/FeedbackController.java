package com.example.slackpoc.controller;

import com.example.slackpoc.model.FeedbackSubmission;
import com.example.slackpoc.service.FeedbackService;
import com.example.slackpoc.service.SlackService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/feedback")
public class FeedbackController {
    private final FeedbackService feedbackService;
    private final SlackService slackService;


    public FeedbackController(FeedbackService feedbackService, SlackService slackService) {
        this.feedbackService = feedbackService;
        this.slackService = slackService;
    }

    @GetMapping
    public ResponseEntity<List<FeedbackSubmission>> getAllFeedback() {
        return ResponseEntity.ok(feedbackService.getAllFeedback());
    }

    /*@GetMapping("/user/{userId}")
    public ResponseEntity<List<FeedbackSubmission>> getFeedbackByUser(@PathVariable String userId) {
        return ResponseEntity.ok(feedbackService.getFeedbackByUser(userId));
    }*/

    @PostMapping("/send-form/{userId}")
    public ResponseEntity<String> sendFeedbackForm(@PathVariable String userId) {
        try {
            slackService.sendFormToUser(userId);
            return ResponseEntity.ok("Form sent successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error sending form: " + e.getMessage());
        }
    }
}
