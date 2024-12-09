package com.example.demo.repository;

import com.example.demo.model.Torneo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TorneoRepository extends JpaRepository<Torneo, Long>, JpaSpecificationExecutor<Torneo> {

    @Query("SELECT DISTINCT t.nombreJuego FROM Torneo t")
    List<String> findDistinctNombreJuego();

    boolean existsTorneoByNombre(String nombre);
}