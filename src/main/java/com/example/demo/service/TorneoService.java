package com.example.demo.service;

import com.example.demo.model.Enfrentamiento;
import com.example.demo.model.Torneo;
import com.example.demo.model.Usuario;
import com.example.demo.repository.EnfrentamientoRepository;
import com.example.demo.repository.TorneoRepository;
import com.example.demo.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TorneoService {

    @Autowired
    private TorneoRepository torneoRepository;

    @Autowired
    private EnfrentamientoRepository enfrentamientoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Obtener todos los torneos
    public List<Torneo> obtenerTodosLosTorneos() {
        return torneoRepository.findAll();
    }

    // Obtener un torneo por su ID
    public Torneo obtenerTorneoPorId(Long id) {
        return torneoRepository.findById(id).orElse(null);
    }

    // Guardar o actualizar un torneo
    public Torneo guardarTorneo(Torneo torneo) {
        return torneoRepository.save(torneo);
    }

    // Eliminar un torneo por su ID
    public void eliminarTorneo(Long id) {
        torneoRepository.deleteById(id);
    }

    public List<String> obtenerNombresJuegosUnicos() {
        return torneoRepository.findDistinctNombreJuego();
    }

    public void actualizarRondas(Torneo torneo) {
        List<Enfrentamiento> enfrentamientos = torneo.getEnfrentamientos();

        Map<Integer, List<Enfrentamiento>> rondas = enfrentamientos.stream()
                .collect(Collectors.groupingBy(Enfrentamiento::getRonda));

        int maxRonda = rondas.keySet().stream().max(Comparator.naturalOrder()).orElse(1);

        for (int ronda = 1; ronda <= maxRonda; ronda++) {
            List<Enfrentamiento> enfrentamientosRondaActual = rondas.getOrDefault(ronda, new ArrayList<>());

            if (enfrentamientosRondaActual.isEmpty()) {
                continue; // No procesar rondas vacías
            }

            List<Enfrentamiento> enfrentamientosRondaSiguiente = rondas.getOrDefault(ronda + 1, new ArrayList<>());

            for (int i = 0; i < enfrentamientosRondaActual.size(); i += 2) {
                if (i + 1 < enfrentamientosRondaActual.size()) {
                    Enfrentamiento enfrentamiento1 = enfrentamientosRondaActual.get(i);
                    Enfrentamiento enfrentamiento2 = enfrentamientosRondaActual.get(i + 1);

                    String ganador1 = enfrentamiento1.getResultado();
                    String ganador2 = enfrentamiento2.getResultado();

                    if (ganador1 != null && ganador2 != null) {
                        Usuario jugadorGanador1 = usuarioRepository.findUsuarioByNombreUsuario(ganador1).orElse(null);
                        Usuario jugadorGanador2 = usuarioRepository.findUsuarioByNombreUsuario(ganador2).orElse(null);

                        if (jugadorGanador1 != null && jugadorGanador2 != null) {
                            // Evitar duplicados
                            boolean yaExiste = enfrentamientosRondaSiguiente.stream().anyMatch(e ->
                                    e.getJugador1().equals(jugadorGanador1) && e.getJugador2().equals(jugadorGanador2)
                            );

                            if (!yaExiste) {
                                Enfrentamiento nuevoEnfrentamiento = new Enfrentamiento();
                                nuevoEnfrentamiento.setJugador1(jugadorGanador1);
                                nuevoEnfrentamiento.setJugador2(jugadorGanador2);
                                nuevoEnfrentamiento.setRonda(ronda + 1);
                                nuevoEnfrentamiento.setTorneo(torneo);

                                enfrentamientoRepository.save(nuevoEnfrentamiento);
                                enfrentamientosRondaSiguiente.add(nuevoEnfrentamiento);
                            }
                        }
                    }
                }
            }
        }
    }

    public Page<Torneo> listarTorneosFiltrados(Pageable pageable, String juego, String estado, String participantes) {
        Specification<Torneo> spec = Specification.where(null);

        if (juego != null && !juego.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("nombreJuego"), juego));
        }

        if (estado != null && !estado.isEmpty()) {
            LocalDateTime ahora = LocalDateTime.now();
            spec = spec.and((root, query, cb) -> {
                switch (estado) {
                    case "proximo":
                        return cb.greaterThan(root.get("fechaInicio"), ahora);
                    case "en_curso":
                        return cb.and(
                                cb.lessThanOrEqualTo(root.get("fechaInicio"), ahora),
                                cb.greaterThan(root.get("fechaFin"), ahora)
                        );
                    case "finalizado":
                        return cb.lessThan(root.get("fechaFin"), ahora);
                    default:
                        return cb.conjunction();
                }
            });
        }

        if (participantes != null && !participantes.isEmpty()) {
            spec = spec.and((root, query, cb) -> {
                switch (participantes) {
                    case "disponibles":
                        return cb.lessThan(cb.size(root.get("participantes")), root.get("maxParticipantes"));
                    case "completos":
                        return cb.equal(cb.size(root.get("participantes")), root.get("maxParticipantes"));
                    default:
                        return cb.conjunction();
                }
            });
        }

        return torneoRepository.findAll(spec, pageable);
    }

    public int obtenerMaxRondas(Torneo torneo) {
        int numParticipantes = torneo.getParticipantes().size();
        return Math.max(1, (int) Math.ceil(Math.log(numParticipantes) / Math.log(2)));
    }

    public List<Enfrentamiento> obtenerNuevosEnfrentamientos(Torneo torneo) {
        // Obtener todos los enfrentamientos del torneo
        List<Enfrentamiento> enfrentamientos = enfrentamientoRepository.findByTorneoOrderByRondaAsc(torneo);

        // Agrupar enfrentamientos por ronda
        Map<Integer, List<Enfrentamiento>> rondas = enfrentamientos.stream()
                .collect(Collectors.groupingBy(Enfrentamiento::getRonda));

        // Determinar la ronda actual
        int rondaActual = rondas.keySet().stream().max(Integer::compareTo).orElse(1); // Mínimo 1 ronda

        // Obtener los enfrentamientos de la siguiente ronda
        List<Enfrentamiento> nuevosEnfrentamientos = new ArrayList<>();

        // Verificar si hay enfrentamientos en la ronda actual
        if (rondas.containsKey(rondaActual)) {
            List<Enfrentamiento> enfrentamientosRondaActual = rondas.get(rondaActual);

            for (int i = 0; i < enfrentamientosRondaActual.size(); i += 2) {
                if (i + 1 < enfrentamientosRondaActual.size()) {
                    Enfrentamiento enfrentamiento1 = enfrentamientosRondaActual.get(i);
                    Enfrentamiento enfrentamiento2 = enfrentamientosRondaActual.get(i + 1);

                    // Obtener los ganadores
                    String ganador1 = enfrentamiento1.getResultado();
                    String ganador2 = enfrentamiento2.getResultado();

                    if (ganador1 != null && ganador2 != null) {
                        // Crear un nuevo enfrentamiento para la siguiente ronda
                        Enfrentamiento nuevoEnfrentamiento = new Enfrentamiento();
                        nuevoEnfrentamiento.setJugador1(usuarioRepository.findUsuarioByNombreUsuario(ganador1).orElse(null));
                        nuevoEnfrentamiento.setJugador2(usuarioRepository.findUsuarioByNombreUsuario(ganador2).orElse(null));
                        nuevoEnfrentamiento.setRonda(rondaActual + 1); // Establecer como la siguiente ronda
                        nuevoEnfrentamiento.setTorneo(torneo);

                        // Guardar el nuevo enfrentamiento en la base de datos
                        nuevosEnfrentamientos.add(enfrentamientoRepository.save(nuevoEnfrentamiento));
                    }
                }
            }
        }

        return nuevosEnfrentamientos;
    }
}
