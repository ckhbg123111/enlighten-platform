## SQL 版本控制（Flyway）使用说明

本文面向后续开发者，说明本项目如何使用 Flyway 管理数据库版本、如何新增迁移脚本、在不同环境如何运行，以及关键注意事项与故障排查。

### 总览
- **集成模块**：`enlighten-platform-web`
- **迁移脚本目录**：`enlighten-platform-web/src/main/resources/db/migration`
- **核心配置**：位于 `enlighten-platform-web/src/main/resources/application.properties`
  - `spring.flyway.enabled=true`
  - `spring.flyway.locations=classpath:db/migration`
  - `spring.flyway.baseline-on-migrate=true`
  - `spring.flyway.validate-on-migrate=true`
  - `spring.flyway.out-of-order=false`
- **首次建库**：默认关闭应用内的 `DatabaseInitializer`（仅建库）。推荐使用仓库的 `scripts/init_db.ps1` 完成数据库创建，随后由 Flyway 执行表结构与数据迁移。

### 启动时行为
- 应用启动后，Flyway 会扫描 `classpath:db/migration` 下按版本命名的脚本（如 `V1__init.sql`、`V2__add_user_fields.sql`、`V3__add_tenant_id.sql`），并在目标数据源对应库上执行未执行过的迁移。
- `baseline-on-migrate=true`：当目标库非空但没有 Flyway 历史表时，会自动创建基线，从而避免与历史手工建表冲突。
- `validate-on-migrate=true`：校验已执行迁移的校验和，若历史脚本被修改将导致启动失败（见“常见问题与排查”）。
- `out-of-order=false`：按版本号严格顺序执行，禁止乱序补历史版本。

### 本地开发与环境准备
- 建议使用环境变量或本地 profile 覆盖数据源，避免提交明文凭证：
  - `SPRING_DATASOURCE_URL`
  - `SPRING_DATASOURCE_USERNAME`
  - `SPRING_DATASOURCE_PASSWORD`
- 首次启动前建库（二选一）：
  - 使用 PowerShell 脚本（推荐，Windows 友好）：
    ```powershell
    # 在仓库根目录执行
    powershell -ExecutionPolicy Bypass -File .\scripts\init_db.ps1 `
      -Host 127.0.0.1 -Port 3306 -User root -Password your_pass -Database enlighten_platform
    ```
  - 临时开启应用内建库（仅建库，不建表；建表交由 Flyway）：
    - 将 `enlighten-platform-web/src/main/resources/application.properties` 中 `app.database-initializer.enabled=true`
    - 启动一次应用完成建库后，再改回 `false`，避免与 Flyway 冲突
- 启动应用（示例）：
  ```bash
  # Windows
  mvnw.cmd -pl enlighten-platform-web -am spring-boot:run
  # 或打包后运行
  mvnw.cmd -DskipTests package
  java -jar enlighten-platform-web\target\*.jar
  ```

### 如何新增一个迁移脚本
- 在 `enlighten-platform-web/src/main/resources/db/migration` 下新增文件，命名规范：`V{版本号}__{下划线描述}.sql`，例如：`V4__add_order_table.sql`
- 编写 SQL 建议遵循以下规范（V2、V3 已示范）：
  - **幂等性/可重复执行**：通过 `INFORMATION_SCHEMA` 判断列/索引是否存在，再按需执行 DDL，兼容 MySQL 5.7/8.0
    ```sql
    -- 添加列示例（若不存在）
    SET @col_exists := (
      SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'tenant_id'
    );
    SET @ddl := IF(@col_exists = 0,
      'ALTER TABLE `user` ADD COLUMN `tenant_id` BIGINT NULL DEFAULT NULL COMMENT ''租户ID''',
      'SELECT 1'
    );
    PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
    ```
  - **避免破坏性变更**：修改列类型、删除列、批量数据清洗需评估影响与停机窗口，必要时拆分为多步变更。
  - **数据初始化**：需要初始化数据时，使用 `WHERE NOT EXISTS` 方式保证幂等。
  - **事务性与批量**：MySQL 的 DDL 多为隐式提交，尽量保证脚本“可中断可恢复”；大数据回填分批进行。
- 提交规范：
  - 每次变更新建 `V{n}` 文件；**不要修改**已合入并执行过的历史迁移脚本。
  - 在 PR 里标明该版本脚本的变更目的与对线上影响。

### 与应用内建库的关系
- 组件：`com.zhongjia.web.config.DatabaseInitializer`
  - 开关：`app.database-initializer.enabled`（默认 `false`）
  - 作用：仅在目标库不存在时执行“建库”（`CREATE DATABASE ...`），不负责建表；表结构统一由 Flyway 迁移完成。
  - 使用建议：仅用于本地或特殊环境首次初始化；生产环境推荐 DBA 建库或使用运维脚本。

### 版本冲突与合并策略
- 本项目配置 `out-of-order=false`，要求严格版本递增。多人并行开发时：
  - 若出现版本号冲突（两个分支都写了 `V4__...`），后合并的分支需将自己的版本号改为更大的下一个号（如改成 `V5__...`）。
  - 避免往回插队补低版本脚本，否则会被拒绝执行。
- `validate-on-migrate=true`：一旦历史脚本被修改，启动会失败。正确做法：
  - 不修改已执行脚本；如需修复，创建“修复向前”的新版本脚本（fix-forward）。
  - 不手工修改 `flyway_schema_history` 或强行覆盖校验和（除非非常清楚后果并经过评审）。

### 多环境与权限要求
- 确保应用使用的数据库用户具备执行迁移所需权限（至少：`CREATE/ALTER/INDEX/INSERT/UPDATE` 等）。
- 使用环境变量或外部化配置覆盖数据源，避免将凭证硬编码进 `application.properties` 并提交仓库：
  - `SPRING_DATASOURCE_URL=jdbc:mysql://<host>:<port>/<db>?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true`
  - `SPRING_DATASOURCE_USERNAME=...`
  - `SPRING_DATASOURCE_PASSWORD=...`

