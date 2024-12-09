    package com.example.demo.controller;

    import com.example.demo.dto.UsuarioLoginDTO;
    import com.example.demo.dto.UsuarioRegistroDTO;
    import com.example.demo.model.SolicitudAmistad;
    import com.example.demo.model.Usuario;
    import com.example.demo.repository.SolicitudAmistadRepository;
    import com.example.demo.repository.UsuarioRepository;
    import com.example.demo.service.SolicitudAmistadService;
    import com.example.demo.service.UsuarioService;
    import jakarta.servlet.http.HttpServletRequest;
    import jakarta.servlet.http.HttpSession;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.core.Authentication;
    import org.springframework.security.core.annotation.AuthenticationPrincipal;
    import org.springframework.security.core.userdetails.User;
    import org.springframework.security.core.userdetails.UserDetails;
    import org.springframework.stereotype.Controller;
    import org.springframework.ui.Model;
    import org.springframework.validation.BindingResult;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.servlet.view.RedirectView;
    import com.example.demo.model.BibliotecaJuego;
    import java.security.Principal;
    import java.util.*;
    import java.util.stream.Collectors;

    @Controller
    @CrossOrigin(origins = "http://localhost:9090")
    public class UsuarioController {

        @Autowired
        private UsuarioService usuarioService;

        @Autowired
        private SolicitudAmistadRepository solicitudAmistadRepository;

        @Autowired
        private UsuarioRepository usuarioRepository;

        @Autowired
        private SolicitudAmistadService solicitudAmistadService;

        @GetMapping("/registro")
        public String mostrarFormularioRegistro(Model model) {
            model.addAttribute("usuarioRegistroDTO", new UsuarioRegistroDTO());
            return "registro";
        }

        @PostMapping("/registro")
        public String registrarUsuario(@ModelAttribute UsuarioRegistroDTO usuarioRegistroDTO, BindingResult result) {
            // Verificar si el nombre de usuario ya existe
            if (usuarioService.nombreUsuarioExistente(usuarioRegistroDTO.getNombreUsuario())) {
                result.rejectValue("nombreUsuario", "error.usuario", "El nombre de usuario ya está en uso.");
            }

            // Si hay errores, volver a la página de registro
            if (result.hasErrors()) {
                return "registro"; // Asegúrate de que este es el nombre correcto de tu vista de registro
            }
            // Si todo está bien, registrar el nuevo usuario
            usuarioService.registrarNuevoUsuario(usuarioRegistroDTO);
            return "redirect:/login"; // Redirigir después de registrar
        }

        @GetMapping("/login")
        public String mostrarFormularioLogin(Model model) {
            model.addAttribute("usuarioLoginDTO", new UsuarioLoginDTO());
            return "login";
        }

        @PostMapping("/login")
        public String loginPost(HttpServletRequest request, @ModelAttribute UsuarioLoginDTO usuarioLoginDTO, Model model) {
            try {
                Usuario usuario = usuarioService.login(usuarioLoginDTO); // Verificar las credenciales del usuario
                HttpSession session = request.getSession();
                session.setAttribute("authenticated", true); // Establecer la autenticación en la sesión
                session.setAttribute("urlFotoPerfil", usuario.getFotoPerfil()); // Establecer la foto de perfil
                session.setAttribute("nombreUsuario", usuario.getNombre()); // Guardar el nombre del usuario en la sesión
                session.setAttribute("usuarioId", usuario.getId());
                session.setAttribute("rol", usuario.getRol());
                return "redirect:/"; // Redirigir a la página de inicio después del inicio de sesión exitoso
            } catch (RuntimeException e) {
                model.addAttribute("error", "Credenciales inválidas"); // Agregar un mensaje de error al modelo
                return "login"; // Volver a cargar la página de inicio de sesión con un mensaje de error
            }
        }

        @GetMapping("/amigos")
        public String mostrarAmigos(Authentication authentication, Model model) {
            String email = authentication.getName();
            Usuario usuario = usuarioRepository.findByEmail(email); // Obtener el usuario autenticado

            // Obtener amigos y solicitudes
            Set<Usuario> amigos = usuario.getAmigos();
            List<SolicitudAmistad> solicitudesEnviadas = solicitudAmistadRepository.findByRemitenteAndEstado(usuario, SolicitudAmistad.EstadoSolicitud.PENDIENTE);
            List<SolicitudAmistad> solicitudesRecibidas = solicitudAmistadRepository.findByDestinatarioAndEstado(usuario, SolicitudAmistad.EstadoSolicitud.PENDIENTE);
            String fotoPerfil = usuario.getFotoPerfil();

            // Obtener el ID del usuario
            int usuarioId = usuario.getId(); // Aquí obtienes el ID del usuario

            // Agregar datos al modelo
            model.addAttribute("amigos", amigos);
            model.addAttribute("solicitudesEnviadas", solicitudesEnviadas);
            model.addAttribute("solicitudesRecibidas", solicitudesRecibidas);
            model.addAttribute("urlFotoPerfil", fotoPerfil);
            model.addAttribute("usuarioId", usuarioId); // Agregar el ID del usuario al modelo

            return "amigos"; // Devuelve la vista
        }

        @GetMapping("/solicitudesAmistad")
        public String verSolicitudesAmistad(Model model, Principal principal) {
            Usuario usuario = usuarioService.findByEmail(principal.getName());
            model.addAttribute("solicitudesRecibidas", usuario.getSolicitudesAmistadRecibidas());
            return "solicitudesAmistad";
        }

        @PostMapping("/enviarSolicitudAmistad")
        @ResponseBody  // Importante para manejar respuesta JSON
        public ResponseEntity<String> enviarSolicitudAmistad(@RequestParam String email, Principal principal) {
            try {
                // Obtener el usuario remitente
                Usuario remitente = usuarioService.findByEmail(principal.getName());

                // Obtener el destinatario
                Usuario destinatario = usuarioService.findByEmail(email);
                if (destinatario == null) {
                    return ResponseEntity.badRequest().body("El usuario destinatario no existe.");
                }

                // Comprobar si ya son amigos
                if (remitente.getAmigos().contains(destinatario)) {
                    return ResponseEntity.badRequest().body("No puedes enviarle una solicitud a " + destinatario.getEmail() + " porque ya sois amigos.");
                }

                // Comprobar si ya se ha enviado una solicitud
                Optional<SolicitudAmistad> solicitudExistente = solicitudAmistadRepository.findByRemitenteAndDestinatario(remitente, destinatario);

                // Si hay una solicitud existente, comprobar su estado
                if (solicitudExistente.isPresent()) {
                    SolicitudAmistad solicitud = solicitudExistente.get();
                    if (solicitud.getEstado() == SolicitudAmistad.EstadoSolicitud.PENDIENTE) {
                        return ResponseEntity.badRequest().body("Ya has enviado una solicitud a este usuario.");
                    }
                    // Si la solicitud fue aceptada en el pasado, se permite enviar una nueva
                }

                // Comprobar que no se ha enviado una solicitud a sí mismo
                if (destinatario.getNombreUsuario().equals(remitente.getNombreUsuario())) {
                    return ResponseEntity.badRequest().body("No puedes enviarte solicitudes de amistad a ti mismo.");
                }

                // Comprobar si hay solicitudes pendientes recibidas o enviadas
                List<SolicitudAmistad> solicitudesPendientesRecibidas = solicitudAmistadRepository.findByDestinatarioAndEstado(destinatario, SolicitudAmistad.EstadoSolicitud.PENDIENTE);
                List<SolicitudAmistad> solicitudesPendientesEnviadas = solicitudAmistadRepository.findByRemitenteAndEstado(remitente, SolicitudAmistad.EstadoSolicitud.PENDIENTE);

                if (!solicitudesPendientesRecibidas.isEmpty() || !solicitudesPendientesEnviadas.isEmpty()) {
                    return ResponseEntity.badRequest().body("Ya tienes solicitudes pendientes con este usuario.");
                }

                // Llamar al servicio para enviar la solicitud de amistad
                solicitudAmistadService.enviarSolicitudAmistad(remitente, destinatario);

                return ResponseEntity.ok("Solicitud de amistad enviada con éxito.");
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body("Error: " + e.getMessage());
            }
        }

        @PostMapping("/aceptarSolicitud")
        @ResponseBody
        public RedirectView aceptarSolicitud(Authentication authentication, @RequestParam("solicitudId") Long solicitudId) {
            Optional<SolicitudAmistad> solicitudOpt = solicitudAmistadRepository.findById(solicitudId);

            if (solicitudOpt.isPresent()) {
                SolicitudAmistad solicitud = solicitudOpt.get();
                String destinatarioEmail = authentication.getName();

                if (!solicitud.getDestinatario().getEmail().equals(destinatarioEmail)) {
                    throw new RuntimeException("Solicitud no pertenece al usuario destinatario.");
                }

                // Cambiar el estado de la solicitud
                solicitud.setEstado(SolicitudAmistad.EstadoSolicitud.ACEPTADA);
                solicitudAmistadRepository.save(solicitud);

                // Agregar amigos
                Usuario remitente = solicitud.getRemitente();
                Usuario destinatario = solicitud.getDestinatario();
                remitente.getAmigos().add(destinatario);
                destinatario.getAmigos().add(remitente);

                usuarioRepository.save(remitente);
                usuarioRepository.save(destinatario);
            }

            // Redirigir a la página de amigos
            return new RedirectView("/amigos");
        }

        @PostMapping("/rechazarSolicitud")
        @ResponseBody
        public ResponseEntity<String> rechazarSolicitud(@RequestParam Long solicitudId, Principal principal) {
            usuarioService.rechazarSolicitud(principal.getName(), solicitudId);
            return ResponseEntity.ok("Solicitud rechazada");
        }

        @PostMapping("/eliminarAmigo/{amigoId}")
        @ResponseBody
        public RedirectView eliminarAmigo(@PathVariable Long amigoId) {
            try {
                usuarioService.eliminarAmigo(amigoId);
                return new RedirectView("/amigos");
            } catch (RuntimeException e) {
                return new RedirectView("/amigos?error=" + e.getMessage());
            }
        }

        @GetMapping("/amigos/{id}/detalles")
        @ResponseBody
        public ResponseEntity<?> obtenerDetallesAmigo(@PathVariable Long id) {
            // Buscar al usuario por su ID
            Usuario amigo = usuarioService.obtenerUsuarioPorId(id);
            if (amigo == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Amigo no encontrado");
            }

            // Crear una estructura de respuesta con los detalles necesarios
            Map<String, Object> detallesAmigo = new HashMap<>();
            detallesAmigo.put("nombre", amigo.getNombre());
            detallesAmigo.put("apellidos", amigo.getApellidos());
            detallesAmigo.put("fotoPerfil", amigo.getFotoPerfil());
            detallesAmigo.put("email", amigo.getEmail());
            detallesAmigo.put("anioNacimiento", amigo.getFechaNacimiento());

            List<Map<String, Object>> juegos = amigo.getBiblioteca().stream()
                    .map(juego -> {
                        Map<String, Object> juegoInfo = new HashMap<>();
                        juegoInfo.put("nombre", juego.getNombreJuego());
                        juegoInfo.put("foto", juego.getImagenJuego());
                        juegoInfo.put("fechaSalida", juego.getAnioSalida());
                        juegoInfo.put("plataformas", juego.getPlataformas());
                        return juegoInfo;
                    })
                    .collect(Collectors.toList());

            detallesAmigo.put("juegos", juegos);

            return ResponseEntity.ok(detallesAmigo); // Devolver los detalles del amigo en formato JSON
        }
    }