-- Example account for local development only.
-- username: demo
-- password: 123456

INSERT INTO `t_group_1` (`id`, `gid`, `name`, `username`, `sort_order`, `create_time`, `update_time`, `del_flag`)
VALUES (2026072700000000001, 'demo-group', '默认分组', 'demo', 0, '2026-07-27 00:00:00', '2026-07-27 00:00:00', 0);

INSERT INTO `t_user_1` (`id`, `username`, `password`, `real_name`, `phone`, `mail`, `deletion_time`, `create_time`, `update_time`, `del_flag`)
VALUES (2026072700000000002, 'demo', '123456', 'Local Demo', NULL, NULL, NULL, '2026-07-27 00:00:00', '2026-07-27 00:00:00', 0);
