package zh.tools.common.okhttp;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OkHttpExecutor 集成测试：基于 MockWebServer 断言实际发出的请求与响应解析结果。
 */
class OkHttpExecutorTest {

    private MockWebServer server;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    private String baseUrl() {
        return server.url("/").toString();
    }

    // OkHttpRequest 的 files 无 setter，统一用全参构造创建带文件的请求
    private OkHttpRequest multipartRequest(Map<String, OkHttpRequest.FilePart> files) {
        return new OkHttpRequest(HttpMethod.POST, "/api/upload", null,
                new HashMap<>(), new HashMap<>(), files, 30L, true);
    }

    // POST JSON body：请求体为 JSON，Content-Type 为 application/json
    @Test
    void postJsonBody() throws InterruptedException {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("{\"code\":0,\"msg\":\"ok\"}"));

        Map<String, Object> body = new HashMap<>();
        body.put("name", "张三");
        body.put("age", 18);
        OkHttpRequest req = new OkHttpRequest(HttpMethod.POST, "/api/user");
        req.setBody(body);

        OkHttpResponse resp = OkHttpExecutor.execute(req, baseUrl());

        RecordedRequest recorded = server.takeRequest();
        assertEquals("POST", recorded.getMethod());
        assertTrue(String.valueOf(recorded.getHeader("Content-Type")).startsWith("application/json"));
        String sentBody = recorded.getBody().readUtf8();
        assertTrue(sentBody.contains("\"name\""));
        assertTrue(sentBody.contains("张三"));

