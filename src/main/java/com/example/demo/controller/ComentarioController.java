package com.example.demo.controller;

import com.example.demo.model.Comentario;
import com.example.demo.model.Foro;
import com.example.demo.model.Usuario;
import com.example.demo.repository.ComentarioRepository;
import com.example.demo.repository.ForoRepository;
import com.example.demo.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/comentarios")
public class ComentarioController {

    @Autowired
    private ComentarioRepository comentarioRepository;

    @Autowired
    private ForoRepository foroRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/crear/{foroId}")
    public String crearComentario(@PathVariable Long foroId, @RequestParam String contenido, Principal principal) {
        Foro foro = foroRepository.findById(foroId).orElseThrow(() -> new IllegalArgumentException("Foro no encontrado"));
        Usuario usuario = usuarioRepository.findByEmail(principal.getName());

        Comentario comentario = new Comentario(contenido, foro, usuario);
        comentarioRepository.save(comentario);

        return "redirect:/foros";
    }
}