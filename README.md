# GeneralBot 后端

GeneralBot 后端是基于 Spring Boot 3 的 REST 服务，为前端控制台和 QQ/NapCat 机器人提供统一的用户认证、资源管理、工作流执行、插件运行、AI 生成、统计和主题服务。

> **项目状态：持续更新中**
>
> 后端仍在持续更新与优化。接口、数据结构、工作流节点、AI 能力和配置项可能随版本调整；升级时请备份数据库，并在测试环境完成回归后再替换生产服务。

## 主要能力

- 用户注册、登录、个人资料、密码/邮箱找回；JWT 无状态认证并结合 Redis 管理会话有效性。
- Bot 配置与在线状态管理，可通过 NapCat WebSocket 接入 QQ 事件和动作。
- 插件创建、编辑、发布、版本查询和动态调用。
- 可视化工作流定义、节点执行、调用解析、测试运行和执行日志查询。
- AI 插件工作台：对话式生成代码、代码审查、编译与流式 SSE 输出；支持 OpenAI 兼容接口和 Anthropic。
- Docker Java 集成，可按 Bot 创建/删除 NapCat 容器。
- 运行统计、主题列表、主题编辑、当前主题切换和主题紧急恢复。

## 后端模块结构

```text
src/main/java/com/generalbot/
├── user/          # 用户、登录、邮箱与密码找回
├── bot/           # Bot 注册、在线状态、事件和动作元数据
├── plugin/        # 插件实体、版本、反射调用和发布
├── workflow/      # 工作流定义、执行引擎、日志和大文本存储
├── ai/             # AI 对话、生成、审查、编译和 SSE 流
├── docker/         # NapCat 容器创建和管理
├── statistics/     # 统计查询和定时维护任务
├── theme/          # 主题 CRUD、校验和内置主题
├── security/       # JWT、用户主体和认证过滤器
└── config/         # Spring、Redis、工作流执行器配置
```

后端统一使用 `Result<T>` 返回 `code`、`message` 和 `data`。认证采用 JWT + Redis：JWT 负责签名和过期时间，Redis 用于校验当前会话是否仍然有效。

## 技术栈

- Java 21、Spring Boot 3.5、Spring Web、Spring Security
- MyBatis-Plus、MySQL 8+、Redis 6+
- Spring AI 1.1（OpenAI 兼容接口、Anthropic、MCP WebFlux）
- Docker Java、Playwright、Thymeleaf、Java JWT、Lombok
- Maven 构建，默认打包文件名为 `bot.jar`

## 环境要求

| 依赖 | 建议版本 | 用途 |
| --- | --- | --- |
| JDK | 21 | 编译和运行后端 |
| Maven | 3.9+ | 依赖管理和打包 |
| MySQL | 8.0+ | 业务数据、工作流和插件数据 |
| Redis | 6.0+ | JWT 会话、验证码及缓存 |
| Docker | 20+（可选） | 创建 NapCat 机器人容器 |
| Node.js | 20+（前端） | 构建管理控制台 |

## 配置

默认配置位于 `src/main/resources/application.yml`，并自动导入 `application-napcat.yml`（NapCat/WebSocket/线程池）和 `application-ai.yml`（AI 服务商、模型、SSE 与编译器）。生产环境建议在 `demo/config/application.yml` 或启动参数中覆盖配置。

至少检查以下项目：

```yaml
server:
  port: 8080
  address: 0.0.0.0
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/bot?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: your_mysql_user
    password: your_mysql_password
  data:
    redis:
      host: 127.0.0.1
      port: 6379
jwt:
  secret: replace-with-a-long-random-secret
  expiration: 86400000
docker:
  host: unix:///var/run/docker.sock
```

AI 配置请使用环境变量、外部配置或密钥管理服务提供 `api-key`：

