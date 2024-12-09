package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BibliotecaJuego {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_juego")
    private String nombreJuego;

    @Column(name = "imagen_juego")
    private String imagenJuego;

    @Column(name = "genero")
    private String genero;

    @Column(name = "anio_salida")
    private String anioSalida;

    @ElementCollection
    @CollectionTable(name = "juego_plataformas", joinColumns = @JoinColumn(name = "juego_id"))
    @Column(name = "plataforma")
    private List<String> plataformas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "tiempo_total")  // Agregar columna para tiempo total
    private String tiempoTotal;

    public BibliotecaJuego(String nombreJuego, String imagenJuego, String genero, String anioSalida, List<String> plataformas, Usuario usuario) {
        this.nombreJuego = nombreJuego;
        this.imagenJuego = imagenJuego;
        this.genero = genero;
        this.anioSalida = anioSalida;
        this.plataformas = plataformas;
        this.usuario = usuario;
        this.tiempoTotal = "0:00:00";  // Valor por defecto o inicialización
    }


}