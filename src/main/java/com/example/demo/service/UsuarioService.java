package com.example.demo.service;

import com.example.demo.dto.UsuarioLoginDTO;
import com.example.demo.dto.UsuarioRegistroDTO;
import com.example.demo.model.RolNombre;
import com.example.demo.model.SolicitudAmistad;
import com.example.demo.model.Usuario;
import com.example.demo.repository.SolicitudAmistadRepository;
import com.example.demo.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.management.relation.Role;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SolicitudAmistadRepository solicitudAmistadRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Transactional
    public void registrarNuevoUsuario(UsuarioRegistroDTO usuarioRegistroDTO) {
        Usuario usuario = new Usuario();
        usuario.setNombre(usuarioRegistroDTO.getNombre());
        usuario.setApellidos(usuarioRegistroDTO.getApellidos());
        usuario.setEmail(usuarioRegistroDTO.getEmail());
        usuario.setNombreUsuario(usuarioRegistroDTO.getNombreUsuario());
        usuario.setPais(usuarioRegistroDTO.getPais());
        usuario.setFechaNacimiento(usuarioRegistroDTO.getFechaNacimiento());
        usuario.setContrasenia(passwordEncoder.encode(usuarioRegistroDTO.getContrasenia())); // Encripta la contraseña
        usuario.setFotoPerfil(usuarioRegistroDTO.getUrlFotoPerfil());
        usuario.setRol(RolNombre.USUARIO); // Valor de USUARIO por defecto
        usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario login(UsuarioLoginDTO loginDTO) {
        Usuario usuario = usuarioRepository.findByEmail(loginDTO.getEmail());

        if (passwordEncoder.matches(loginDTO.getContrasenia(), usuario.getContrasenia())) {
            return usuario;
        } else {
            throw new RuntimeException("Credenciales inválidas");
        }
    }

    public boolean nombreUsuarioExistente(String nombreUsuario) {
        return usuarioRepository.existsByNombreUsuario(nombreUsuario);
    }

    public List<Usuario> obtenerTodosUsuarios(){ return usuarioRepository.findAll(); }

    public Usuario findByNombre(String nombre) {
        return usuarioRepository.findByNombre(nombre);
    }

    public Usuario findById(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    public void save(Usuario usuario) {
        usuarioRepository.save(usuario);
    }

    public Usuario obtenerUsuarioPorId(Long id){
        return usuarioRepository.findById(id).orElse(null);
    }

    @Transactional
    public Usuario findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Transactional
    public void actualizar(Usuario usuario) {
        if (usuario.getId() != 0) {
            usuarioRepository.save(usuario);
        } else {
            // En este caso, el ID del usuario no está establecido
            // por lo tanto, es más seguro lanzar una excepción o manejar el error de alguna manera.
            throw new IllegalArgumentException("El usuario no tiene un ID válido para actualizar.");
        }
    }

    @Transactional
    public void rechazarSolicitud(String destinatarioEmail, Long solicitudId) {
        SolicitudAmistad solicitud = solicitudAmistadRepository.findById(solicitudId).orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        if (!solicitud.getDestinatario().getEmail().equals(destinatarioEmail)) {
            throw new RuntimeException("Solicitud no pertenece al usuario destinatario.");
        }
        solicitud.setEstado(SolicitudAmistad.EstadoSolicitud.RECHAZADA);
        solicitudAmistadRepository.save(solicitud);
    }

    public Set<Usuario> getAmigos(Usuario usuario) {
        return usuario.getAmigos();
    }

    @Transactional
    public void eliminarAmigo(Long amigoId) {
        Usuario usuarioActual = obtenerUsuarioAutenticado(); // Método para obtener el usuario autenticado, puedes implementarlo según tu lógica
        Usuario amigoAEliminar = usuarioRepository.findById(amigoId).orElseThrow(() -> new RuntimeException("Amigo no encontrado"));

        // Verificar si el amigo a eliminar está realmente en la lista de amigos del usuario actual
        if (!usuarioActual.getAmigos().contains(amigoAEliminar)) {
            throw new RuntimeException("Este usuario no es tu amigo.");
        }

        // Eliminar al amigo de la lista de amigos
        usuarioActual.getAmigos().remove(amigoAEliminar);
        amigoAEliminar.getAmigos().remove(usuarioActual);

        usuarioRepository.save(usuarioActual);
        usuarioRepository.save(amigoAEliminar);
    }

    private Usuario obtenerUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return usuarioRepository.findByEmail(email);
    }

    public List<Usuario> obtenerUsuariosPorIds(List<Long> ids) {
        return usuarioRepository.findAllById(ids);
    }

    public boolean requestPasswordReset(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario == null) {
            return false;
        }
        String code = generateResetCode();
        usuario.setResetCode(code);
        usuario.setResetCodeExpiration(LocalDateTime.now().plusHours(1));
        usuarioRepository.save(usuario);
        sendResetEmail(usuario.getEmail(), code);
        return true;
    }

    public boolean resetPassword(String code, String newPassword) {
        Usuario usuario = usuarioRepository.findUsuarioByResetCode(code);
        if (usuario == null || usuario.getResetCodeExpiration().isBefore(LocalDateTime.now())) {
            return false;
        }
        usuario.setContrasenia(passwordEncoder.encode(newPassword));
        usuario.setResetCode(null);
        usuario.setResetCodeExpiration(null);
        usuarioRepository.save(usuario);
        return true;
    }

    private String generateResetCode() {
        return UUID.randomUUID().toString();
    }

    private void sendResetEmail(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Recuperación de contraseña");
        message.setText("Tu código de recuperación de GameHub es: " + code);
        mailSender.send(message);
    }

}
