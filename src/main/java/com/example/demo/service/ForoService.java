package com.example.demo.service;

import com.example.demo.model.Comentario;
import com.example.demo.model.Foro;
import com.example.demo.model.Usuario;
import com.example.demo.repository.ComentarioRepository;
import com.example.demo.repository.ForoRepository;
import com.example.demo.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ForoService {

    @Autowired
    private final ForoRepository foroRepository;

    @Autowired
    private ComentarioRepository comentarioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    public ForoService(ForoRepository foroRepository) {
        this.foroRepository = foroRepository;
    }

    public List<Foro> obtenerTodosLosForos() {
        return foroRepository.findAll();
    }

    public Foro obtenerForoPorId(Long id) {
        return foroRepository.findById(id).orElse(null);
    }

    public Foro crearForo(Foro foro) {
        return foroRepository.save(foro);
    }

    public void eliminarForo(Long id) {
        foroRepository.deleteById(id);
    }

    // Método para agregar un comentario a un foro
    public Comentario agregarComentario(Comentario comentario) {

        return comentarioRepository.save(comentario); // Guardar el comentario
    }

    public void guardarForo (Foro foro) {
        foroRepository.save(foro);
    }
}
