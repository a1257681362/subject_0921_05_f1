# EVOPS 基础工作区（连锁眼镜店 · 验光与加工底账）

Spring Boot 骨架项目。

技术栈：Java 8、Spring Boot 2.7、MyBatis-Plus、H2 本地文件数据库、Shiro、Thymeleaf。

## F1 覆盖范围

- **镜片基础信息**：建档、修改、冲销（逻辑删除）、恢复、详情、分页检索。
- **加工单**：建档（主表 + 左右眼处方同事务，要么全部生效要么全部不见）、按加工单号检索。
- **取镜状态流转**：`CREATED → PROCESSING → READY → PICKED`；`CREATED → CANCELLED（冲销）→ 恢复回 CREATED`。
- **度数校验**：球镜 S、柱镜 C 必须 25 度一档（0.25D 的整数倍），轴位 1~180。
- **留痕**：物理删除一律禁止；冲销/恢复/状态流转/建档/修改全部写入 `t_op_log`。
- **每次写留存**：`operator` 操作人、`request_no` 请求号、`biz_time` 业务时间、`version_no` 版本号（乐观锁）。
- **重复提交**：库层唯一约束 `uk_lens_code` / `uk_process_order_no` 兜底，接口返回明确业务码。

## 请求约定

每次写请求建议带头：

| 头 | 说明 |
| --- | --- |
| `X-Operator` | 操作人，缺省 system |
| `X-Request-No` | 请求号，缺省自动生成（幂等排查用） |
| `X-Biz-Time` | 业务时间 `yyyy-MM-ddTHH:mm:ss`，缺省当前时间 |

统一返回结构：`{ "code": "0", "success": true, "message": "成功", "data": ... }`。

主要业务码：`B1101` 镜片编码重复、`B1102` 镜片不存在、`B1201` 加工单号重复、
`B1202` 加工单不存在、`B1203` 状态不允许该操作、`B1204` 度数非 25 度一档、
`B1009` 版本冲突、`A0400` 参数有误。

## 接口一览

- 镜片：`POST /api/lens/create|update|revoke|restore`、`GET /api/lens/{id}`、`GET /api/lens/page`
- 加工单：`POST /api/orders/create`、`POST /api/orders/transition`
  （action：`START_PROCESSING` / `READY` / `PICK` / `CANCEL`）、
  `POST /api/orders/restore`、`GET /api/orders/{orderNo}`

## 启动

1. 执行 `mvn -q -DskipTests compile` 验证构建。
2. 执行 `mvn spring-boot:run` 启动；Spring Boot 自动执行 `src/main/resources/schema.sql`。
3. H2 数据文件默认写入工作区 `data/evops`。改路径编辑 `application.yml` 的 `jdbc:h2:file:` 连接串。

根目录 `docs/schema/evops.sql` 是交付副本，应与 `src/main/resources/schema.sql` 保持一致。

> 如本地 Maven 中央仓不可达，可在 `~/.m2/settings.xml` 配置可达镜像（如 aliyun public）。
