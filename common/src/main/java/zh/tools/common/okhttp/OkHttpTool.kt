package zh.tools.common.okhttp

import com.alibaba.fastjson.JSONObject
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

// 配套的请求方法枚举
enum class HttpMethod {
    GET, POST, PUT, DELETE, PATCH
}

data class OkHttpRequest(
    val httpMethod: HttpMethod,
    val url: String,
    var body: Any? = null,
    var params: MutableMap<String, Any> = mutableMapOf(),
    var headers: MutableMap<String, String> = mutableMapOf(),
    var files: Map<String, FilePart> = emptyMap(),
    var timeout: Long = 30, // 默认超时时间30秒
    var followRedirects: Boolean = true // 默认跟随重定向
) {
    // 文件部分的密封类
    sealed class FilePart {
        abstract val partName: String
        abstract val fileName: String?
        abstract val contentType: String
        abstract val content: ByteArray
    }

    // 字节数组形式
    data class ByteArrayPart(
        override val partName: String,
        override val fileName: String,
        override val contentType: String = "application/octet-stream",
        override val content: ByteArray,
    ) : FilePart() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ByteArrayPart

            if (partName != other.partName) return false
            if (fileName != other.fileName) return false
            if (contentType != other.contentType) return false
            if (!content.contentEquals(other.content)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = partName.hashCode()
            result = 31 * result + fileName.hashCode()
            result = 31 * result + contentType.hashCode()
            result = 31 * result + content.contentHashCode()
            return result
        }
    }

    // 本地文件形式
    data class LocalFilePart(
        override val partName: String,
        val file: File,
        override val fileName: String = file.name,
        override val contentType: String = file.extension.toContentType(),
    ) : FilePart() {
        override val content: ByteArray
            get() = file.readBytes()
    }

    // MultipartFile形式（Spring专用）
    data class MultipartFilePart(
        override val partName: String,
        val multipartFile: MultipartFile,
        override val fileName: String = multipartFile.originalFilename ?: "file",
        override val contentType: String = multipartFile.contentType ?: "application/octet-stream",
    ) : FilePart() {
        override val content: ByteArray
            get() = multipartFile.bytes
    }
}

// 扩展函数：文件扩展名转ContentType
private fun String.toContentType(): String = when (lowercase()) {
    "jpg", "jpeg" -> "image/jpeg"
    "png" -> "image/png"
    "pdf" -> "application/pdf"
    "doc", "docx" -> "application/msword"
    "xls", "xlsx" -> "application/vnd.ms-excel"
    "txt" -> "text/plain"
    "json" -> "application/json"
    "xml" -> "application/xml"
    "zip" -> "application/zip"
    else -> "application/octet-stream"
}

data class OkHttpResponse(
    val statusCode: Int,
    val body: Any?, // 可以是 Map<String, Any>（JSON）或 String（非 JSON）或 ByteArray（文件）
    val headers: Map<String, String>,
    val isFile: Boolean = false,
    val fileName: String? = null,
    val contentType: String? = null,
    val contentLength: Long? = null
)

object OkHttpExecutor {
    private val mapper = jacksonObjectMapper()
    private val defaultClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    fun execute(request: OkHttpRequest, baseUrl: String): OkHttpResponse {
        // 创建自定义客户端
        // 基于 defaultClient 创建新客户端。
        // 注意：newBuilder() 会自动复用 defaultClient 的连接池和线程池，因此不会有性能损耗。
        val client = defaultClient.newBuilder()
            .connectTimeout(request.timeout, TimeUnit.SECONDS)
            .readTimeout(request.timeout, TimeUnit.SECONDS)
            .writeTimeout(request.timeout, TimeUnit.SECONDS)
            .followRedirects(request.followRedirects)
            .followSslRedirects(request.followRedirects)
            .build()

        val urlBuilder = "$baseUrl${request.url}".toHttpUrlOrNull()?.newBuilder()
            ?: throw IllegalArgumentException("Invalid URL: $baseUrl${request.url}")

        // 处理查询参数
        request.params.forEach { (key, value) ->
            urlBuilder.addQueryParameter(key, value.toString())
        }

        // 构建请求（区分普通请求和文件上传）
        val okRequest = if (request.files.isEmpty()) {
            buildNormalRequest(request, urlBuilder.build())
        } else {
            buildMultipartRequest(request, urlBuilder.build())
        }

        return try {
            client.newCall(okRequest).execute().use { response ->
                response.toOkHttpResponse()
            }
        } catch (e: IOException) {
            throw OkHttpException("Request failed: ${e.message}", e)
        }
    }

