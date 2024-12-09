package com.example.demo.controller;

import com.example.demo.model.Enfrentamiento;
import com.example.demo.model.Torneo;
import com.example.demo.model.Usuario;
import com.example.demo.repository.EnfrentamientoRepository;
import com.example.demo.repository.TorneoRepository;
import com.example.demo.service.TorneoService;
import com.example.demo.service.UsuarioService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/torneos")
public class TorneoController {

    @Autowired
    private TorneoService torneoService;

    @Autowired
    private TorneoRepository torneoRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EnfrentamientoRepository enfrentamientoRepository;



    @GetMapping
    public String listarTorneos(@RequestParam(defaultValue = "0") int page, Model model, Authentication authentication,
                                @RequestParam(required = false) String juego,
                                @RequestParam(required = false) String estado,
                                @RequestParam(required = false) String participantes) {

        Page<Torneo> torneos = torneoService.listarTorneosFiltrados(
                PageRequest.of(page, 10),
                juego,
                estado,
                participantes
        );
        model.addAttribute("torneos", torneos);

        if (authentication != null) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Usuario usuarioAutenticado = usuarioService.findByEmail(userDetails.getUsername());
            model.addAttribute("usuarioAutenticado", usuarioAutenticado);
            model.addAttribute("urlFotoPerfil", usuarioAutenticado.getFotoPerfil());
            model.addAttribute("username", usuarioAutenticado.getNombreUsuario());
        } else {
            model.addAttribute("username", "Invitado");
            model.addAttribute("urlFotoPerfil", "https://static.vecteezy.com/system/resources/thumbnails/005/544/718/small/profile-icon-design-free-vector.jpg");
        }

        List<String> nombresJuegos = torneoService.obtenerNombresJuegosUnicos();
        model.addAttribute("nombresJuegos", nombresJuegos);

        // Añadir los parámetros de filtro al modelo para mantener los valores seleccionados
        model.addAttribute("juegoSeleccionado", juego);
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("participantesSeleccionado", participantes);

