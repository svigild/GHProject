package com.example.demo.service;

import com.example.demo.model.Usuario;
import com.example.demo.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Busca el usuario por su email
        Usuario usuario = usuarioRepository.findByEmail(email);

        if (usuario == null) {
            throw new UsernameNotFoundException("Usuario no encontrado con el email: " + email);
        }

        // Obtiene el rol desde la entidad usuario y le añade el prefijo "ROLE_"
        String rolUsuario = usuario.getRol().name();
        System.out.println("Rol del usuario " + email + ": " + rolUsuario);
        // Crea el UserDetails usando el rol como autoridad con el prefijo
        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getContrasenia())
                .roles(rolUsuario)
                .build();
    }
}
