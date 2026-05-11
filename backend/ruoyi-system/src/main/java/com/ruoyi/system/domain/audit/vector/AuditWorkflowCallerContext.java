package com.ruoyi.system.domain.audit.vector;

import com.fasterxml.jackson.annotation.JsonAlias;

public class AuditWorkflowCallerContext
{
    public static final String PERMISSION_MODE_EXPLICIT_SCOPE = "explicit_scope";

    public static final String PERMISSION_MODE_CALLER_USER = "caller_user";

    public static final String PERMISSION_MODE_WORKFLOW_SERVICE = "workflow_service";

    @JsonAlias("user_id")
    private String userId;

    @JsonAlias("dept_id")
    private String deptId;

    @JsonAlias("tenant_id")
    private String tenantId;

    @JsonAlias("role_ids")
    private String[] roleIds;

    @JsonAlias("permission_mode")
    private String permissionMode;

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public String getDeptId()
    {
        return deptId;
    }

    public void setDeptId(String deptId)
    {
        this.deptId = deptId;
    }

    public String getTenantId()
    {
        return tenantId;
    }

    public void setTenantId(String tenantId)
    {
        this.tenantId = tenantId;
    }

    public String[] getRoleIds()
    {
        return roleIds;
    }

    public void setRoleIds(String[] roleIds)
    {
        this.roleIds = roleIds;
    }

    public String getPermissionMode()
    {
        return permissionMode;
    }

    public void setPermissionMode(String permissionMode)
    {
        this.permissionMode = permissionMode;
    }
}
