package com.ruoyi.system.exception;

/**
 * FastGPT 审核异常
 *
 * @author ruoyi
 */
public class FastGptAuditException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final ErrorCode errorCode;

    /**
     * 错误码枚举
     */
    public enum ErrorCode
    {
        DISABLED("FastGPT功能未启用"),
        CONFIG_MISSING("FastGPT配置缺失"),
        REPORT_URL_EMPTY("报告文件URL为空"),
        HTTP_401("API Key无效或已过期"),
        HTTP_404("FastGPT服务地址不存在"),
        HTTP_TIMEOUT("请求超时"),
        HTTP_ERROR("HTTP请求失败"),
        RESPONSE_EMPTY("响应内容为空"),
        RESPONSE_NOT_JSON("响应不是有效的JSON格式"),
        PROTOCOL_INVALID("响应协议不符合要求"),
        JSON_PARSE_ERROR("JSON解析失败");

        private final String description;

        ErrorCode(String description)
        {
            this.description = description;
        }

        public String getDescription()
        {
            return description;
        }
    }

    public FastGptAuditException(ErrorCode errorCode)
    {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
    }

    public FastGptAuditException(ErrorCode errorCode, String message)
    {
        super(message);
        this.errorCode = errorCode;
    }

    public FastGptAuditException(ErrorCode errorCode, String message, Throwable cause)
    {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode()
    {
        return errorCode;
    }

    @Override
    public String toString()
    {
        return "FastGptAuditException{" +
                "errorCode=" + errorCode +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}
