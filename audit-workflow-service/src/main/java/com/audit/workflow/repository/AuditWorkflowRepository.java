package com.audit.workflow.repository;

import com.audit.workflow.domain.AuditWorkflow;
import com.audit.workflow.domain.AuditWorkflowNode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class AuditWorkflowRepository {

    private final JdbcTemplate jdbcTemplate;

    public AuditWorkflowRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<AuditWorkflow> findEnabledWorkflows() {
        return jdbcTemplate.query("""
                SELECT * FROM audit_workflow
                WHERE enabled = 1
                ORDER BY workflow_id
                """, this::mapWorkflow);
    }

    public Optional<AuditWorkflow> findByCode(String workflowCode) {
        List<AuditWorkflow> workflows = jdbcTemplate.query("""
                SELECT * FROM audit_workflow
                WHERE workflow_code = ?
                """, this::mapWorkflow, workflowCode);
        return workflows.stream().findFirst();
    }

    public List<AuditWorkflowNode> findEnabledNodes(String workflowCode) {
        return jdbcTemplate.query("""
                SELECT * FROM audit_workflow_node
                WHERE workflow_code = ? AND enabled = 1
                ORDER BY node_order ASC, node_id ASC
                """, this::mapNode, workflowCode);
    }

    private AuditWorkflow mapWorkflow(ResultSet rs, int rowNum) throws SQLException {
        AuditWorkflow workflow = new AuditWorkflow();
        workflow.setWorkflowId(rs.getLong("workflow_id"));
        workflow.setWorkflowCode(rs.getString("workflow_code"));
        workflow.setWorkflowName(rs.getString("workflow_name"));
        workflow.setDescription(rs.getString("description"));
        workflow.setInputSchema(rs.getString("input_schema"));
        workflow.setKnowledgeBinding(rs.getString("knowledge_binding"));
        workflow.setRetrievalConfig(rs.getString("retrieval_config"));
        workflow.setPromptTemplate(rs.getString("prompt_template"));
        workflow.setOutputSchema(rs.getString("output_schema"));
        workflow.setEnabled(rs.getBoolean("enabled"));
        workflow.setCreateTime(rs.getTimestamp("create_time") == null ? null : rs.getTimestamp("create_time").toLocalDateTime());
        workflow.setUpdateTime(rs.getTimestamp("update_time") == null ? null : rs.getTimestamp("update_time").toLocalDateTime());
        return workflow;
    }

    private AuditWorkflowNode mapNode(ResultSet rs, int rowNum) throws SQLException {
        AuditWorkflowNode node = new AuditWorkflowNode();
        node.setNodeId(rs.getLong("node_id"));
        node.setWorkflowCode(rs.getString("workflow_code"));
        node.setNodeCode(rs.getString("node_code"));
        node.setNodeName(rs.getString("node_name"));
        node.setNodeType(rs.getString("node_type"));
        node.setNodeOrder(rs.getInt("node_order"));
        node.setNodeConfig(rs.getString("node_config"));
        node.setEnabled(rs.getBoolean("enabled"));
        node.setCreateTime(rs.getTimestamp("create_time") == null ? null : rs.getTimestamp("create_time").toLocalDateTime());
        node.setUpdateTime(rs.getTimestamp("update_time") == null ? null : rs.getTimestamp("update_time").toLocalDateTime());
        return node;
    }
}
