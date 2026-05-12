package com.audit.workflow.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReviewFeedbackRequest {

    @JsonAlias("task_no")
    private String taskNo;
    @JsonAlias("reviewer_id")
    private String reviewerId;
    @JsonAlias("reviewer_name")
    private String reviewerName;
    @JsonAlias("source_system")
    private String sourceSystem;
    private List<FeedbackItem> feedbacks = new ArrayList<>();

    public String getTaskNo() {
        return taskNo;
    }

    public void setTaskNo(String taskNo) {
        this.taskNo = taskNo;
    }

    public String getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(String reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public List<FeedbackItem> getFeedbacks() {
        return feedbacks;
    }

    public void setFeedbacks(List<FeedbackItem> feedbacks) {
        this.feedbacks = feedbacks;
    }

    public static class FeedbackItem {
        @JsonAlias("issue_id")
        private Long issueId;
        @JsonAlias("review_status")
        private String reviewStatus;
        @JsonAlias("feedback_content")
        private String feedbackContent;
        @JsonAlias("corrected_issue")
        private Map<String, Object> correctedIssue;

        public Long getIssueId() {
            return issueId;
        }

        public void setIssueId(Long issueId) {
            this.issueId = issueId;
        }

        public String getReviewStatus() {
            return reviewStatus;
        }

        public void setReviewStatus(String reviewStatus) {
            this.reviewStatus = reviewStatus;
        }

        public String getFeedbackContent() {
            return feedbackContent;
        }

        public void setFeedbackContent(String feedbackContent) {
            this.feedbackContent = feedbackContent;
        }

        public Map<String, Object> getCorrectedIssue() {
            return correctedIssue;
        }

        public void setCorrectedIssue(Map<String, Object> correctedIssue) {
            this.correctedIssue = correctedIssue;
        }
    }
}
