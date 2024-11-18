package com.example.slackpoc.service;

import com.example.slackpoc.model.FeedbackSubmission;

import java.util.List;
import java.util.Map;

public interface FeedbackService {
//    void saveFeedback (String payload) throws Exception;

    void saveFeedback(String payload, Map<String, List<String>> headers) throws Exception;

    List<FeedbackSubmission> getAllFeedback();
}