### 验证迁移是否成功
- 查看控制台日志，Flyway 会打印迁移计划与结果。
- 检查历史表：
  ```sql
  SELECT * FROM flyway_schema_history ORDER BY installed_rank;
  ```
- 确认目标表/列/索引已按预期创建或变更。

### 常见问题与排查
- **Unknown database '<db>'**：目标库不存在
  - 先执行 `scripts/init_db.ps1` 或临时开启 `app.database-initializer.enabled=true` 完成建库。
- **权限不足（CREATE/ALTER 被拒）**：提升数据库用户权限或让 DBA 协助。
- **Checksum mismatch（校验和不匹配）**：有人修改了历史脚本。不要修旧脚本，改为新增修复脚本。
- **Versioned migration rejected due to outOfOrder=false**：有人提交了更低版本号的脚本。将版本号改为更大的数并重试。
- **重复对象错误（如列已存在）**：脚本不幂等。参考 V2/V3 写法使用 `INFORMATION_SCHEMA + PREPARE` 做条件化 DDL。

### 现有迁移脚本一览
- `V1__init.sql`：创建 `enlighten_platform` 库与基础 `user` 表，并插入示例数据（包含 `CREATE DATABASE IF NOT EXISTS` 与 `USE`）。若 DB 已存在且基线生效，会跳过重复初始化。
- `V2__add_user_fields.sql`：为 `user` 表新增 `role`、`last_login_time`，并添加 `idx_role` 索引，采用幂等写法。
- `V3__add_tenant_id.sql`：为 `user` 表新增 `tenant_id` 及 `idx_tenant_id` 索引，同样为幂等写法。

### 最佳实践清单
- 新增/修改表结构只能通过新建 `V{n}__*.sql` 实现，严禁改历史迁移。
- 迁移脚本尽量幂等，并兼容 MySQL 5.7/8.0。
- 大规模数据迁移/回填要可中断、可恢复，必要时拆分多步。
- 并行开发时尽早确定版本号，避免合并冲突。
- 生产变更前先在测试库验证，确保迁移耗时和影响可接受。
- 数据源凭证不入库；用环境变量或配置中心管理。


