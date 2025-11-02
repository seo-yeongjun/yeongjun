package com.yeongjun.yeongjun.hyerin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "naver.smart-store")
public class NaverSmartStoreProperties {

    /**
     * 기본 API 엔드포인트. 문서 기준 값을 기본으로 사용하고 필요 시 properties 에서 override 할 수 있습니다.
     */
    private String baseUrl = "https://api.commerce.naver.com";

    /**
     * 네이버 커머스 센터에서 발급받은 Application ID.
     */
    private String clientId;

    /**
     * 네이버 커머스 센터에서 발급받은 Application Secret.
     */
    private String clientSecret;

    /**
     * 명시적으로 끄고 싶을 때 사용할 플래그. 값이 없으면 clientId/clientSecret 존재 여부로 판단합니다.
     */
    private boolean enabled = true;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isConfigured() {
        return enabled && clientId != null && !clientId.isBlank()
                && clientSecret != null && !clientSecret.isBlank();
    }
}
