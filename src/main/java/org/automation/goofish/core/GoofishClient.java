package org.automation.goofish.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.invoke.MethodHandles.lookup;
import static org.automation.goofish.core.ConnectionProperties.APP_KEY;
import static org.springframework.http.HttpHeaders.*;

@Lazy
@Component
public class GoofishClient implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(lookup().lookupClass());

    @Autowired
    ConnectionProperties properties;

    @Getter
    private WebClient delegate;
    private final Tika tika = new Tika();

    public static final Map<String, String> COMMON_PARAMS = Map.of(
            "jsv", "2.7.2", "v", "1.0", "type", "originaljson", "accountSite",
            "xianyu", "dataType", "json", "timeout", "20000", "sessionOption",
            "AutoLoginOnly", "appKey", APP_KEY,
            "api", "mtop.taobao.idlemessage.pc.login.token"
    );

    //@Retryable(value = {RuntimeException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public boolean hasLogin() {
        ResponseEntity<ObjectNode> response = delegate.post().uri(new DefaultUriBuilderFactory(properties.loginCheckUrl).builder()
                        .queryParams(MultiValueMap.fromSingleValue(
                                Map.of(
                                        "appName", "xianyu",
                                        "fromSite", "77"
                                ))).build())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(MultiValueMap.fromSingleValue(Map.ofEntries(
                        Map.entry("hid", properties.userId),
                        Map.entry("ltl", "true"),
                        Map.entry("appName", "xianyu"),
                        Map.entry("appEntrance", "web"),
                        Map.entry("_csrf_token", properties.csrfToken),
                        Map.entry("umidToken", ""),
                        Map.entry("hsiz", properties.cookie2),
                        Map.entry("bizParams", "taobaoBizLoginFrom=web"),
                        Map.entry("mainPage", "false"),
                        Map.entry("isMobile", "false"),
                        Map.entry("lang", "zh_CN"),
                        Map.entry("returnUrl", ""),
                        Map.entry("fromSite", "77"),
                        Map.entry("isIframe", "true"),
                        Map.entry("documentReferer", properties.httpsUrl),
                        Map.entry("defaultView", "hasLogin"),
                        Map.entry("umidTag", "SERVER"),
                        Map.entry("deviceId", properties.cna)
                )))).retrieve()
                .toEntity(ObjectNode.class)
                .block(Duration.ofSeconds(5));

        return response.getStatusCode().is2xxSuccessful() &&
                response.getBody() != null &&
                "success".equals(response.getBody()
                        .path("content")
                        .path("data")
                        .path("loginResult")
                        .asText());
    }

    /*spm_pre=a21ybx.personal.sidebar.1.494f6ac2w6LFhH&log_id=494f6ac2w6LFhH*/
    //@Retryable(value = {RuntimeException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public Mono<String> getToken() {
        String time = String.valueOf(System.currentTimeMillis());
        String deviceIdDataVal = properties.getDeviceIdDataVal();
        logger.info("start fetching access token for webSocket connection");
        return delegate.post()
                .uri(builder -> {
                    builder.path(properties.getTokenUri());
                    COMMON_PARAMS.forEach(builder::queryParam);
                    return builder
                            .queryParam("sign", properties.generateSign(time, properties.generateToken(), deviceIdDataVal))
                            .queryParam("spm_cnt", "a21ybx.im.0.0")
                            .queryParam("t", time)
                            .build();
                })
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(MultiValueMap.fromSingleValue(Map.of("data", deviceIdDataVal)))
                .retrieve()
                .bodyToMono(ObjectNode.class)
                .flatMap(response -> {
                    String ret = response.path("ret").toString();
                    if (ret.contains("SUCCESS::调用成功")) {
                        return Mono.just(response.path("data").path("accessToken").asText());
                    }
                    return Mono.error(new RuntimeException("获取Token失败: " + response));
                });
    }

    private final static String pvid = new Random().ints(14, 0, 60)
            .mapToObj(i -> String.valueOf("0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz".charAt(i)))
            .collect(Collectors.joining());

    public Mono<JsonNode> getItemInfo(String itemId) {
        String time = String.valueOf(System.currentTimeMillis());
        String itemIdDataVal = """
                {"itemId": "%s"}
                """.trim().formatted(itemId);
        logger.info("send https requests to retrieve item info for: {}", itemId);
        return delegate.post()
                .uri(builder -> {
                    builder.path(properties.getIteminfoUri());
                    COMMON_PARAMS.forEach(builder::queryParam);
                    return builder
                            .queryParam("sign", properties.generateSign(time, properties.generateToken(), itemIdDataVal))
                            .queryParam("spm_cnt", "a21ybx.item.0.0")
                            .queryParam("spm_pre", "a21ybx.personal.feeds.%d.%s".formatted(
                                    new Random().nextInt(50) + 1, pvid
                            ))
                            .queryParam("t", time)
                            .build();
                })
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(MultiValueMap.fromSingleValue(Map.of("data", itemIdDataVal)))
                .retrieve()
                .bodyToMono(ObjectNode.class)
                .flatMap(response -> {
                    String ret = response.path("ret").toString();
                    if (ret.contains("SUCCESS::调用成功")) {
                        logger.info("get item info successful");
                        return Mono.just(response.path("data"));
                    }
                    return Mono.error(new RuntimeException("API调用失败: " + response));
                });
    }

    /**
     * 通过文件系统路径上传
     */
    public Mono<ResponseEntity<JsonNode>> upload(Path filePath) {
        return Mono.using(
                () -> Files.newInputStream(filePath),
                is -> upload(is, filePath.getFileName().toString()),
                this::closeQuietly
        );
    }

    /**
     * 通过Spring Resource上传
     */
    public Mono<ResponseEntity<JsonNode>> upload(Resource resource) {
        return Mono.using(
                resource::getInputStream,
                is -> upload(is, resource.getFilename()),
                this::closeQuietly
        );
    }

    /**
     * 通过输入流上传（需提供文件名）
     */
    @SneakyThrows
    public Mono<ResponseEntity<JsonNode>> upload(InputStream inputStream, String filename) {
        // 复制 InputStream 以便重复使用
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        inputStream.transferTo(baos);
        byte[] bytes = baos.toByteArray();

        return Mono.fromCallable(() -> {
                    MediaType contentType = detectContentType(inputStream, filename);

                    return new FilePart() {
                        @Override
                        public String filename() {
                            return "uploadImg"; // 固定文件名
                        }

                        @Override
                        public Flux<DataBuffer> content() {
                            // 每次调用都从字节数组新建流
                            return DataBufferUtils.readInputStream(
                                    () -> new ByteArrayInputStream(bytes),
                                    new DefaultDataBufferFactory(),
                                    4096
                            );
                        }

                        @Override
                        public HttpHeaders headers() {
                            HttpHeaders headers = new HttpHeaders();
                            headers.setContentDispositionFormData("file", "uploadImg");
                            headers.setContentType(detectContentType(inputStream, filename));
                            return headers;
                        }

                        @Override
                        public String name() {
                            return "file";
                        }

                        @Override
                        public Mono<Void> transferTo(Path dest) {
                            return DataBufferUtils.write(content(), dest);
                        }
                    };
                })
                .flatMap(this::upload);
    }

    public Mono<ResponseEntity<JsonNode>> upload(FilePart filePart) {
        // 1. 生成唯一的boundary（模拟WebKit格式）
        String boundary = "----WebKitFormBoundary" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        // 2. 构建URI（保持原样）
        URI uploadUrl = new DefaultUriBuilderFactory(properties.getUploadUrl()).builder()
                .queryParams(MultiValueMap.fromSingleValue(
                        Map.of("floderId", "0", "appkey", "xy_chat", "_input_charset", "utf-8")
                )).build();

        // 3. 构建Multipart请求体
        return filePart.content()
                .collectList()
                .flatMap(dataBuffers -> {
                    // 合并DataBuffer
                    DataBuffer buffer = new DefaultDataBufferFactory().join(dataBuffers);
                    byte[] fileBytes = new byte[buffer.readableByteCount()];
                    buffer.read(fileBytes);
                    DataBufferUtils.release(buffer);

                    // 自动检测类型
                    MediaType fileContentType = detectContentType(fileBytes, filePart.filename());

                    // 构建multipart body（关键修改点）
                    MultipartBodyBuilder builder = new MultipartBodyBuilder();
                    builder.part("file", new ByteArrayResource(fileBytes) {
                                @Override
                                public String getFilename() {
                                    return "uploadImg"; // 固定文件名
                                }
                            })
                            .contentType(fileContentType);

                    // 4. 发送请求（精确匹配Content-Type）
                    return delegate.post()
                            .uri(uploadUrl)
                            .contentType(MediaType.parseMediaType(
                                    "multipart/form-data; boundary=" + boundary // 关键修改
                            ))
                            .headers(headers -> {
                                // 设置固定值头
                                headers.set("Accept", "*/*");
                                headers.set("Accept-Encoding", "gzip, deflate, br, zstd");
                                headers.set("Access-Control-Allow-Origin", "*");
                                headers.set("Priority", "u=1, i");
                                headers.set("Sec-Ch-Ua-Mobile", "?0");
                                headers.set("Sec-Fetch-Dest", "empty");
                                headers.set("Sec-Fetch-Mode", "cors");
                                headers.set("Sec-Fetch-Site", "same-site");
                                headers.set("X-Requested-With", "XMLHttpRequest");

                                // 设置从配置获取的头
                                headers.set("Origin", properties.getHttpsUrl());
                                headers.set("Referer", properties.getHttpsUrl());
                                headers.set("User-Agent", properties.getUserAgent());
                                headers.set("Sec-Ch-Ua", properties.getUserAgent());
                                headers.set("Sec-Ch-Ua-Platform", "Windows");

                                // 设置复杂值头
                                headers.set("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7,ja-JP;q=0.6,ja;q=0.5,ko-KR;q=0.4,ko;q=0.3");
                            })
                            .body(BodyInserters.fromMultipartData(builder.build()))
                            .retrieve()
                            .toEntity(JsonNode.class);
                });
    }

    private MediaType detectContentType(byte[] content, String filename) {
        try {
            String mimeType = tika.detect(content, filename);
            return MediaType.parseMediaType(mimeType);
        } catch (Exception e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private MediaType detectContentType(InputStream inputStream, String filename) {
        try {
            String mimeType = tika.detect(inputStream, filename);
            return MediaType.parseMediaType(mimeType);
        } catch (IOException e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ignored) {
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.delegate = WebClient.builder()
                .baseUrl(properties.apiUrl)
                .defaultHeaders(headers -> {
                    headers.add(ACCEPT, "application/json");
                    headers.add(ACCEPT_LANGUAGE, "zh-CN,zh;q=0.9");
                    headers.add(CACHE_CONTROL, "no-cache");
                    headers.add(ORIGIN, properties.httpsUrl);
                    headers.add(PRAGMA, "no-cache");
                    headers.add("priority", "u=1, i");
                    headers.add(REFERER, properties.httpsUrl);
                    headers.add("sec-ch-ua", "'Not(A:Brand';v='99', 'Google Chrome';v='133', 'Chromium';v='133'");
                    headers.add("sec-ch-ua-mobile", "?0");
                    headers.add("sec-ch-ua-platform", "Windows");
                    headers.add("sec-fetch-dest", "empty");
                    headers.add("sec-fetch-mode", "cors");
                    headers.add("sec-fetch-site", "same-site");
                    headers.add(USER_AGENT, properties.userAgent);
                })
                .defaultCookies(m -> Arrays.stream(properties.cookieStr.split("; "))
                        .map(cookie -> cookie.split("=", 2))
                        .filter(parts -> parts.length == 2)
                        .collect(Collectors.toMap(
                                parts -> parts[0],
                                parts -> parts[1],
                                (existing, replacement) -> existing
                        )).forEach(m::add))
                .build();
    }
}
