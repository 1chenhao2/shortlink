# 本地开发指南

## 1. 依赖服务

启动应用前需要准备：

- JDK 22
- Maven 3.9+
- MySQL 8.x
- Redis 6+
- Nacos 2.x

执行环境检查：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\environment-check.ps1 -SkipBuild
```

## 2. 数据库初始化

创建数据库：

```sql
CREATE DATABASE shortlink CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

依次导入：

```text
resources/database/link.sql
resources/database/link-data.sql
```

本地示例数据：

```text
用户名：demo
密码：123456
gid：demo-group
```

示例数据只能用于本地环境。

## 3. 配置说明

默认依赖地址：

```text
MySQL  127.0.0.1:3306/shortlink
Redis  127.0.0.1:6379
Nacos  127.0.0.1:8848
```

需要地区统计时，通过环境变量提供高德 Web 服务 Key：

```powershell
$env:AMAP_API_KEY = "你的高德 Web 服务 Key"
```

不要把真实密码、Token 或 API Key 写入版本库。

## 4. 聚合模式

聚合模式将 Admin 和 Project 运行在同一个 JVM 中，适合本地开发和功能验证。

终端一：

```powershell
mvn -pl aggregation -am spring-boot:run "-Dspring-boot.run.arguments=--short-link.domain.default=localhost:8003"
```

终端二：

```powershell
mvn -pl gateway spring-boot:run
```

服务地址：

```text
Gateway     http://localhost:8002
Aggregation http://localhost:8003
```

## 5. 微服务模式

分别启动以下模块：

```powershell
mvn -pl admin -am spring-boot:run
mvn -pl project -am spring-boot:run
mvn -pl gateway spring-boot:run
```

确保各服务已经注册到 Nacos，并检查 Gateway 路由是否指向正确的服务名称。

## 6. 接口验证

可使用 IntelliJ HTTP Client 打开：

```text
docs/api-examples.http
```

推荐验证顺序：

1. 登录并获取 Token。
2. 查询短链接分组。
3. 创建短链接。
4. 在浏览器中访问返回的短链接。
5. 查询访问统计数据。

## 7. 常见问题

### 数据库连接失败

确认 MySQL 已监听 `3306`，数据库名称、用户名和密码与本地配置一致。

### Redis 连接失败

确认 Redis 已监听 `6379`。如果设置了密码，请通过本地覆盖配置提供。

### 服务无法注册

确认 Nacos 已监听 `8848`，并检查命名空间、分组和服务名配置。

### 地区统计为空

确认已配置 `AMAP_API_KEY`。该配置不影响短链接创建和跳转主链路。

### Maven 构建失败

先确认 Java 与 Maven 版本：

```powershell
java -version
mvn -version
```

然后执行：

```powershell
mvn -DskipTests package
```
