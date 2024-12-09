package com.example.demo.repository;

import com.example.demo.model.Enfrentamiento;
import com.example.demo.model.Foro;
import com.example.demo.model.Torneo;
import com.example.demo.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnfrentamientoRepository extends JpaRepository<Enfrentamiento, Long> {
    List<Enfrentamiento> findByTorneoOrderByRondaAsc(Torneo torneo);

    void deleteByTorneoAndJugador1OrJugador2(Torneo torneo, Usuario jugador1, Usuario jugador2);
}
