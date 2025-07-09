package org.automation.goofish.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
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