```yaml
ai:
  default:
    plugin:
      provider: OpenAI
      base-url: https://api.openai.com
      api-key: ${AI_API_KEY}
      plugin-model: your-model
    review:
      enabled: false
```

NapCat 默认 WebSocket 端点为 `/ws/bot`，并启用动态 Token 注册。若前面使用 Nginx，请同时转发 `/api/`、WebSocket 和 AI SSE 路由。

### 配置项说明

- `server.port`：HTTP 服务端口，默认 8080。
- `spring.datasource.*`：MySQL 连接；数据库名默认 `bot`。
- `spring.data.redis.*`：Redis 连接，默认 `localhost:6379`。
- `jwt.secret`、`jwt.expiration`：JWT 签名密钥和有效期；生产环境必须使用随机长密钥。
- `upload.plugin-path`：插件上传目录，默认位于运行目录的 `upload/plugins`。
- `docker.host`：Docker Engine 地址；Linux 通常使用 Unix Socket，Docker Desktop 可使用 TCP 地址。
- `napcat.ws.*`：NapCat WebSocket、Token、心跳和任务线程池。
- `workflow.executor.*`：工作流执行器线程数、队列容量和并发上限。
- `ai.default.*`：系统默认 AI 提供商、模型、上下文窗口和代码审查策略。
- `plugin.compiler.*`：AI 插件编译工作目录、Maven 命令和超时。

配置文件中涉及数据库密码、邮箱授权码、JWT 密钥、AI API Key 和公网地址的内容都应视为敏感信息。建议通过外部配置、环境变量或密钥管理服务注入，并在凭据曾经公开后立即轮换。

## 初始化数据库

```sql
CREATE DATABASE bot DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

```bash
mysql -u root -p bot < mysql_create_tables.sql
```

升级前请备份数据库；SQL 文件会随功能更新而变化，不建议未经检查直接重复执行到生产库。

## 本地开发与打包

```bash
cd demo
mvn test
mvn spring-boot:run
```

或：

```bash
mvn clean package -DskipTests
java -jar target/bot.jar
```

Linux 可使用脚本管理服务（脚本默认查找当前目录 `bot.jar`，端口 8080）：

```bash
chmod +x start.sh
./start.sh start       # 启动
./start.sh status      # 状态与最近日志
./start.sh logs        # 实时日志
./start.sh restart     # 重启
./start.sh stop        # 停止
```

支持通过 `--spring.config.additional-location=optional:file:./config/` 指定外部配置目录。

Windows PowerShell 示例：

```powershell
cd demo
$env:SPRING_CONFIG_ADDITIONAL_LOCATION = "optional:file:./config/"
mvn spring-boot:run
```

Linux 生产环境示例：

```bash
cd demo
mvn clean package -DskipTests
cp target/bot.jar /opt/generalbot/
cd /opt/generalbot
java -Xms1g -Xmx2g -Dfile.encoding=UTF-8 \
  -jar bot.jar \
  --spring.config.additional-location=optional:file:./config/
