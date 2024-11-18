package com.example.slackpoc.repository;

import com.example.slackpoc.model.FeedbackSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<FeedbackSubmission, Long> {
}
