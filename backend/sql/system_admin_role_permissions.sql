-- ----------------------------
-- 系统管理员角色权限整理脚本
-- ----------------------------
-- 目标：
-- 1. 创建/更新“系统管理员”角色，role_key = system_admin。
-- 2. 系统管理员是业务层面的最高管理员，拥有审核员全部权限和全部 AI 审核业务权限。
-- 3. 系统管理员可管理全部审核任务、AI任务、审核资源、知识库/资源库业务数据。
-- 4. 系统管理员不可管理 RuoYi 底座权限：用户、角色、菜单。
-- 5. 系统管理员对业务数据拥有全部数据范围。
--
-- 注意：
-- 该角色不是 RuoYi 超级管理员，不授予 *:*:*，也不触发 user_id = 1 的超级管理员逻辑。

-- 创建系统管理员角色。
INSERT INTO `sys_role`
(`role_name`, `role_key`, `role_sort`, `data_scope`, `menu_check_strictly`, `dept_check_strictly`,
 `status`, `del_flag`, `create_by`, `create_time`, `remark`)
SELECT '系统管理员', 'system_admin', 2, '1', 1, 1, '0', '0', 'admin', NOW(), '业务层面的系统管理员，拥有全部AI审核业务权限'
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role` r WHERE r.`role_key` = 'system_admin'
);

-- 更新系统管理员角色基础信息，保证重复执行后配置一致。
UPDATE `sys_role`
SET `role_name` = '系统管理员',
    `role_sort` = 2,
    `data_scope` = '1',
    `menu_check_strictly` = 1,
    `dept_check_strictly` = 1,
    `status` = '0',
    `del_flag` = '0',
    `remark` = '业务层面的系统管理员，拥有全部AI审核业务权限'
WHERE `role_key` = 'system_admin';

-- 重建系统管理员菜单权限，确保不会遗留用户、角色、菜单等 RuoYi 底座管理权限。
DELETE rm
FROM `sys_role_menu` rm
INNER JOIN `sys_role` r ON r.`role_id` = rm.`role_id`
WHERE r.`role_key` = 'system_admin';

-- 授予系统管理员权限：
-- 1. 字典只读入口：系统管理目录、字典管理菜单、字典查询按钮。
-- 2. AI审核业务全量权限：审核列表、AI任务、审核资源、审核文件库、常用资源、任务资源全部菜单和按钮。
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT r.`role_id`, m.`menu_id`
FROM `sys_role` r
INNER JOIN `sys_menu` m ON m.`menu_id` IN (
    -- 业务字典只读
    1, 105, 1025,

    -- 审核列表管理全量权限
    2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010,

    -- AI任务管理全量权限
    2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020,

    -- 审核资源与知识库全量业务权限
    2021, 2022, 2023, 2024, 2025, 2026, 2027, 2028, 2029, 2030, 2050,
    2031, 2032, 2033, 2034, 2035, 2036,
    2037, 2038, 2039, 2040, 2041, 2042, 2043,
    2044, 2045, 2046, 2047, 2048
)
WHERE r.`role_key` = 'system_admin'
  AND m.`status` = '0';
