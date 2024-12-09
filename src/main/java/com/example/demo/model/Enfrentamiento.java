package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Enfrentamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Usuario jugador1;

    @ManyToOne
    private Usuario jugador2;

    private Integer ronda; // Número de ronda

    private String resultado; // Ejemplo: "Jugador 1 gana" o "Pendiente"

    @ManyToOne
    @JoinColumn(name = "torneo_id")
    private Torneo torneo;  // Relacion con el torneo
}