```

运行日志默认由 Logback 输出；生产环境建议将日志目录、上传目录和配置目录放在应用包之外，并配置轮转和磁盘容量告警。

## API 概览

受保护请求需要携带：

```http
Authorization: Bearer <JWT_TOKEN>
```

统一响应结构：

```json
{"code": 200, "message": "操作成功", "data": {}}
```

前端将 `code === 200` 视为成功；常见错误码为 400、401、403、404、500。主要接口分组如下：

| 前缀 | 说明 |
| --- | --- |
| `/user`、`/email` | 注册、登录、用户信息、验证码和密码找回 |
| `/bot`、`/api/bot` | Bot 配置、在线状态、事件/动作元数据 |
| `/plugin` | 插件及版本管理 |
| `/workflow`、`/workflowLog` | 工作流定义、测试执行和执行日志 |
| `/ai-plugin`、`/user-ai-config`、`/code` | AI 对话、生成/编译、模型配置和代码文件 |
| `/docker` | NapCat 容器创建、查询和删除 |
| `/statistics` | 首页统计数据 |
| `/theme` | 主题 CRUD、当前主题和恢复默认主题 |
| `/ws/bot` | NapCat WebSocket 连接端点 |

开放接口包括登录、注册、邮箱验证码和 NapCat WebSocket 握手；其余业务接口默认要求 JWT。调试 API 时请以控制器源码和最新前端调用为准。

### 典型使用流程

1. 调用注册接口创建账号，并通过邮箱验证码完成邮箱相关操作。
2. 登录获取 JWT，前端或 API 客户端保存 Token。
3. 创建 Bot，记录 Bot Token，并让 NapCat 按 `/ws/bot` 连接后端。
4. 创建插件或工作流，先使用测试入口验证参数和节点执行结果。
5. 启用工作流或发布插件，观察执行日志和统计页面。
6. 需要 AI 能力时，先在 AI 配置中验证模型连接，再生成、审查和编译插件。

AI 生成和编译接口使用 SSE，客户端应持续读取事件流，不要按普通 JSON 请求立即关闭连接。

## 运行检查

1. 访问 `GET /actuator/health`（若网关未限制）确认服务存活。
2. 检查日志中的 MySQL、Redis 和 NapCat 初始化结果。
3. 登录前端后创建 Bot，确认 Bot Token 与 NapCat 配置一致。
4. 先运行简单工作流测试，再启用生产 Bot 事件触发。
5. AI 功能需确认 API Key、模型、网络，以及编译器工作目录写权限。

## NapCat 与 Docker 使用说明

### 已有 NapCat 实例

如果已有 NapCat/OneBot 实例，只需在 Bot 配置中使用后端登记的 Token，并将客户端 WebSocket 地址指向：

```text
ws://<后端地址>/ws/bot
```

实际路径以 `napcat.ws.server.url` 为准；使用 HTTPS 时对应改为 `wss://`。

### 由后端创建容器

使用 `/docker` 接口或前端相关入口创建容器时，运行后端的账号必须有 Docker 权限，且 Docker 镜像、端口范围和挂载目录可用。创建后请：

1. 查询容器状态和分配端口。
2. 打开 NapCat UI 完成扫码登录。
3. 确认容器中的 OneBot 配置包含正确的后端地址和 Token。
4. 在 Bot 页面确认在线状态，再启用工作流触发。

删除容器前请确认不再需要其中的登录状态和本地数据。

## 测试与升级

```bash
mvn test
```

当前测试覆盖工作流执行记录、主题 CSS 校验和通用异常处理等核心逻辑。升级时建议按以下顺序操作：

1. 备份 MySQL 数据库、`upload/` 插件目录和外部配置。
2. 阅读最新 SQL 和配置差异，确认是否需要迁移。
3. 在测试环境执行 `mvn test` 并手动验证登录、Bot、工作流和 AI 链路。
4. 替换 JAR，保留旧版本用于快速回滚。

## 当前边界与演进方向

当前主要执行链路是 Java 后端、Java 插件和现有工作流引擎。仓库中的 `PYTHON_EXECUTION_ARCHITECTURE.md` 描述了 Python Worker 的演进方向，相关能力仍在持续设计、开发和优化中，不应直接当作当前版本的默认运行要求。

## 安全与运维注意事项

- 不要提交真实数据库密码、邮箱授权码、JWT 密钥或 AI API Key；示例值上线前必须替换。
- Redis、MySQL、Docker Socket 和管理端口不要直接暴露到公网，优先使用内网、防火墙和反向代理。
- 文件上传默认允许较大文件，生产环境应结合 Nginx、磁盘配额和文件类型校验。
- AI 生成代码会进入编译目录，建议使用隔离用户、容器或受限权限运行。
- 定期备份数据库、`upload/` 插件目录和外部配置；升级前保留可回滚的 JAR 和配置版本。