        assertEquals(200, resp.getStatusCode());
        assertFalse(resp.isFile());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody() instanceof Map, "JSON 对象响应应解析为 Map");
        assertEquals(0, ((Map<?, ?>) resp.getBody()).get("code"));
    }

    // 响应为 JSON 数组时解析成 List
    @Test
    void jsonArrayResponseParsedToList() {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("[{\"id\":1},{\"id\":2}]"));

        OkHttpResponse resp = OkHttpExecutor.execute(
                new OkHttpRequest(HttpMethod.GET, "/api/list"), baseUrl());

        assertTrue(resp.getBody() instanceof List, "JSON 数组响应应解析为 List");
        assertEquals(2, ((List<?>) resp.getBody()).size());
    }

    // normal 模式下 params 拼接到 URL query
    @Test
    void paramsAppendedToQueryInNormalMode() throws InterruptedException {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "text/plain")
                .setBody("ok"));

        OkHttpRequest req = new OkHttpRequest(HttpMethod.GET, "/api/search");
        req.getParams().put("keyword", "测试");
        req.getParams().put("page", 1);

        OkHttpExecutor.execute(req, baseUrl());

        RecordedRequest recorded = server.takeRequest();
        String path = recorded.getPath();
        assertNotNull(path);
        assertTrue(path.startsWith("/api/search?"));
        assertTrue(path.contains("keyword=%E6%B5%8B%E8%AF%95"));
        assertTrue(path.contains("page=1"));
    }

    // multipart 模式：params 只进 form 字段，不再拼到 query（回归测试）
    @Test
    void multipartParamsOnlyInFormNotInQuery() throws InterruptedException {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("{\"success\":true}"));

        Map<String, OkHttpRequest.FilePart> files = Collections.singletonMap("file",
                new OkHttpRequest.ByteArrayPart("file", "a.txt", "text/plain",
                        "file-content".getBytes(StandardCharsets.UTF_8)));
        OkHttpRequest req = multipartRequest(files);
        req.getParams().put("bizId", "42");

        OkHttpExecutor.execute(req, baseUrl());

        RecordedRequest recorded = server.takeRequest();
        assertNotNull(recorded.getPath());
        assertFalse(recorded.getPath().contains("bizId"), "params 不应拼到 multipart 请求的 URL query");
        String body = recorded.getBody().readUtf8();
        assertTrue(body.contains("name=\"bizId\""));
        assertTrue(body.contains("42"));
        assertTrue(body.contains("name=\"file\""));
        assertTrue(body.contains("filename=\"a.txt\""));
        assertTrue(body.contains("file-content"));
    }

    // LocalFilePart 上传本地文件（临时目录），服务端收到完整内容
    @Test
    void localFilePartUploadsFileContent() throws Exception {
        server.enqueue(new MockResponse().setBody("{\"success\":true}"));

        Path file = tempDir.resolve("report.txt");
        Files.write(file, "本地文件内容".getBytes(StandardCharsets.UTF_8));

        OkHttpRequest req = multipartRequest(Collections.singletonMap("file",
                new OkHttpRequest.LocalFilePart("file", file.toFile())));

        OkHttpExecutor.execute(req, baseUrl());

        RecordedRequest recorded = server.takeRequest();
        String body = recorded.getBody().readUtf8();
        assertTrue(body.contains("本地文件内容"));
        assertTrue(body.contains("filename=\"report.txt\""));
    }

    // octet-stream + attachment 响应识别为文件，body 为字节数组并提取文件名
    @Test
    void fileResponseDetectedWithFileName() {
        byte[] bytes = "binary-data".getBytes(StandardCharsets.UTF_8);
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/octet-stream")
                .setHeader("Content-Disposition", "attachment; filename=\"report.xlsx\"")
                .setBody(new okio.Buffer().write(bytes)));

        OkHttpResponse resp = OkHttpExecutor.execute(
                new OkHttpRequest(HttpMethod.GET, "/api/download"), baseUrl());

        assertTrue(resp.isFile());
        assertEquals("report.xlsx", resp.getFileName());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody() instanceof byte[], "文件响应 body 应为 byte[]");
        assertArrayEquals(bytes, (byte[]) resp.getBody());
    }

    // filename*=UTF-8''... 优先于 filename=（回归测试：中文文件名取扩展格式）
    @Test
    void filenameStarTakesPriorityOverFilename() {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/octet-stream")
                .setHeader("Content-Disposition",
                        "attachment; filename=\"backup.xlsx\"; filename*=UTF-8''%E6%8A%A5%E8%A1%A8.xlsx")
                .setBody(new okio.Buffer().write("x".getBytes(StandardCharsets.UTF_8))));

        OkHttpResponse resp = OkHttpExecutor.execute(
                new OkHttpRequest(HttpMethod.GET, "/api/download"), baseUrl());

        assertTrue(resp.isFile());
        assertEquals("报表.xlsx", resp.getFileName());
    }

    // 同名响应头合并为逗号连接（回归测试：多个 Set-Cookie 不再丢失）
    @Test
    void duplicateResponseHeadersJoinedWithComma() {
        MockResponse response = new MockResponse()
                .setHeader("Content-Type", "text/plain")
                .setBody("ok");
        response.addHeader("Set-Cookie", "a=1");
        response.addHeader("Set-Cookie", "b=2");
        server.enqueue(response);

        OkHttpResponse resp = OkHttpExecutor.execute(
                new OkHttpRequest(HttpMethod.GET, "/api/cookie"), baseUrl());

        Object cookie = resp.getHeaders().get("Set-Cookie");
        assertNotNull(cookie);
        assertTrue(cookie.toString().contains("a=1"));
        assertTrue(cookie.toString().contains("b=2"));
    }

    // 非法 URL 抛 IllegalArgumentException
    @Test
    void invalidUrlThrowsIllegalArgument() {
        OkHttpRequest req = new OkHttpRequest(HttpMethod.GET, "/api/x");
        assertThrows(IllegalArgumentException.class,
                () -> OkHttpExecutor.execute(req, "http://in valid url"));
    }

    // 请求头注入
    @Test
    void headersSentToServer() throws InterruptedException {
        server.enqueue(new MockResponse().setBody("ok"));

        OkHttpRequest req = new OkHttpRequest(HttpMethod.GET, "/api/auth");
        req.getHeaders().put("X-Token", "abc123");

        OkHttpExecutor.execute(req, baseUrl());

        RecordedRequest recorded = server.takeRequest();
        assertEquals("abc123", recorded.getHeader("X-Token"));
    }

    // 请求失败（连接被断开）抛 OkHttpException
    @Test
    void ioErrorWrappedAsOkHttpException() {
        server.enqueue(new MockResponse()
                .setSocketPolicy(okhttp3.mockwebserver.SocketPolicy.DISCONNECT_AT_START));

        OkHttpRequest req = new OkHttpRequest(HttpMethod.GET, "/api/fail");
        assertThrows(OkHttpException.class, () -> OkHttpExecutor.execute(req, baseUrl()));
    }
}
