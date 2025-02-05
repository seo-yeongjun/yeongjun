package com.yeongjun.yeongjun.news.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yeongjun.yeongjun.news.entity.NewsCompany;
import com.yeongjun.yeongjun.news.entity.NewsEntitiesWithStart;
import com.yeongjun.yeongjun.news.entity.NewsEntity;
import com.yeongjun.yeongjun.news.repository.NewsCompanyDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


@Service
@Slf4j
public class NewsService {
    private final HttpHeaders headers = new HttpHeaders();

    private final String clientId;
    private final String clientSecret;
    private final NewsCompanyDAO newsCompanyDAO;

    public NewsService(
            NewsCompanyDAO newsCompanyDAO,
            @Value("${naver.client.id}") String clientId,
            @Value("${naver.client.secret}") String clientSecret,
            ArrayList<NewsCompany> newsCompanyList, NewsCompanyDAO newsCompanyDAO1
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.newsCompanyDAO = newsCompanyDAO1;

        // 안전하게 값이 세팅된 상태이므로 바로 헤더에 추가 가능
        headers.add("X-Naver-Client-Id", this.clientId);
        headers.add("X-Naver-Client-Secret", this.clientSecret);
    }

    NewsEntitiesWithStart getNewsBySearch(String search, NewsEntitiesWithStart newsEntities) {
        String apiURL = "https://openapi.naver.com/v1/search/news?query=" + search + "&display=" + newsEntities.getDisplay() + "&sort=date&start=" + newsEntities.getStart();


        log.info("apiURL: {}", apiURL);
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();


        ResponseEntity<String> responseEntity = restTemplate.exchange(apiURL, HttpMethod.GET, requestEntity, String.class);

        HttpStatusCode statusCode = responseEntity.getStatusCode();
        String responseBody = responseEntity.getBody();

        if (statusCode == HttpStatus.OK && responseBody != null) {
            JsonObject jsonObj = JsonParser.parseString(responseBody).getAsJsonObject();


            JsonArray items = jsonObj.getAsJsonArray("items");

            for (int i = 0; i < items.size(); i++) {
                JsonObject item = items.get(i).getAsJsonObject();
                if (item.get("title").getAsString().contains(search)) { // 제목에 검색어가 포함된 기사만 저장
                    NewsEntity newsEntity = jsonToNewsEntity(item, search);
                    if (newsEntity != null) {
                        newsEntities.getNewsEntities().add(jsonToNewsEntity(item, search));
                    }
                }
            }
            if (newsEntities.getStart() + newsEntities.getDisplay() <= 1000) {
                if (newsEntities.getNewsEntities().isEmpty() || newsEntities.getNewsEntities().size() < 20) {
                    newsEntities.setStart(newsEntities.getStart() + newsEntities.getDisplay());
                    return getNewsBySearch(search, newsEntities);
                }
            }

            return newsEntities;
        } else {
            return null;
        }
    }

    NewsEntity jsonToNewsEntity(JsonObject jsonObj, String search) {
        NewsEntity newsEntity = new NewsEntity();
        newsEntity.setSearch(search);
        newsEntity.setTitle(jsonObj.get("title").getAsString());
        newsEntity.setLink(jsonObj.get("originallink").getAsString());
        newsEntity.setDescription(jsonObj.get("description").getAsString());
        newsEntity.setCompany(getCompany(jsonObj.get("originallink").getAsString()));
        newsEntity.setPub_date(parsePubDateToLocalDateTime(jsonObj.get("pubDate").getAsString()));

        if (newsEntity.getCompany().equals("기타")) { // 기타로 분류된 기사는 제외
            return null;
        }
        return newsEntity;
    }

    private String getCompany(String originallink) {

        for (NewsCompany newsCompany : getNewsCompanyList()) {
            if (originallink.contains(newsCompany.getPart_of_link())) {
                return newsCompany.getCompany_name();
            }
        }
        return "기타";
    }

    public List<NewsCompany> getNewsCompanyList() {
        return newsCompanyDAO.selectAllCompany();
    }

    public LocalDateTime parsePubDateToLocalDateTime(String pubDate) {
        // 패턴에서 'Z'는 UTC 오프셋(예: +0900)을 인식
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

        // ZonedDateTime으로 먼저 파싱한 다음 LocalDateTime으로 변환
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(pubDate, formatter);
        return zonedDateTime.toLocalDateTime();
    }
}
