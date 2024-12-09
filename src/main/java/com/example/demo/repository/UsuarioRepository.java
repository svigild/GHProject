package com.example.demo.repository;

import com.example.demo.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Usuario findByEmail(String email);
    Usuario findByNombre(String nombre);
    boolean existsByNombreUsuario(String nombreUsuario);

    Optional<Usuario> findUsuarioByNombreUsuario(String nombreUsuario);

    Usuario findUsuarioByResetCode(String resetCode);
}