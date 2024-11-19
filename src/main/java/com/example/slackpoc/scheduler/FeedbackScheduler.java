package com.example.slackpoc.scheduler;

import com.example.slackpoc.service.SlackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FeedbackScheduler {
    @Value("${scheduled.user-id}")
    private String userId;
    private final SlackService slackService;

    public FeedbackScheduler(SlackService slackService) {
        this.slackService = slackService;

    }
    @Scheduled(cron = "0 */5 * * * ?") // Run every 5 minutes
    private void sendUserForm() {
        try {
            slackService.sendFormToUser(userId);
            log.info("Scheduling the tasks here -----");
        } catch (Exception e) {
            System.err.println("Error in scheduled job: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
