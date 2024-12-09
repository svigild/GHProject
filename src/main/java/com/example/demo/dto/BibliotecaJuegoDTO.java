package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BibliotecaJuegoDTO {
    private Long id;
    private Long usuarioId;
    private String nombreJuego;
    private String imagenJuego;
    private String genero;
    private List<String> plataformas;
    private String anioSalida;
}
