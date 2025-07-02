package org.automation.goofish.core;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
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

    @Retryable(value = {RuntimeException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
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

    //@Retryable(value = {RuntimeException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public String getToken() {
        String t = String.valueOf(System.currentTimeMillis());
        MultiValueMap<String, String> params = MultiValueMap.fromSingleValue(
                Map.ofEntries(
                        Map.entry("jsv", "2.7.2"),
                        Map.entry("appKey", APP_KEY),
                        Map.entry("t", t),
                        Map.entry("sign", properties.generateSign(t, properties.generateToken(), properties.deviceIdDataVal)),
                        Map.entry("v", "1.0"),
                        Map.entry("type", "originaljson"),
                        Map.entry("accountSite", "xianyu"),
                        Map.entry("dataType", "json"),
                        Map.entry("timeout", "20000"),
                        Map.entry("api", "mtop.taobao.idlemessage.pc.login.token"),
                        Map.entry("sessionOption", "AutoLoginOnly"),
                        Map.entry("spm_cnt", "a21ybx.im.0.0")
                ));
        URI uri = new DefaultUriBuilderFactory(properties.apiUrl).builder()
                .queryParams(params).build();
        logger.info("access {} to get token", uri);
        ResponseEntity<ObjectNode> response = delegate.post().uri(uri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(MultiValueMap.fromSingleValue(Map.of("data", properties.deviceIdDataVal)))
                .retrieve()
                .toEntity(ObjectNode.class)
                .block();
        String ret = response.getBody().path("ret").toString();
        if (ret.contains("SUCCESS::调用成功")) return response.getBody().path("data").path("accessToken").asText();
        throw new RuntimeException("failed to get token: " + response.getBody().toString());
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
