package com.example.demo.service;

import com.example.demo.dto.BibliotecaJuegoDTO;
import com.example.demo.model.BibliotecaJuego;
import com.example.demo.model.Usuario;
import com.example.demo.repository.BibliotecaJuegoRepository;
import com.example.demo.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class BibliotecaJuegoService {

    @Autowired
    private BibliotecaJuegoRepository bibliotecaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public void addJuegoToBiblioteca(Long usuarioId, BibliotecaJuegoDTO bibliotecaJuegoDTO) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));

        BibliotecaJuego juego = new BibliotecaJuego();
        // Setear propiedades del juego desde el DTO
        juego.setNombreJuego(bibliotecaJuegoDTO.getNombreJuego());
        juego.setImagenJuego(bibliotecaJuegoDTO.getImagenJuego());
        juego.setGenero(bibliotecaJuegoDTO.getGenero());
        juego.setAnioSalida(bibliotecaJuegoDTO.getAnioSalida());
        juego.setPlataformas(bibliotecaJuegoDTO.getPlataformas());
        juego.setUsuario(usuario);

        bibliotecaRepository.save(juego);
    }

    public List<BibliotecaJuego> getBibliotecaByEmail(String correoElectronico) {
        Usuario usuario = usuarioRepository.findByEmail(correoElectronico);
        return bibliotecaRepository.findByUsuario(usuario);
    }

    public void eliminarJuego(Long juegoId) {
        bibliotecaRepository.deleteById(juegoId);
    }

    public boolean checkJuegoEnBiblioteca(String nombreUsuario, Long idJuego) {
        Usuario usuario = usuarioRepository.findByNombre(nombreUsuario);
        if (usuario != null) {
            return bibliotecaRepository.existsByUsuarioAndId(usuario, idJuego);
        }
        return false;
    }

    public void toggleJuegoEnBiblioteca(String nombreUsuario, Long idJuego) {
        Usuario usuario = usuarioRepository.findByNombre(nombreUsuario);
        if (usuario != null) {
            Optional<BibliotecaJuego> bibliotecaJuegoOptional = bibliotecaRepository.findByUsuarioAndId(usuario, idJuego);

            if (bibliotecaJuegoOptional.isPresent()) {
                // Si el juego ya está en la biblioteca, lo eliminamos
                bibliotecaRepository.delete(bibliotecaJuegoOptional.get());
            } else {
                // Aquí deberías implementar la lógica para obtener los detalles del juego
                // por ejemplo:
                String nombreJuego = "Nombre del Juego";
                String imagenJuego = "URL de la imagen";
                String genero = "Género del juego";
                String anioSalida = "Año de salida";
                List<String> plataformas = List.of("Plataforma 1", "Plataforma 2");

                // Crear un nuevo objeto BibliotecaJuego y guardarlo en la base de datos
                BibliotecaJuego nuevoJuego = new BibliotecaJuego(nombreJuego, imagenJuego, genero, anioSalida, plataformas, usuario);
                bibliotecaRepository.save(nuevoJuego);
            }
        }
    }

    public void eliminarJuego(Long juegoId, String email) {
        // Verificar si el usuario tiene permisos para eliminar el juego
        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario == null) {
            throw new RuntimeException("Usuario no encontrado con correo: " + email);
        }

        Optional<BibliotecaJuego> juegoOptional = bibliotecaRepository.findById(juegoId);
        if (!juegoOptional.isPresent()) {
            throw new RuntimeException("Juego no encontrado con ID: " + juegoId);
        }

        BibliotecaJuego juego = juegoOptional.get();
        if (!juego.getUsuario().equals(usuario)) {
            throw new RuntimeException("No tienes permisos para eliminar este juego");
        }

        bibliotecaRepository.deleteById(juegoId);
    }

    public boolean puedeEliminarJuego(Long usuarioId, Long juegoId) {
        // Obtener el usuario
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));

        // Verificar si el juego pertenece al usuario
        Optional<BibliotecaJuego> juegoOptional = bibliotecaRepository.findById(juegoId);
        return juegoOptional.isPresent() && juegoOptional.get().getUsuario().equals(usuario);
    }

    public BibliotecaJuego obtenerJuegoPorId(Long juegoId) {
        Optional<BibliotecaJuego> juego = bibliotecaRepository.findById(juegoId);
        if (juego.isPresent()) {
            return juego.get();
        } else {
            throw new IllegalArgumentException("Juego no encontrado con ID: " + juegoId);
        }
    }
}
