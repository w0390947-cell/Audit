-- ----------------------------
-- 普通用户角色权限整理脚本
-- ----------------------------
-- 目标：
-- 1. 将 RuoYi 默认 common 角色收窄为业务普通用户/资料提交人员。
-- 2. 普通用户只能提交待审核任务，并在任务被退回后重新上传/修改本人资料。
-- 3. 普通用户可以往审核文件库上传公共审核依据/常用文件。
-- 4. 普通用户不具备系统管理、系统监控、系统工具、审核、删除、导出、AI任务管理等权限。
--
-- 注意：
-- “只能查看和维护本人提交的任务”需要配合后端服务层校验，本脚本只处理 RuoYi 菜单/按钮权限。

-- 更新普通用户角色基础信息。
UPDATE `sys_role`
SET `role_name` = '普通用户',
    `role_sort` = 4,
    `data_scope` = '5',
    `menu_check_strictly` = 1,
    `dept_check_strictly` = 1,
    `status` = '0',
    `del_flag` = '0',
    `remark` = '普通业务用户，仅可提交本人审核任务和上传公共审核依据文件'
WHERE `role_key` = 'common';

-- 重建 common 角色菜单权限，清除 RuoYi 底座管理、监控、工具和多余业务管理权限。
DELETE rm
FROM `sys_role_menu` rm
INNER JOIN `sys_role` r ON r.`role_id` = rm.`role_id`
WHERE r.`role_key` = 'common';

-- 授予普通用户最小业务权限：
-- 1. 审核任务：查看本人任务列表、查询本人任务、新增任务、退回后修改本人任务。
-- 2. 审核文件库：浏览文件库、查询公共常用文件、上传公共常用文件。
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT r.`role_id`, m.`menu_id`
FROM `sys_role` r
INNER JOIN `sys_menu` m ON m.`menu_id` IN (
    -- 提交待审核任务
    2001, 2002, 2003, 2004, 2005,

    -- 审核文件库上传公共审核依据/常用文件
    2031, 2032, 2033, 2037, 2038, 2039
)
WHERE r.`role_key` = 'common'
  AND m.`status` = '0';
