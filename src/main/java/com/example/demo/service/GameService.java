package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class GameService {

    @Value("${rawg.apiKey}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public Page<Map<String, Object>> getAllGames(Pageable pageable) {
        String url = "https://api.rawg.io/api/games?key=" + apiKey + "&page=" + (pageable.getPageNumber() + 1) + "&page_size=" + pageable.getPageSize();
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        List<Map<String, Object>> juegos = (List<Map<String, Object>>) response.get("results");

        // Obtener el número total de juegos
        Long total = ((Number) response.get("count")).longValue();

        return new PageImpl<>(juegos, pageable, total);
    }

    public Page<Map<String, Object>> getAllGamesFiltered(Pageable pageable, String plataforma, String genero) {
        // Construir la URL base de la API
        String baseUrl = "https://api.rawg.io/api/games";

        // Construir los parámetros de la consulta
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("key", apiKey)
                .queryParam("page", pageable.getPageNumber() + 1)
                .queryParam("page_size", pageable.getPageSize());

        // Agregar el filtro de plataforma si está presente
        if (plataforma != null && !plataforma.isEmpty()) {
            builder.queryParam("platforms", plataforma);
        }

        // Agregar el filtro de género si está presente
        if (genero != null && !genero.isEmpty()) {
            builder.queryParam("genres", genero);
        }

        // Obtener la respuesta de la API
        ResponseEntity<Map> responseEntity = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                null,
                Map.class);

        Map<String, Object> responseBody = responseEntity.getBody();
        List<Map<String, Object>> juegos = (List<Map<String, Object>>) responseBody.get("results");

        // Obtener el número total de juegos
        Long total = ((Number) responseBody.get("count")).longValue();

        return new PageImpl<>(juegos, pageable, total);
    }

    /**
     * Obtiene juegos aleatorios
     *
     * @return
     */
    public List<Map<String, Object>> getThreeRandomGames() {
        String url = "https://api.rawg.io/api/games?key=" + apiKey;

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            List<Map<String, Object>> games = (List<Map<String, Object>>) response.get("results");

            // Si hay menos de tres juegos, devolver todos los juegos
            if (games.size() <= 3) {
                return games;
            } else {
                // Mezclar la lista y obtener los tres primeros juegos
                Collections.shuffle(games);
                return games.subList(0, 3);
            }
        } catch (Exception e) {
            // Manejo de errores: retorna una lista vacía o una lista con juegos de ejemplo
            System.err.println("Error al obtener juegos de RAWG API: " + e.getMessage());
            return Collections.emptyList(); // O retorna una lista con datos de ejemplo
        }
    }
}