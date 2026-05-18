package com.ruoyi.system.service.audit.support;

import java.util.List;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

public final class AuditBusinessPermissionUtils
{
    public static final String ROLE_COMMON = "common";

    private AuditBusinessPermissionUtils()
    {
    }

    public static boolean isCommonSubmitterOnly()
    {
        try
        {
            if (SecurityUtils.isAdmin())
            {
                return false;
            }
            LoginUser loginUser = SecurityUtils.getLoginUser();
            if (loginUser == null || loginUser.getUser() == null)
            {
                return false;
            }
            List<SysRole> roles = loginUser.getUser().getRoles();
            if (roles == null || roles.isEmpty())
            {
                return false;
            }
            boolean hasCommonRole = false;
            for (SysRole role : roles)
            {
                if (role == null || StringUtils.isBlank(role.getRoleKey()))
                {
                    continue;
                }
                if (ROLE_COMMON.equals(role.getRoleKey()))
                {
                    hasCommonRole = true;
                }
                else
                {
                    return false;
                }
            }
            return hasCommonRole;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public static String getCurrentUsername()
    {
        try
        {
            return SecurityUtils.getUsername();
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
