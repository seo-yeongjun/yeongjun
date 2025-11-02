package com.yeongjun.yeongjun.hyerin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yeongjun.yeongjun.hyerin.config.NaverSmartStoreProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class NaverSmartStoreService {

    private static final String CLIENT_ID_HEADER = "X-COMMERCE-CLIENT-ID";
    private static final String CLIENT_SECRET_HEADER = "X-COMMERCE-CLIENT-SECRET";
    private static final String INVENTORY_ENDPOINT = "/external/v1/products/{productNo}/inventory";

    private final NaverSmartStoreProperties properties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public NaverSmartStoreService(NaverSmartStoreProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(getRequestFactory())
                .build();
    }

    private ClientHttpRequestFactory getRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(10).toMillis());
        return factory;
    }

    public boolean isIntegrationEnabled() {
        return properties.isConfigured();
    }

    public Optional<Integer> fetchInventoryQuantity(Long productNo) {
        if (!isIntegrationEnabled()) {
            return Optional.empty();
        }
        try {
            String response = restClient.get()
                    .uri(INVENTORY_ENDPOINT, productNo)
                    .headers(headers -> headers.addAll(defaultHeaders()))
                    .retrieve()
                    .body(String.class);

            if (response == null || response.isBlank()) {
                log.warn("네이버 스마트스토어 재고 조회 응답이 비어 있습니다. productNo={}", productNo);
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response);
            JsonNode dataNode = root.path("data");
            if (dataNode.isMissingNode() || dataNode.isNull()) {
                dataNode = root; // 응답 구조가 단순한 경우 대응
            }

            Integer quantity = extractQuantity(dataNode);
            if (quantity == null) {
                log.warn("네이버 스마트스토어 재고 응답에서 수량을 찾지 못했습니다. productNo={}, response={}", productNo, response);
                return Optional.empty();
            }
            return Optional.of(quantity);
        } catch (RestClientException | IOException e) {
            log.error("네이버 스마트스토어 재고 조회 실패. productNo={}", productNo, e);
            return Optional.empty();
        }
    }

    private Integer extractQuantity(JsonNode node) {
        if (node.hasNonNull("totalInventory")) {
            return node.get("totalInventory").asInt();
        }
        if (node.hasNonNull("quantity")) {
            return node.get("quantity").asInt();
        }
        if (node.hasNonNull("availableInventory")) {
            return node.get("availableInventory").asInt();
        }
        if (node.hasNonNull("inventories") && node.get("inventories").isArray()) {
            int sum = 0;
            for (JsonNode element : node.get("inventories")) {
                if (element.hasNonNull("quantity")) {
                    sum += element.get("quantity").asInt();
                }
            }
            return sum;
        }
        return null;
    }

    public void updateInventoryQuantity(Long productNo, int quantity) {
        if (!isIntegrationEnabled()) {
            return;
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("quantity", quantity);

        try {
            restClient.put()
                    .uri(INVENTORY_ENDPOINT, productNo)
                    .headers(headers -> headers.addAll(defaultHeaders()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.error("네이버 스마트스토어 재고 업데이트 실패. productNo={}, quantity={}", productNo, quantity, e);
        }
    }

    private MultiValueMap<String, String> defaultHeaders() {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(CLIENT_ID_HEADER, properties.getClientId());
        headers.add(CLIENT_SECRET_HEADER, properties.getClientSecret());
        return headers;
    }
}
