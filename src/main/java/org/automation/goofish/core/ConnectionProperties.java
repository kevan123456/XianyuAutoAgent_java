package org.automation.goofish.core;

import com.github.curiousoddman.rgxgen.RgxGen;
import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.DigestUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Configuration
@ConfigurationProperties
public class ConnectionProperties implements InitializingBean {

    @Value("${goofish.api-url}")
    String apiUrl = "https://h5api.m.goofish.com/h5/"; // default url
    @Value("${goofish.get-token-uri}")
    String tokenUri = "mtop.taobao.idlemessage.pc.login.token/1.0/"; // default uri
    @Value("${goofish.get-iteminfo-uri}")
    String iteminfoUri = "mtop.taobao.idle.pc.detail/1.0/"; // default uri

    @Value("${goofish.socket-url}")
    URI socketUrl = URI.create("wss://wss-goofish.dingtalk.com/");
    @Value("${goofish.https-url}")
    String httpsUrl = "https://www.goofish.com";
    @Value("${goofish.login-check-url}")
    String loginCheckUrl;
    @Value("${goofish.upload-url}")
    String uploadUrl;

    @Getter
    @Value("${goofish.heartbeat.interval}")
    long interval = 15L;
    @Value("${goofish.heartbeat.timeout}")
    int timeout;

    @Value("${goofish.cookies-str}")
    String cookieStr;
    Map<String, String> cookies;
    String userId;
    String deviceId;
    String csrfToken;
    String cookie2;
    String cna;
    String token;
    String deviceIdDataVal;
    String sign;

    String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36";

    public String generateToken() {
        return Arrays.stream(cookies.getOrDefault("_m_h5_tk", "").split("_")).findFirst().orElse("");
    }

    public static final String APP_KEY = "34839810";

    public String generateSign(String t, String token, String data) {
        String msg = String.format("%s&%s&%s&%s", token, t, APP_KEY, data);
        return DigestUtils.md5DigestAsHex(msg.getBytes(StandardCharsets.UTF_8));
    }

    public String generateDeviceId(String userId) {
        final String pattern = "^[0-9A-F]{8}-[0-9A-F]{4}-4[0-9A-F]{3}-[0-9A-F][89AB][0-9A-F]{2}-[0-9A-F]{12}$";
        RgxGen rgxGen = RgxGen.parse(pattern); // Create generator
        String s = rgxGen.generate();
        return "%s-%s".formatted(s, userId);
    }

    @Override
    public void afterPropertiesSet() {
        cookies = Arrays.stream(cookieStr.split("; "))
                .map(cookie -> cookie.split("=", 2))  // 最多分割成2部分
                .filter(parts -> parts.length == 2)    // 只保留有效键值对
                .collect(Collectors.toMap(
                        parts -> parts[0].trim(),                 // 键
                        parts -> parts[1].trim(),                 // 值
                        (existing, replacement) -> existing // 重复键处理：保留已有值
                ));
        userId = cookies.getOrDefault("unb", "");
        deviceId = generateDeviceId(userId);
        csrfToken = cookies.getOrDefault("XSRF-TOKEN", "");
        cookie2 = cookies.getOrDefault("cookie2", "");
        cna = cookies.getOrDefault("cna", "");
        token = generateToken();
        deviceIdDataVal = """
                {"appKey": "444e9908a51d1cb236a27862abc769c9","deviceId": "%s"}
                """.trim().formatted(deviceId);
        sign = generateSign(String.valueOf(System.currentTimeMillis()), generateToken(), deviceIdDataVal);
    }
}
