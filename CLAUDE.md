# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目性质

`zh-tools` 是一个 Java/Kotlin 工具库聚合项目（groupId `io.github.p-nickzhang-q`），供其他业务项目作为依赖引用，不是可运行的应用。父 POM 基于 Spring Boot 2.5.6，编译目标 Java 8，Kotlin 1.9.10 与 Java 混编。发布通过 `mvn deploy` 写入本地文件仓库（distributionManagement 指向 `file:/home/dev/java-tools/maven-repo/`，与仓库内的 `maven-repo/` 目录内容一致，为构建产物，勿手工编辑）。

## 常用命令

```bash
# 全量编译（环境需有 mvn；当前机器只装了 Java 17，无 Maven，需自行安装或用 IDE）
mvn compile

# 只构建某模块（含其依赖）
mvn compile -pl common,jpa -am

# 跑某个测试模块的全部测试
mvn test -pl jpa-test -am

# 跑单个测试类 / 方法
mvn test -pl jpa-test -Dtest=JpaTest
mvn test -pl jpa-test -Dtest=JpaTest#testGreaterThan

# CI profile 只构建 7 个发布模块（排除 jpa-test / mybatis-plus-test / map-to-sql / services）
mvn compile -Pci
```

注意：`jpa-test` 和 `mybatis-plus-test` 的测试是 `@SpringBootTest` 集成测试，需要能连上 `jpa-test/src/main/resources/application.yml` 里配置的 MySQL（内网地址），离线环境会失败。日常验证改动优先用 `mvn compile` + 对应模块的单测。

## 核心架构：过滤器体系

整个库的核心抽象是"把 `Map<String, Object>` 形式的过滤条件解析成查询"，三层结构：

1. **common 模块的 `zh.tools.common.map.FilterMap`** — 构造过滤条件的 DSL（嵌套 wrapper/set），操作符枚举在 `zh.tools.common.filterparse.enums.Operator`（EQ/LT/GT/FUZZY/BETWEEN/LIST 等）。
2. **`zh.tools.common.filterparse`** — `ParseStrategy` 接口 + 每个 Operator 一个解析类（EqualParse、BetweenParse…），由 `BaseFilterParse` 分发。
3. **各持久层模块实现自己的 Strategy**：
   - `jpa` 模块：`JpaStrategy` 把过滤器转成 JPA `Specification`；对外能力由 `BaseFilterService<T, ID>`（业务服务基类，含分页 filter）→ `BaseRepositorySupport` → `BaseFilterController` 一路串起来，`SearchHook` 提供解析前后的钩子。
   - `mybatis-plus` 模块：平行的 `FilterParser` / `CommonService` / `BaseEntity` / `MyBatisPlusStrategy`，并依赖 `map-to-sql` 模块。
   - `map-to-sql` 模块：纯 Kotlin（`JsonToSqlConverter` / `QueryBuilder` / `SqlDialect`），把 JSON 条件直接转 SQL 文本。

改过滤逻辑（新操作符、值类型解析）通常只需要动 common 的 filterparse；JPA 和 MyBatis-Plus 侧各自要在 Strategy 中对接。

## 模块依赖速览

- `common`（zh-tools-common）是根依赖：FilterMap/filterparse、ListQuery/ListUtil、JavaTimeUtil、Json/JsonUtil、OkHttpTool（**Kotlin 文件放在 `src/main/java` 下**——根 POM 的 kotlin-maven-plugin 把 `src/main/java` 配为 Kotlin 源目录，Java/Kotlin 同目录混编，新 Kotlin 文件也放这里）、bitmap、paging（FilterRequest）。
- `jpa`、`mybatis-plus`、`excel`（easyexcel 3.1.0）、`word`（POI 4.1.2 模板填充）、`pdf`（pdfbox 2.0.26）都依赖 common。
- `services`（zh-tools-services，独立版本线）：Spring Boot 自动配置模块（基于 Redis 的 `TaskSchedulerService`），同时注册在 `META-INF/spring.factories`（Boot 2）和 `META-INF/spring/...AutoConfiguration.imports`（Boot 3），改自动配置时两处都要维护。
- `all`（zh-tools-all）是便捷聚合包，POM 里硬编码了各兄弟模块版本。
- `jpa-test` / `mybatis-plus-test` 不是普通测试模块：它们各自带可启动的 Spring Boot 应用（实体、Service、配置在 `src/main` 里），测试类在 `src/test` 并继承/使用这些应用上下文。

## 各模块功能清单

### common（基础工具集）

