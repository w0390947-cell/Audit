package com.ruoyi.system.exception;

public class AuditWorkflowException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public AuditWorkflowException(String message)
    {
        super(message);
    }

    public AuditWorkflowException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
