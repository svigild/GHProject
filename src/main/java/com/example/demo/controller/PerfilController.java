package com.example.demo.controller;

import com.example.demo.model.Usuario;
import com.example.demo.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Date;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/editar")
    public String mostrarFormularioEdicion(Model model, @AuthenticationPrincipal UserDetails userDetails, Authentication authentication) {
        String email = userDetails.getUsername();
        Usuario usuario = usuarioService.findByEmail(email);

        // Paso el usuario al formulario de edición
        model.addAttribute("usuario", usuario);

        Usuario usuarioAutenticado = usuarioService.findByEmail(userDetails.getUsername());
        model.addAttribute("urlFotoPerfil", usuarioAutenticado.getFotoPerfil());
        model.addAttribute("contrasenia", usuario);
        return "editar-perfil";
    }

    @PostMapping("/guardar")
    public String guardarPerfil(@ModelAttribute Usuario usuario, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuarioExistente = usuarioService.findById((long) usuario.getId());

            // Encriptar la nueva contraseña si se proporciona
            if (!usuario.getContrasenia().isEmpty()) {
                String contraseniaEncriptada = passwordEncoder.encode(usuario.getContrasenia());
                usuario.setContrasenia(contraseniaEncriptada);
                usuario.setRol(usuarioExistente.getRol());
                usuario.setNombreUsuario(usuarioExistente.getNombreUsuario());
            } else {
                // Mantener la contraseña existente si no se cambia
                usuario.setContrasenia(usuarioExistente.getContrasenia());
                usuario.setRol(usuarioExistente.getRol());
                usuario.setNombreUsuario(usuarioExistente.getNombreUsuario());
            }

            // Validar y asignar fecha de nacimiento si no está presente
            if (usuario.getFechaNacimiento() == null) {
                usuario.setFechaNacimiento(usuarioExistente.getFechaNacimiento());
            }

            usuarioService.actualizar(usuario); // Actualiza el usuario en el servicio
            redirectAttributes.addFlashAttribute("successMessage", "Perfil actualizado correctamente");
            return "redirect:/perfil/editar";
        } catch (IllegalArgumentException e) {
            // Maneja la excepción si el ID no es válido
            redirectAttributes.addFlashAttribute("errorMessage", "No se pudo actualizar el perfil");
            return "redirect:/perfil/editar"; // Redirige de vuelta al formulario de edición
        }
    }
}
