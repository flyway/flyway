# flyway-starrocks

Flyway 数据库方言 — StarRocks 支持。

此模块是 [flyway/flyway](https://github.com/flyway/flyway) 主仓库的一部分，
遵循主仓库的数据库方言规范（包名 `org.flywaydb.database`，继承 `BaseDatabaseType`）。

## 使用方法

1. Flyway 已内置此插件，无需额外安装
2. JDBC URL 格式：`jdbc:mysql://<host>:<port>/<database>`
3. 需要 MySQL Connector/J 驱动（Flyway 已包含）

## 检测机制

StarRocks 使用 MySQL 协议（`jdbc:mysql://`），通过两层检测与 MySQL 区分：

1. **`SELECT version()`** — 某些 StarRocks 版本直接返回包含 `"starrocks"` 的版本字符串
2. **`SELECT @@language`** — 回退检测，StarRocks 返回的路径包含 `/starrocks/`

优先级设为 1（高于 MySQL 的 0），确保 StarRocks 优先匹配。
