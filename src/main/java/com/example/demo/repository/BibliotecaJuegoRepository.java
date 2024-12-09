package com.example.demo.repository;

import com.example.demo.model.BibliotecaJuego;
import com.example.demo.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BibliotecaJuegoRepository extends JpaRepository<BibliotecaJuego, Long> {
    List<BibliotecaJuego> findByUsuario(Usuario usuario);
    List<BibliotecaJuego> findByUsuarioId(Long id);
    boolean existsByUsuarioAndNombreJuego(Usuario usuario, String nombreJuego);
    boolean existsByUsuarioAndId(Usuario usuario, Long id);
    void deleteByUsuarioAndId(Usuario usuario, Long id);
    Optional<BibliotecaJuego> findByUsuarioAndId(Usuario usuario, Long id);
}