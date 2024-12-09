package com.example.demo.repository;

import com.example.demo.model.SolicitudAmistad;
import com.example.demo.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudAmistadRepository extends JpaRepository<SolicitudAmistad, Long> {
    List<SolicitudAmistad> findByRemitenteId(Long remitenteId);
    List<SolicitudAmistad> findByDestinatarioId(Long destinatarioId);

    // Método para encontrar solicitudes de amistad enviadas por un usuario con un estado específico
    List<SolicitudAmistad> findByRemitenteAndEstado(Usuario remitente, SolicitudAmistad.EstadoSolicitud estado);

    // Método para encontrar solicitudes de amistad recibidas por un usuario con un estado específico
    List<SolicitudAmistad> findByDestinatarioAndEstado(Usuario destinatario, SolicitudAmistad.EstadoSolicitud estado);
    Optional<SolicitudAmistad> findByRemitenteAndDestinatario(Usuario remitente, Usuario destinatario);
}