| 功能 | 类 | 说明 |
|---|---|---|
| 过滤条件 DSL | `map/FilterMap` | 以 `Map<String, Object>` 构造嵌套过滤条件（wrapper/set），是整个过滤体系的数据载体 |
| 过滤器解析 | `filterparse/` | `ParseStrategy` 接口 + 每个操作符一个解析器：Equal、NotEqual、GreaterThan/Less（含 Equal 版）、Between、Fuzzy、List、Start/End、Null/NotNull、MapParse |
| 内存列表查询 | `list/ListQuery` | 对 `List<T>` 做 eq/like/gt/gte/lt/lte/between/or 链式过滤，`findFirst`/`result` 取结果——数据库过滤器语义的内存版 |
| 列表处理 | `list/ListUtil`、`ListProcess`、`LambdaUtils` | 取属性列、按条件回填字段（setMultiValueInList）、sum（BigDecimal/int）、find/filter |
| 时间处理 | `time/` | `TimeProcess`（Long/String/LocalDateTime 互转、取 Quarter/HalfYear、按 TimeRangeType 取时间范围）、`JavaTimeUtil` |
| JSON 封装 | `json/Json`、`JsonUtil` | 统一 JSON 入口、对象转 JSONObject/JSONArray |
| HTTP 客户端 | `okhttp/OkHttpTool.kt`（Kotlin） | 请求封装：普通/multipart、header 注入、响应体按 content-type 解析（JSON 对象/数组）、从 Content-Disposition 提取下载文件名（文件下载） |
| 分页 | `paging/FilterRequest` | 过滤 + 分页请求的标准结构（配合各持久层模块） |
| 其他 | `bitmap/`（IntMap/MultiIntMap）、`number/NumberUtil`（除法/百分比）、`name/NameUtil`（驼峰↔下划线）、`booleans/BooleanUtil` | 小工具 |

### 持久层模块（同一套过滤语义的不同实现）

| 模块 | 功能 |
|---|---|
| `jpa` | `BaseFilterService` / `BaseFilterController` / `BaseRepositorySupport` / `BaseClient`：继承即可获得 `filter(Map/FilterRequest)` 分页查询能力；`JpaStrategy` 把 FilterMap 转 Specification；`SearchHook` 提供查询钩子 |
| `mybatis-plus` | 平行实现：`CommonService` + `FilterParser` + `BaseEntity`，查询回调，依赖 map-to-sql |
| `map-to-sql` | 纯 Kotlin，`JsonToSqlConverter` 把 JSON 条件直接生成 SQL 文本；`SqlDialect` 支持 MySQL / PostgreSQL / Oracle（标识符引号、值格式、分页语法各有实现） |

### 文档处理模块

| 模块 | 功能 |
|---|---|
| `excel` | 基于 easyexcel 3.1.0：`ExcelExport` 导出、导入监听器（`ExcelAnalysisEventListener`、合并单元格读取 `ExcelMergeReadHandler`）、表单模型导入校验（`@ExcelForm`/`@ExcelFormField`/`@ExcelValidate` 注解）、`validateExcel` 文件校验、下载响应头设置 |
| `word` | 基于 POI 4.1.2：`WordFillExport` 模板填充导出（含 `WordFunctionParams`）、下载响应封装 |
| `pdf` | 基于 pdfbox 2.0.26：`PdfExtract` 按正则+分隔符从 PDF 文本提取标签信息，输出 `FilterMap` |

### services（Spring Boot 自动配置）

基于 Redis 的任务调度（Kotlin）：`TaskSchedulerService`（`scheduleTask`/`cancelTask`/`shutdown`，协程轮询执行）、`ScheduledTask` 任务基类（多态 JSON 注册 `registerSubtype`）、`TaskSchedulerAutoConfiguration` 自动装配（`@ConditionalOnClass(RedisTemplate)` + `@ConditionalOnMissingBean`，可被业务方覆盖）。

### all

便捷聚合包，一个依赖引入 common/excel/jpa/pdf 等常用模块。

## 版本管理的坑

各模块版本**独立维护且不同步**（父 POM 0.0.7、common 0.1.0、services 1.0.1、map-to-sql 0.0.8、all 0.0.8），兄弟模块间依赖常用硬编码版本号而非 `${project.parent.version}`。改动某模块后，引用它的模块 POM 里的版本号要手动同步，否则构建拿到的是本地仓库里的旧版本。

## 代码约定

- 混编栈：主体 Java 8 语法，新代码两种语言都有（services、map-to-sql、OkHttpTool 用 Kotlin）；Kotlin 配置了 `-Xjvm-default all`。
- 常用工具库：hutool（5.8.15）、fastjson 2.0.25、Lombok；测试为 JUnit 5（父 POM 已排除 junit-vintage-engine，勿引 JUnit 4），惯例 `@TestInstance(PER_CLASS)` + `@SpringBootTest`。
- 根目录的 `diff_output.txt`、`*.iml` 为 IDE/临时产物，不要提交依赖它们的内容。
