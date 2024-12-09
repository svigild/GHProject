package com.example.demo.controller;

import com.example.demo.dto.BibliotecaJuegoDTO;
import com.example.demo.model.BibliotecaJuego;
import com.example.demo.model.Usuario;
import com.example.demo.repository.BibliotecaJuegoRepository;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.service.BibliotecaJuegoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/biblioteca")
public class BibliotecaJuegoController {

    @Autowired
    private BibliotecaJuegoRepository bibliotecaJuegoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BibliotecaJuegoService bibliotecaJuegoService;

    @GetMapping("")
    public String verBiblioteca(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername());
        List<BibliotecaJuego> juegos = bibliotecaJuegoRepository.findByUsuario(usuario);
        model.addAttribute("juegos", juegos);
        model.addAttribute("urlFotoPerfil", usuario.getFotoPerfil());
        return "biblioteca";
    }

    @GetMapping("/agregar")
    public String mostrarFormularioAgregarJuego(Model model) {
        model.addAttribute("bibliotecaJuegoDTO", new BibliotecaJuegoDTO());
        return "agregar-juego";
    }

    @PostMapping("/agregar")
    public String agregarJuego(@RequestParam("usuarioId") Long usuarioId, @ModelAttribute BibliotecaJuegoDTO bibliotecaJuegoDTO) {
        // Obtener el nombre de usuario por su id
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));

        // Crear el juego y guardar en la biblioteca
        BibliotecaJuego juego = new BibliotecaJuego();
        juego.setNombreJuego(bibliotecaJuegoDTO.getNombreJuego());
        juego.setImagenJuego(bibliotecaJuegoDTO.getImagenJuego());
        juego.setGenero(bibliotecaJuegoDTO.getGenero());
        juego.setAnioSalida(bibliotecaJuegoDTO.getAnioSalida());
        juego.setUsuario(usuario); // Establecer el usuario en el juego
        bibliotecaJuegoRepository.save(juego);

        return "redirect:/biblioteca/" + usuario.getId(); // Redirigir a la biblioteca del usuario
    }

    @GetMapping("/{id}")
    public String verBiblioteca(@PathVariable("id") Long id, Model model) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
        List<BibliotecaJuego> juegos = bibliotecaJuegoRepository.findByUsuarioId(id);
        model.addAttribute("juegos", juegos);
        return "biblioteca";
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<String> eliminarJuego(@PathVariable Long id) {
        try {
            bibliotecaJuegoRepository.deleteById(id);
            return ResponseEntity.ok("Juego eliminado correctamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar el juego: " + e.getMessage());
        }
    }
}