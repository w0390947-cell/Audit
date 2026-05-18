-- ----------------------------
-- 超级管理员全权限整理脚本
-- ----------------------------
-- 目标：
-- 1. 保留 RuoYi 既有超级管理员机制：user_id = 1 在代码层拥有 *:*:* 全权限。
-- 2. 同时把 role_id = 1 的“超级管理员”角色在数据库层显式关联全部菜单和按钮权限。
-- 3. 脚本保持幂等，可在菜单初始化和业务菜单迁移后重复执行。

-- 确保超级管理员角色存在且启用，数据权限为全部数据权限。
UPDATE `sys_role`
SET `role_name` = '超级管理员',
    `role_key` = 'admin',
    `role_sort` = 1,
    `data_scope` = '1',
    `menu_check_strictly` = 1,
    `dept_check_strictly` = 1,
    `status` = '0',
    `del_flag` = '0',
    `remark` = '超级管理员'
WHERE `role_id` = 1;

-- 确保超级管理员账号存在时处于正常状态。
UPDATE `sys_user`
SET `status` = '0',
    `del_flag` = '0'
WHERE `user_id` = 1;

-- 确保 user_id = 1 绑定超级管理员角色。
INSERT INTO `sys_user_role` (`user_id`, `role_id`)
SELECT 1, 1
WHERE EXISTS (
    SELECT 1 FROM `sys_user` u WHERE u.`user_id` = 1
)
AND EXISTS (
    SELECT 1 FROM `sys_role` r WHERE r.`role_id` = 1
)
AND NOT EXISTS (
    SELECT 1 FROM `sys_user_role` ur WHERE ur.`user_id` = 1 AND ur.`role_id` = 1
);

-- 显式授予超级管理员角色全部菜单、目录、按钮权限。
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, m.`menu_id`
FROM `sys_menu` m
WHERE NOT EXISTS (
    SELECT 1
    FROM `sys_role_menu` rm
    WHERE rm.`role_id` = 1
      AND rm.`menu_id` = m.`menu_id`
);
