-- ----------------------------
-- 审核员角色权限整理脚本
-- ----------------------------
-- 目标：
-- 1. 创建/更新“审核员”角色，role_key = auditor。
-- 2. 审核员可审核系统中提交的所有审核任务，不做任务分配/领取限制。
-- 3. 审核员可查看、审核、退回、触发 AI 重新分析、导出审核结果。
-- 4. 审核员不可删除审核任务、审核资源、AI任务。
-- 5. 审核员可只读查看审核依据、常用文件资源、任务文件资源，不可维护知识库。
--
-- 注意：
-- 当前 audit:review:edit 会修改审核任务基础信息，不授予审核员。
-- 审核员填写审核意见/处理结论，当前通过 audit:ai:review 与 audit:asset:review 承载。

-- 创建审核员角色。
INSERT INTO `sys_role`
(`role_name`, `role_key`, `role_sort`, `data_scope`, `menu_check_strictly`, `dept_check_strictly`,
 `status`, `del_flag`, `create_by`, `create_time`, `remark`)
SELECT '审核员', 'auditor', 3, '1', 1, 1, '0', '0', 'admin', NOW(), '可审核系统中提交的全部审核任务'
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role` r WHERE r.`role_key` = 'auditor'
);

-- 更新审核员角色基础信息，保证重复执行后配置一致。
UPDATE `sys_role`
SET `role_name` = '审核员',
    `role_sort` = 3,
    `data_scope` = '1',
    `menu_check_strictly` = 1,
    `dept_check_strictly` = 1,
    `status` = '0',
    `del_flag` = '0',
    `remark` = '可审核系统中提交的全部审核任务'
WHERE `role_key` = 'auditor';

-- 重建审核员菜单权限，确保不会遗留删除、分配、知识库维护等高风险权限。
DELETE rm
FROM `sys_role_menu` rm
INNER JOIN `sys_role` r ON r.`role_id` = rm.`role_id`
WHERE r.`role_key` = 'auditor';

-- 授予审核员菜单和按钮权限。
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT r.`role_id`, m.`menu_id`
FROM `sys_role` r
INNER JOIN `sys_menu` m ON m.`menu_id` IN (
    -- 审核列表：查看、查询、详情、历史、导出。不授予 add/edit/remove/changeStatus。
    2001, 2002, 2003, 2007, 2008, 2009,

    -- AI任务：查看、查询、详情、导出、人工审核、重新分析。不授予状态切换、提优先级、删除。
    2011, 2012, 2013, 2014, 2015, 2018, 2020,

    -- 审核资源：查看、查询、详情、导出、批量下载、打包、重新上传、审核。不授予分配、删除。
    2021, 2022, 2023, 2024, 2025, 2028, 2029, 2030, 2050,

    -- 审核依据/资源库只读：文件库、常用资源、任务资源的列表和查询。不授予新增、修改、删除、归类。
    2031, 2032, 2033, 2037, 2038, 2044, 2045
)
WHERE r.`role_key` = 'auditor'
  AND m.`status` = '0';
