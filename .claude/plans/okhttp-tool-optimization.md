# OkHttpTool 优化 + 测试用例

## 现状事实（已核实）

- `common/src/main/java/zh/tools/common/okhttp/OkHttpTool.kt`（306 行）是 okhttp 包唯一文件
- **仓库内零调用方**（全仓 grep 无 import zh.tools.common.okhttp），改动风险低
- 本地 m2 里 zh-tools-common 0.0.8 已被外部拉取过，但引用方均用 `${project.parent.version}`，无硬编码耦合
- common 模块**无 src/test 目录**；根 POM kotlin-maven-plugin 只有 `compile` execution，**无 test-compile** → Kotlin 测试文件在 mvn test 下被静默跳过
- 本地无 mvn 二进制 → 编译验证用 JetBrains MCP `build_project`；MockWebServer 4.9.3 本地缺失但 aliyun 镜像可达（IDE 会自动下载）

## 代码改动（OkHttpTool.kt，用户已确认"全面优化"）

### Bug 修复
1. `toContentType()`：`docx` → `application/vnd.openxmlformats-officedocument.wordprocessingml.document`，`doc` → `application/msword`；`xlsx` → `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`，`xls` → `application/vnd.ms-excel`
2. `extractFileName()`：`filename*=UTF-8''...`（RFC 6266 扩展格式）优先于 `filename=`。实现：先扫一遍找 `filename*`，找到即返回；否则再找 `filename=`
3. `LocalFilePart.content`：整文件读进内存 → buildMultipartRequest 里对 `LocalFilePart` 特判用 `file.asRequestBody()` 流式上传（okhttp3.RequestBody.Companion.asRequestBody），其余 FilePart 子类保持 `content.toRequestBody()`
4. URL 拼接：`baseUrl.trimEnd('/')` + `/` + `request.url.trimStart('/')`，防双斜杠

### 行为变更（用户已确认）
5. multipart 模式：`params` 只进 form 表单字段，不再拼 URL query。normal 模式保持 params 拼 query 不变
6. 响应头解析：`headers.toMap()`（4.9.3 后值覆盖前值）→ 手工按 name 分组合并，同名逗号连接：`values(name).joinToString(", ")`
7. GET+body：行为保持（applyMethod GET 分支丢弃 body），加 KDoc 注释说明 GET 携带 body 会被忽略

### 小优化
8. `request.timeout == 30L && request.followRedirects` 时直接复用 `defaultClient`，跳过 newBuilder()

## 测试改动

### common/pom.xml
- 新增 test scope 依赖 `com.squareup.okhttp3:mockwebserver:4.9.3`（aliyun 可达）

### 新建 common/src/test/java/zh/tools/common/okhttp/OkHttpExecutorTest.java
Java + JUnit 5（Kotlin test-compile 未配置，必须 Java）。MockWebServer 逐用例 enqueue MockResponse。

测试矩阵：
1. **POST JSON body**：body 用 Map → 断言收到的 Content-Type=application/json、body 为 JSON 字符串；响应 JSON 对象解析成 Map
2. **POST JSON 数组**：响应 `[...]` → body 解析为 List
3. **params 拼 query**（normal 模式）：断言请求 path 带 `?a=1&b=2`（顺序不敏感）
4. **multipart 上传 ByteArrayPart**：断言 multipart body 含 part name、filename、内容字节；断言 params 进 form 字段而**不在** query 里（行为变更 5 的回归测试）
5. **LocalFilePart 流式**：用临时文件（@TempDir）上传 → 断言服务端收到完整内容
6. **文件响应识别**：响应 Content-Type: application/octet-stream + Content-Disposition: attachment; filename="报表.xlsx" → isFile=true、fileName="报表.xlsx"、body 为 byte[]
7. **filename\* 优先**：Content-Disposition 同时含 `filename="a.xlsx"` 和 `filename*=UTF-8''%E6%8A%A5%E8%A1%A8.xlsx` → 取中文名"报表.xlsx"（bug 2 的回归测试）
8. **重名响应头合并**：两个 Set-Cookie → body 无关，headers 里该 key 值为逗号连接（行为变更 6 的回归测试）
9. **非法 URL** → IllegalArgumentException
10. **超时复用 defaultClient**：不专门测（行为不可观察），仅代码路径保持

注：测试文件路径放 `src/test/java`（与 main 同目录混编约定一致）；JUnit 5 依赖经根 POM spring-boot-starter-test 已传递可用。

## 验证

1. JetBrains MCP `build_project`（filesToRebuild 不指定，全量或按 IDE 提示）确认编译通过
2. 如 IDE 构建包含 test 源集则一并确认；否则用 `get_file_problems` 检查两个改动文件
3. 不改版本号、不 commit（用户未要求）

## 明确不做

- 不把 `body`/`params`/`headers` 改为不可变（API 兼容）
- 不处理 OkHttp 5 / 升级依赖版本
- 不动 `MultipartFilePart`（Spring 专用路径，行为不变）
