package com.audit.workflow.repository;

import com.audit.workflow.domain.AuditTask;
import com.audit.workflow.dto.ReviewFeedbackRequest;
import com.audit.workflow.support.JsonSupport;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AuditReviewFeedbackRepository {

    private final JdbcTemplate jdbcTemplate;
    private final JsonSupport jsonSupport;

    public AuditReviewFeedbackRepository(JdbcTemplate jdbcTemplate, JsonSupport jsonSupport) {
        this.jdbcTemplate = jdbcTemplate;
        this.jsonSupport = jsonSupport;
    }

    public void insertFeedback(AuditTask task, ReviewFeedbackRequest request, ReviewFeedbackRequest.FeedbackItem item) {
        jdbcTemplate.update("""
                INSERT INTO audit_review_feedback (
                  task_id, task_no, issue_id, review_status, reviewer_id, reviewer_name,
                  feedback_content, corrected_issue, source_system, create_time
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
                """,
                task.getTaskId(),
                task.getTaskNo(),
                item.getIssueId(),
                value(item.getReviewStatus()),
                value(request.getReviewerId()),
                value(request.getReviewerName()),
                item.getFeedbackContent(),
                jsonSupport.toJson(item.getCorrectedIssue()),
                value(request.getSourceSystem()));
    }

    private String value(String value) {
        return value == null ? "" : value;
    }
}
