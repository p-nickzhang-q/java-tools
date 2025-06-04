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

// 配套的请求方法枚举
enum class HttpMethod {
    GET, POST, PUT, DELETE
}


data class OkHttpRequest(
    val httpMethod: HttpMethod,
    val url: String,
    var body: Any? = null,
    var params: MutableMap<String, Any> = mutableMapOf(),
    var headers: MutableMap<String, String> = mutableMapOf(),
    // 修改为普通Map，避免数据类冲突
    var files: Map<String, FilePart> = emptyMap(),
) {
    // 将密封类改为普通类层次结构
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
    else -> "application/octet-stream"
}

data class OkHttpResponse(
    val statusCode: Int,
    val body: Any?, // 可以是 Map<String, Any>（JSON）或 String（非 JSON）
    val headers: Map<String, String>,
)

object OkHttpExecutor {
    private val mapper = jacksonObjectMapper()
    private val client: OkHttpClient = OkHttpClient()
    fun execute(request: OkHttpRequest, baseUrl: String): OkHttpResponse {
        val urlBuilder = "$baseUrl${request.url}".toHttpUrlOrNull()?.newBuilder()
            ?: throw IllegalArgumentException("Invalid URL")

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

        return client.newCall(okRequest).execute().toOkHttpResponse()
    }

    private fun buildMultipartRequest(request: OkHttpRequest, url: HttpUrl): Request {
        val multipartBuilder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)

        // 添加文件部分
        request.files.forEach { (_, part) ->
            multipartBuilder.addFormDataPart(
                part.partName,
                part.fileName,
                part.content.toRequestBody(part.contentType.toMediaType())
            )
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
            mapper.writeValueAsString(it)
                .toRequestBody("application/json".toMediaType())
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
            HttpMethod.POST -> post(body!!)
            HttpMethod.PUT -> put(body!!)
            HttpMethod.DELETE -> delete(body)
        }
    }

    private fun Response.toOkHttpResponse(): OkHttpResponse {
        val bodyString = this.body?.string() // 先读取原始字符串
        val bodyJson = if (bodyString != null) {
            try {
                jacksonObjectMapper().readValue<JSONObject>(bodyString)
            } catch (e: Exception) {
                // 如果解析失败，返回原始字符串（兼容非 JSON 响应）
                bodyString
            }
        } else {
            null
        }

        return OkHttpResponse(
            statusCode = this.code,
            body = bodyJson, // 返回 JSON 对象或原始字符串
            headers = this.headers.toMap()
        )
    }
}