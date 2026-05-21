package com.audit.workflow.enums;

public final class NodeType {

    public static final String INPUT_VALIDATE = "INPUT_VALIDATE";
    public static final String FILE_PARSE = "FILE_PARSE";
    public static final String TEXT_SPLIT = "TEXT_SPLIT";
    public static final String BASIS_FILE_PARSE = "BASIS_FILE_PARSE";
    public static final String UPLOADED_BASIS_MATCH = "UPLOADED_BASIS_MATCH";
    public static final String KNOWLEDGE_RETRIEVE = "KNOWLEDGE_RETRIEVE";
    public static final String AI_AUDIT = "AI_AUDIT";
    public static final String RESULT_VALIDATE = "RESULT_VALIDATE";
    public static final String SUMMARY_GENERATE = "SUMMARY_GENERATE";
    public static final String RESULT_SAVE = "RESULT_SAVE";
    public static final String CALLBACK = "CALLBACK";

    private NodeType() {
    }
}
