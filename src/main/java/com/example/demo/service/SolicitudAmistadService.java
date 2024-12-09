package com.example.demo.service;

import com.example.demo.model.SolicitudAmistad;
import com.example.demo.model.Usuario;
import com.example.demo.repository.SolicitudAmistadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SolicitudAmistadService {

    @Autowired
    private SolicitudAmistadRepository solicitudAmistadRepository;

    @Autowired
    private UsuarioService usuarioService;

    public void enviarSolicitudAmistad(Usuario remitente, Usuario destinatario) {
        SolicitudAmistad solicitud = new SolicitudAmistad();
        solicitud.setRemitente(remitente);
        solicitud.setDestinatario(destinatario);
        solicitud.setFechaEnvio(LocalDateTime.now());
        solicitud.setEstado(SolicitudAmistad.EstadoSolicitud.PENDIENTE);
        solicitudAmistadRepository.save(solicitud);
    }

    public void aceptarSolicitudAmistad(Long solicitudId) {
        SolicitudAmistad solicitud = findById(solicitudId);

        // Cambiar el estado de la solicitud a ACEPTADA
        solicitud.setEstado(SolicitudAmistad.EstadoSolicitud.ACEPTADA);
        solicitudAmistadRepository.save(solicitud);

        // Obtener los usuarios involucrados
        Usuario remitente = solicitud.getRemitente();
        Usuario destinatario = solicitud.getDestinatario();

        // Agregar mutuamente como amigos
        remitente.getAmigos().add(destinatario);
        destinatario.getAmigos().add(remitente);

        // Guardar los cambios en los usuarios
        usuarioService.save(remitente);
        usuarioService.save(destinatario);

        // Eliminar la solicitud de la lista de recibidas del destinatario
        destinatario.getSolicitudesAmistadRecibidas().remove(solicitud);
        usuarioService.save(destinatario);

        // Eliminar la solicitud de la lista de enviadas del remitente
        remitente.getSolicitudesAmistadEnviadas().remove(solicitud);
        usuarioService.save(remitente);

        solicitudAmistadRepository.delete(solicitud);
    }

    public void rechazarSolicitudAmistad(Long solicitudId) {
        SolicitudAmistad solicitud = findById(solicitudId);
        solicitud.setEstado(SolicitudAmistad.EstadoSolicitud.RECHAZADA);

        // Eliminar la solicitud de la lista de recibidas del destinatario
        Usuario destinatario = solicitud.getDestinatario();
        destinatario.getSolicitudesAmistadRecibidas().remove(solicitud);
        usuarioService.save(destinatario);

        // Eliminar la solicitud de la lista de enviadas del remitente
        Usuario remitente = solicitud.getRemitente();
        remitente.getSolicitudesAmistadEnviadas().remove(solicitud);
        usuarioService.save(remitente);

        solicitudAmistadRepository.delete(solicitud);
    }

    public List<SolicitudAmistad> getSolicitudesAmistadEnviadas(Long remitenteId) {
        return solicitudAmistadRepository.findByRemitenteId(remitenteId);
    }

    public List<SolicitudAmistad> getSolicitudesAmistadRecibidas(Long destinatarioId) {
        return solicitudAmistadRepository.findByDestinatarioId(destinatarioId);
    }

    public SolicitudAmistad findById(Long solicitudId) {
        return solicitudAmistadRepository.findById(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud de amistad no encontrada"));
    }
}