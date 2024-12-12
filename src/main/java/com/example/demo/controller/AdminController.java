    package com.example.demo.controller;

    import com.example.demo.model.Foro;
    import com.example.demo.model.Torneo;
    import com.example.demo.model.Usuario;
    import com.example.demo.service.ForoService;
    import com.example.demo.service.TorneoService;
    import com.example.demo.service.UsuarioService;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.core.Authentication;
    import org.springframework.security.core.context.SecurityContextHolder;
    import org.springframework.security.core.userdetails.User;
    import org.springframework.stereotype.Controller;
    import org.springframework.ui.Model;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.servlet.mvc.support.RedirectAttributes;

    import java.time.LocalDateTime;
    import java.util.List;

    @Controller
    @RequestMapping("/admin")
    public class AdminController {

        @Autowired
        private UsuarioService userService;

        @Autowired
        private ForoService forumService;

        @Autowired
        private TorneoService torneoService;

        @GetMapping
        public String adminPanel(Model model) {
            // Obtén el objeto Authentication de Spring Security
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName(); // Obtiene el nombre de usuario (correo)

            // Busca el usuario correspondiente en el servicio
            Usuario loggedInUser = userService.findByEmail(email);
            if (loggedInUser != null) {
                model.addAttribute("urlFotoPerfil", loggedInUser.getFotoPerfil());
            }

            List<Torneo> torneos = torneoService.obtenerTodosLosTorneos();
            model.addAttribute("tournaments", torneos);
            List<Usuario> users = userService.obtenerTodosUsuarios();
            List<Foro> forums = forumService.obtenerTodosLosForos();
            model.addAttribute("users", users);
            model.addAttribute("forums", forums);
            return "admin-panel";
        }

        // Torneos
        @PostMapping("/editarForo/{id}")
        public String updateForo(
                @PathVariable Long id,
                @RequestParam String titulo,
                @RequestParam String descripcion,
                RedirectAttributes redirectAttributes) {
            try {
                Foro foro = forumService.obtenerForoPorId(id);
                if (foro == null) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Foro no encontrado");
                    return "redirect:/admin";
                }

                // Actualizar los datos del foro
                foro.setTitulo(titulo);
                foro.setDescripcion(descripcion);

                forumService.guardarForo(foro);
                redirectAttributes.addFlashAttribute("successMessage", "Foro actualizado con éxito");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar el foro: " + e.getMessage());
            }

            return "redirect:/admin"; // Redirige al panel de administración después de la actualización
        }

        @PostMapping("/foros/eliminar/{id}")
        public String eliminarForo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
            try {
                forumService.eliminarForo(id);
                redirectAttributes.addFlashAttribute("successMessage", "Foro eliminado con éxito");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el foro: " + e.getMessage());
            }
            return "redirect:/admin"; // Redirige al panel de administración
        }

        @GetMapping("/editUser/{id}")
        public String showEditUserForm(@PathVariable Long id, Model model) {
            Usuario user = userService.obtenerUsuarioPorId(id);
            model.addAttribute("user", user);
            return "edit-user"; // vista para editar usuario
        }


        @PostMapping("/usuarios/{id}")
        public String updateUser(@PathVariable Long id,
                                 @ModelAttribute Usuario updatedUser,
                                 RedirectAttributes redirectAttributes) {
            try {
                Usuario usuario = userService.findById(id);
                if (usuario == null) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Usuario no encontrado");
                    return "redirect:/admin"; // Redirige con mensaje de error
                }
                if (updatedUser.getRol() == null) {
                    redirectAttributes.addFlashAttribute("errorMessage", "El rol no puede estar vacío.");
                    return "redirect:/admin";
                }

                // Actualiza el usuario con los nuevos datos
                usuario.setNombre(updatedUser.getNombre());
                usuario.setEmail(updatedUser.getEmail());
                usuario.setFotoPerfil(updatedUser.getFotoPerfil());
                usuario.setRol(updatedUser.getRol());

                userService.save(usuario);

                redirectAttributes.addFlashAttribute("successMessage", "Usuario actualizado con éxito");
                return "redirect:/admin"; // Redirige al panel de administración
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar el usuario: " + e.getMessage());
                return "redirect:/admin"; // Redirige al panel de administración con un mensaje de error
            }
        }

        // Métodos para torneos
        @PostMapping("/torneos/{id}")
        public String updateTournament(@PathVariable Long id,
                                       @RequestParam String nombre,
                                       @RequestParam LocalDateTime fechaInicio,
                                       @RequestParam LocalDateTime fechaFin,
                                       @RequestParam int maxParticipantes,
                                       RedirectAttributes redirectAttributes) {
            try {
                Torneo torneo = torneoService.obtenerTorneoPorId(id);
                if (torneo == null) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Torneo no encontrado");
                    return "redirect:/admin";
                }
                torneo.setNombre(nombre);
                torneo.setFechaInicio(fechaInicio);
                torneo.setFechaFin(fechaFin);
                torneo.setMaxParticipantes(maxParticipantes);

                torneoService.guardarTorneo(torneo);
                redirectAttributes.addFlashAttribute("successMessage", "Torneo actualizado con éxito");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar el torneo: " + e.getMessage());
            }
            return "redirect:/admin"; // Redirige al panel de administración
        }

        @PostMapping("/torneos/eliminar/{id}")
        public String eliminarTorneo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
            try {
                Torneo torneo = torneoService.obtenerTorneoPorId(id);
                if (torneo == null) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Torneo no encontrado");
                    return "redirect:/admin"; // Redirige con mensaje de error
                }

                torneoService.eliminarTorneo(torneo.getId());
                redirectAttributes.addFlashAttribute("successMessage", "Torneo eliminado con éxito");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el torneo: " + e.getMessage());
            }
            return "redirect:/admin"; // Redirige al panel de administración
        }
    }