        return "torneos";
    }

    @GetMapping("/crear")
    public String mostrarFormularioDeCreacion(Model model, Authentication authentication) {
        model.addAttribute("torneo", new Torneo());
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Usuario usuarioAutenticado = usuarioService.findByEmail(userDetails.getUsername());
        model.addAttribute("urlFotoPerfil", usuarioAutenticado.getFotoPerfil());

        return "crear-torneo";
    }

    @PostMapping("/{id}/add-participant")
    public String addParticipant(@PathVariable Long id, @RequestParam Long amigoId, RedirectAttributes redirectAttributes) {
        Torneo torneo = torneoRepository.findById(id).orElseThrow(() -> new RuntimeException("Torneo no encontrado"));
        Usuario amigo = usuarioService.obtenerUsuarioPorId(amigoId);

        if (amigo == null) {
            redirectAttributes.addFlashAttribute("error", "Amigo no encontrado");
        } else if (torneo.getParticipantes().size() >= torneo.getMaxParticipantes()) {
            redirectAttributes.addFlashAttribute("error", "El torneo está lleno");
        } else if (!torneo.getCreadorTorneo().getAmigos().contains(amigo)) {
            redirectAttributes.addFlashAttribute("error", "Solo puedes añadir a tus amigos");
        } else if (torneo.getParticipantes().contains(amigo)) {
            redirectAttributes.addFlashAttribute("error", "Este amigo ya está en el torneo");
        } else {
            torneo.getParticipantes().add(amigo);
            if (torneo.getParticipantes().size() % 2 == 0){
                actualizarEnfrentamientos(torneo);
            }
            torneoRepository.save(torneo);
            redirectAttributes.addFlashAttribute("success", "Participante añadido con éxito");
        }

        return "redirect:/torneos/" + id;
    }

    private void actualizarEnfrentamientos(Torneo torneo) {
        List<Usuario> participantes = new ArrayList<>(torneo.getParticipantes());
        List<Enfrentamiento> enfrentamientos = enfrentamientoRepository.findByTorneoOrderByRondaAsc(torneo);

        // Crear nuevos enfrentamientos solo cuando hay 2 participantes disponibles
        while (enfrentamientos.size() < Math.ceil(participantes.size() / 2.0)) {
            Enfrentamiento nuevoEnfrentamiento = new Enfrentamiento();
            nuevoEnfrentamiento.setTorneo(torneo);
            nuevoEnfrentamiento.setRonda(1);
            enfrentamientos.add(nuevoEnfrentamiento);
        }

        // Asignar participantes a los enfrentamientos
        for (int i = 0; i < participantes.size(); i++) {
            Enfrentamiento enfrentamiento = enfrentamientos.get(i / 2);

            if (i % 2 == 0) {
                // Asignar el primer jugador
                enfrentamiento.setJugador1(participantes.get(i));
            } else {
                // Asignar el segundo jugador
                enfrentamiento.setJugador2(participantes.get(i));
            }
        }

        // Guardar todos los enfrentamientos
        enfrentamientoRepository.saveAll(enfrentamientos);
    }

    @PostMapping("/{torneoId}/remove-participant/{participanteId}")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> removeParticipant(@PathVariable Long torneoId, @PathVariable Long participanteId) {
        // Obtener torneo y participante
        Torneo torneo = torneoRepository.findById(torneoId).orElseThrow(() -> new RuntimeException("Torneo no encontrado"));
        Usuario participante = usuarioService.obtenerUsuarioPorId(participanteId);

        // Verificar que el participante existe en el torneo
        if (participante == null || !torneo.getParticipantes().contains(participante)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Participante no encontrado en el torneo.");
        }

        // Eliminar al participante
        torneo.getParticipantes().remove(participante);

        // Guardar el torneo actualizado
        torneoRepository.save(torneo);

        // Eliminar enfrentamientos si es necesario
        enfrentamientoRepository.deleteByTorneoAndJugador1OrJugador2(torneo, participante, participante);

        return ResponseEntity.ok().build(); // Respuesta exitosa
    }

    @PostMapping("/{torneoId}/set-winner/{enfrentamientoId}/{jugadorId}")
    @ResponseBody
    public ResponseEntity<?> setWinner(@PathVariable Long torneoId, @PathVariable Long enfrentamientoId, @PathVariable Long jugadorId, Authentication authentication) {
        // Verificar si el usuario autenticado es el creador del torneo
        Usuario usuarioAutenticado = (Usuario) authentication.getPrincipal();
        Torneo torneo = torneoRepository.findById(torneoId)
                .orElseThrow(() -> new RuntimeException("Torneo no encontrado"));

        if (!(torneo.getCreadorTorneo().getId()==(usuarioAutenticado.getId()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permiso para realizar esta acción.");
        }

        return ResponseEntity.ok().build(); // Respuesta exitosa
    }

    @PostMapping("/crear")
    public String crearTorneo(
            @ModelAttribute Torneo torneo,
            @RequestParam("imagenTorneo") String imagenJuego,
            @RequestParam("nombreJuego") String nombreJuego,
            Model model,
            Authentication authentication) throws IOException {

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Usuario creador = usuarioService.findByEmail(userDetails.getUsername());
        torneo.setCreadorTorneo(creador);
        torneo.setParticipantes(Collections.singletonList(creador));
        torneo.setNombreJuego(nombreJuego);
        torneo.setImagenJuego(imagenJuego);

        if (torneo.getNombre() == null || torneo.getNombre().isEmpty()) {
            model.addAttribute("error", "El nombre del torneo es obligatorio.");
            return "crear-torneo"; // Redirigir a la vista con error
        }

        if (torneo.getFechaInicio().isAfter(torneo.getFechaFin())) {
            model.addAttribute("error", "La fecha de inicio debe ser anterior a la fecha de fin.");
            return "crear-torneo"; // Redirigir a la vista con error
        }

        if (torneoRepository.existsTorneoByNombre(torneo.getNombre())) {
            model.addAttribute("error", "Ya existe un torneo con ese nombre.");
            return "crear-torneo"; // Redirige a la página de creación del torneo con un mensaje de error.
        }

        torneoService.guardarTorneo(torneo);
        return "redirect:/torneos";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model, Authentication authentication) {
        Torneo torneo = torneoService.obtenerTorneoPorId(id);
        if (torneo == null) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Usuario usuarioAutenticado = usuarioService.findByEmail(userDetails.getUsername());

            // Agregar los atributos al modelo
            model.addAttribute("usuarioAutenticado", usuarioAutenticado);
            model.addAttribute("urlFotoPerfil", usuarioAutenticado.getFotoPerfil());
            return "redirect:/torneos";
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Usuario usuario = usuarioService.findByEmail(userDetails.getUsername());
        List<Usuario> amigos = usuario.getAmigos().stream().collect(Collectors.toList());

        model.addAttribute("torneo", torneo);
        model.addAttribute("amigos", amigos);
        model.addAttribute("tamanosTorneo", Arrays.asList(4, 6, 8, 10, 12, 14, 16, 18, 20));
        return "editar-torneo";
    }

    @PostMapping("/editar/{id}")
    public String editarTorneo(@PathVariable Long id,
                               @RequestParam String nombre,
                               @RequestParam String descripcion,
                               @RequestParam("fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
                               @RequestParam("fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
                               @RequestParam int maxParticipantes) {

        // Obtener el torneo existente
        Torneo torneo = torneoService.obtenerTorneoPorId(id);

        // Actualizar solo los campos editables
        torneo.setNombre(nombre);
        torneo.setDescripcion(descripcion);
        torneo.setFechaInicio(fechaInicio);
        torneo.setFechaFin(fechaFin);
        torneo.setMaxParticipantes(maxParticipantes);

        // Guardar el torneo actualizado
        torneoService.guardarTorneo(torneo);

        return "redirect:/torneos"; // Redirigir a la lista de torneos después de guardar
    }

    // Mostrar la pagina del torneo en cuestión
    @GetMapping("/{id}")
    public String mostrarTorneo(@PathVariable Long id, Model model, Authentication authentication) {
        Torneo torneo = torneoRepository.findById(id).orElseThrow(() -> new RuntimeException("Torneo no encontrado"));
        List<Enfrentamiento> enfrentamientos = enfrentamientoRepository.findByTorneoOrderByRondaAsc(torneo);

        // Calcular el número máximo de rondas
        int maxRondas = (int) Math.ceil(Math.log(torneo.getParticipantes().size()) / Math.log(2));

        // Si hay usuario añade ese usuario, sino tiene los elementos de invitado
        if (authentication != null) {
            // Obtener el usuario autenticado
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Usuario usuarioAutenticado = usuarioService.findByEmail(userDetails.getUsername());

            // Obtener los amigos del usuario
            Set<Usuario> amigos = usuarioAutenticado.getAmigos();
            // Agregar los atributos al modelo
            model.addAttribute("usuarioAutenticado", usuarioAutenticado);
            model.addAttribute("urlFotoPerfil", usuarioAutenticado.getFotoPerfil());
            model.addAttribute("amigos", amigos);
        } else {
            model.addAttribute("username", "Invitado");
            model.addAttribute("urlFotoPerfil", "https://static.vecteezy.com/system/resources/thumbnails/005/544/718/small/profile-icon-design-free-vector.jpg");
        }

        model.addAttribute("torneo", torneo);
        model.addAttribute("enfrentamientos", enfrentamientos);
        model.addAttribute("maxRondas", maxRondas);

        return "detalle-torneo";  // Vista que mostrará los detalles del torneo
    }

    @PostMapping("/{torneoId}/asignar-participante")
    @ResponseBody
    public ResponseEntity<?> asignarParticipante(@PathVariable Long torneoId, @RequestBody Map<String, Long> payload) {
        Long jugadorIndex = payload.get("jugadorIndex");
        Long amigoId = payload.get("amigoId");

        Torneo torneo = torneoService.obtenerTorneoPorId(torneoId);
        if (torneo == null) {
            return ResponseEntity.notFound().build();
        }

        Usuario amigo = usuarioService.obtenerUsuarioPorId(amigoId);
        if (amigo == null) {
            return ResponseEntity.badRequest().body("Amigo no encontrado");
        }



        // Asigna al amigo al puesto correspondiente en el torneo
        torneo.getParticipantes().set(Math.toIntExact(jugadorIndex), amigo);
        torneoService.guardarTorneo(torneo);

        return ResponseEntity.ok("Participante asignado");
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarTorneo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        torneoService.eliminarTorneo(id);
        redirectAttributes.addFlashAttribute("successMessage", "Torneo eliminado con éxito");
        return "redirect:/torneos";
    }

    @PostMapping("/enfrentamientos/{enfrentamientoId}/ganador")
    public ResponseEntity<List<Enfrentamiento>> actualizarGanador(@PathVariable Long enfrentamientoId, @RequestBody Map<String, Long> body) {
        Long jugadorId = body.get("jugadorId");

        Enfrentamiento enfrentamiento = enfrentamientoRepository.findById(enfrentamientoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enfrentamiento no encontrado"));

        // Verifica si el jugador pertenece al enfrentamiento
        Usuario jugadorGanador;
        if (enfrentamiento.getJugador1().getId()==(jugadorId)) {
            jugadorGanador = enfrentamiento.getJugador1();
        } else if (enfrentamiento.getJugador2().getId()==(jugadorId)) {
            jugadorGanador = enfrentamiento.getJugador2();
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El jugador no pertenece al enfrentamiento");
        }

        // Establece el resultado como el nombre del ganador
        enfrentamiento.setResultado(jugadorGanador.getNombreUsuario());
        enfrentamientoRepository.save(enfrentamiento);

        // Verificar si es el último enfrentamiento de la última ronda
        boolean esFinal = (enfrentamiento.getRonda() == torneoService.obtenerMaxRondas(enfrentamiento.getTorneo()));

        if (!esFinal) {
            torneoService.actualizarRondas(enfrentamiento.getTorneo());
        }

        // Obtener nuevos enfrentamientos después de la actualización
        List<Enfrentamiento> nuevosEnfrentamientos = torneoService.obtenerNuevosEnfrentamientos(enfrentamiento.getTorneo());

        return ResponseEntity.ok(nuevosEnfrentamientos);
    }

    @PostMapping("/enfrentamientos/{id}/establecer-resultado")
    @ResponseBody
    public ResponseEntity<Void> establecerResultado(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String resultado = payload.get("resultado");
        Enfrentamiento enfrentamiento = enfrentamientoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enfrentamiento no encontrado"));

        enfrentamiento.setResultado(resultado);
        enfrentamientoRepository.save(enfrentamiento);

        return ResponseEntity.ok().build();
    }

}
