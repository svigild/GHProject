package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.Map;

@Service
public class NewsService {

    @Value("${newsapi.apiKey}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> getVideoGameNews() {
        String url = "https://newsapi.org/v2/everything?q=video+games&language=es&sortBy=publishedAt&apiKey=" + apiKey;
        return restTemplate.getForObject(url, Map.class);
    }

    // Noticias filtradas con los parámetros proporcionados
    public Map<String, Object> getFilteredNews(String keyword, String datePublished, String sortBy) {
        String searchQuery = (keyword != null && !keyword.isEmpty()) ? keyword : "video-games";

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("https://newsapi.org/v2/everything")
                .queryParam("apiKey", apiKey)
                .queryParam("q", searchQuery)
                .queryParam("language", "es");

        if (datePublished != null && !datePublished.isEmpty()) {
            builder.queryParam("from", datePublished);
        }

        if (sortBy != null && !sortBy.isEmpty()) {
            builder.queryParam("sortBy", sortBy);
        }

        builder.queryParam("pageSize", 100);
        builder.queryParam("page", 1);

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(builder.toUriString(), Map.class);
            Map<String, Object> responseBody = response.getBody();

            // Manejo de respuesta nula
            if (responseBody == null || !responseBody.containsKey("articles")) {
                System.out.println("No articles found or API error.");
                return Collections.emptyMap();
            }

            // Si se encuentran artículos, los retornamos
            return responseBody;
        } catch (HttpClientErrorException e) {
            System.out.println("API Error: " + e.getMessage());
            return Collections.emptyMap();  // Devuelve un mapa vacío si ocurre un error
        }
    }
}