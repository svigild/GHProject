package com.example.demo.repository;

import com.example.demo.model.CategoriaEnum;
import com.example.demo.model.Foro;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForoRepository extends JpaRepository<Foro, Long> {
    List<Foro> findAllByOrderByFechaCreacionAsc();
    List<Foro> findAllByOrderByFechaCreacionDesc();
    List<Foro> findByCategoria(CategoriaEnum categoria);
    List<Foro> findByCategoria(CategoriaEnum categoria, Sort sort);
    List<Foro> findByCategoriaOrderByFechaCreacion(CategoriaEnum categoria, Sort sort);
    // Métodos separados para cada orden
    List<Foro> findByCategoriaOrderByFechaCreacionAsc(CategoriaEnum categoria);
    List<Foro> findByCategoriaOrderByFechaCreacionDesc(CategoriaEnum categoria);
}
