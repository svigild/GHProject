package com.example.demo.service;

import com.example.demo.model.Enfrentamiento;
import com.example.demo.repository.EnfrentamientoRepository;
import org.springframework.stereotype.Service;

@Service
public class EnfrentamientoService {

    private final EnfrentamientoRepository enfrentamientoRepository;

    public EnfrentamientoService(EnfrentamientoRepository enfrentamientoRepository) {
        this.enfrentamientoRepository = enfrentamientoRepository;
    }

    public void actualizarResultado(Long enfrentamientoId, Long jugadorId) {
        // Obtener el enfrentamiento
        Enfrentamiento enfrentamiento = enfrentamientoRepository.findById(enfrentamientoId)
                .orElseThrow(() -> new IllegalArgumentException("Enfrentamiento no encontrado"));

        // Actualizar el resultado con el ID del jugador
        enfrentamiento.setResultado(jugadorId.toString());
        enfrentamientoRepository.save(enfrentamiento);
    }
}