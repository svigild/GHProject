package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class NewsService {

    @Value("${newsapi.apiKey}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> getVideoGameNews() {
        String url = "https://newsapi.org/v2/everything?q=video+games&language=es&apiKey=" + apiKey;
        return restTemplate.getForObject(url, Map.class);
    }
}