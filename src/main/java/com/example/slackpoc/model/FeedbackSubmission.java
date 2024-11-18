package com.example.slackpoc.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "feedback_submissions")
public class FeedbackSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String userId;
    private String department;
    @Column(columnDefinition = "TEXT")
    private String feedback;
    private String priority;
    private LocalDate expectedDate;
    private LocalDateTime submissionTime;
}