    private fun buildMultipartRequest(request: OkHttpRequest, url: HttpUrl): Request {
        val multipartBuilder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)

        // 添加文件部分
        request.files.forEach { (_, part) ->
            val requestBody = part.content.toRequestBody(part.contentType.toMediaType())
            multipartBuilder.addFormDataPart(part.partName, part.fileName, requestBody)
        }

        // 添加普通参数
        request.params.forEach { (key, value) ->
            multipartBuilder.addFormDataPart(key, value.toString())
        }

        return Request.Builder()
            .url(url)
            .applyHeaders(request.headers)
            .post(multipartBuilder.build())
            .build()
    }

    private fun buildNormalRequest(request: OkHttpRequest, url: HttpUrl): Request {
        val requestBody = request.body?.let {
            when (it) {
                is String -> it.toRequestBody("text/plain".toMediaType())
                is ByteArray -> it.toRequestBody("application/octet-stream".toMediaType())
                else -> mapper.writeValueAsString(it).toRequestBody("application/json".toMediaType())
            }
        }

        return Request.Builder()
            .url(url)
            .applyHeaders(request.headers)
            .applyMethod(request.httpMethod, requestBody)
            .build()
    }

    // 扩展方法：添加请求头
    private fun Request.Builder.applyHeaders(headers: Map<String, String>): Request.Builder {
        headers.forEach { (key, value) -> addHeader(key, value) }
        return this
    }

    // 扩展方法：设置请求方法
    private fun Request.Builder.applyMethod(method: HttpMethod, body: RequestBody?): Request.Builder {
        return when (method) {
            HttpMethod.GET -> get()
            HttpMethod.POST -> post(body ?: EMPTY_REQUEST)
            HttpMethod.PUT -> put(body ?: EMPTY_REQUEST)
            HttpMethod.DELETE -> delete(body ?: EMPTY_REQUEST)
            HttpMethod.PATCH -> patch(body ?: EMPTY_REQUEST)
        }
    }

    private fun Response.toOkHttpResponse(): OkHttpResponse {
        val contentType = this.header("Content-Type") ?: ""
        val contentDisposition = this.header("Content-Disposition") ?: ""
        val contentLength = this.header("Content-Length")?.toLongOrNull()

        // 判断是否为文件响应
        val isFileResponse = contentType.startsWith("application/octet-stream") ||
                contentType.startsWith("image/") ||
                contentType.startsWith("application/pdf") ||
                contentType.startsWith("application/vnd.ms-") ||
                contentDisposition.contains("attachment")

        return if (isFileResponse) {
            // 处理文件响应
            val bodyBytes = this.body?.bytes()
            val fileName = extractFileName(contentDisposition)

            OkHttpResponse(
                statusCode = this.code,
                body = bodyBytes,
                headers = this.headers.toMap(),
                isFile = true,
                fileName = fileName,
                contentType = contentType,
                contentLength = contentLength
            )
        } else {
            // 处理普通响应
            val bodyString = this.body?.string()
            val parsedBody = parseResponseBody(bodyString, contentType)

            OkHttpResponse(
                statusCode = this.code,
                body = parsedBody,
                headers = this.headers.toMap(),
                isFile = false,
                contentType = contentType,
                contentLength = contentLength
            )
        }
    }

    // 解析响应体
    private fun parseResponseBody(bodyString: String?, contentType: String): Any? {
        if (bodyString == null) return null

        return try {
            when {
                contentType.startsWith("application/json") -> {
                    when {
                        bodyString.startsWith('[') -> mapper.readValue<List<Any>>(bodyString)
                        bodyString.startsWith('{') -> mapper.readValue<JSONObject>(bodyString)
                        else -> bodyString
                    }
                }

                contentType.startsWith("text/") -> bodyString
                else -> bodyString // 其他类型作为字符串处理
            }
        } catch (e: Exception) {
            bodyString // 解析失败返回原始字符串
        }
    }

    // 从Content-Disposition头中提取文件名
    private fun extractFileName(contentDisposition: String): String? {
        return try {
            val parts = contentDisposition.split(";")
            for (part in parts) {
                if (part.trim().startsWith("filename=")) {
                    return part.trim().substring("filename=".length).trim('"')
                }
                // 处理 filename*=UTF-8''encodedfilename 格式
                if (part.trim().startsWith("filename*=UTF-8''")) {
                    val encoded = part.trim().substring("filename*=UTF-8''".length)
                    return java.net.URLDecoder.decode(encoded, "UTF-8")
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    // 空请求体
    private val EMPTY_REQUEST = "".toRequestBody("application/json".toMediaType())
}

// 自定义异常类
class OkHttpException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
