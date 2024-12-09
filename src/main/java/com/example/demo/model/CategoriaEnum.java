package com.example.demo.model;

public enum CategoriaEnum {
    NOTICIAS("Noticias y Actualizaciones", "#FF5733"),
    DISCUSION_GENERAL("Discusión General", "#3498DB"),
    SUGERENCIAS("Sugerencias y Comentarios", "#F1C40F"),
    AYUDA("Ayuda y Soporte Técnico", "#E74C3C"),
    VIDEOJUEGOS("Videojuegos", "#8E44AD"),
    HARDWARE("Hardware y Tecnología", "#2ECC71"),
    COMUNIDAD("Comunidad (Off-Topic)", "#9B59B6"),
    REVIEW_DE_JUEGOS("Reseñas de Juegos", "#FFC300"),
    EVENTOS("Eventos y Concursos", "#FF5733"),
    MODDING("Modding y Personalización", "#C70039"),
    DESARROLLO_DE_JUEGOS("Desarrollo de Juegos", "#581845"),
    MUSICA("Música y Bandas Sonoras", "#1D9BF0"),
    CINE_Y_SERIES("Cine y Series", "#28B463"),
    NOVEDADES_TECNOLOGICAS("Novedades Tecnológicas", "#9B59B6"),
    SALUD_Y_BIENESTAR("Salud y Bienestar", "#F39C12"),
    CULTURA_GAMER("Cultura Gamer", "#2980B9"),
    ARTES_GRAFICAS("Artes Gráficas y Diseño", "#D35400");

    private final String nombre;
    private final String color;

    CategoriaEnum(String nombre, String color) {
        this.nombre = nombre;
        this.color = color;
    }

    public String getNombre() {
        return nombre;
    }

    public String getColor() {
        return color;
    }
}