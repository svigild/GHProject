package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.service.ForoService;
import com.example.demo.service.UsuarioService;
import com.example.demo.repository.ForoRepository;
import com.example.demo.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.*;

@Controller
@RequestMapping("/foros")
public class ForoController {

    @Autowired
    private ForoRepository foroRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ForoService foroService;

    @GetMapping
    public String listarForos(@RequestParam(required = false) String categoria,
                              @RequestParam(required = false, defaultValue = "desc") String orden,
                              Model model, Principal principal) {
        Sort sort = Sort.by(orden.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, "fechaCreacion");
        List<Foro> foros;

        if (categoria != null && !categoria.isEmpty()) {
            try {
                CategoriaEnum categoriaEnum = CategoriaEnum.valueOf(categoria.toUpperCase());
                foros = foroRepository.findByCategoria(categoriaEnum, sort);
                if (foros.isEmpty()) {
                    model.addAttribute("mensaje", "No hay foros disponibles en la categoría seleccionada.");
                }
            } catch (IllegalArgumentException e) {
                model.addAttribute("mensaje", "Categoría no válida.");
                foros = Collections.emptyList();
            }
        } else {
            foros = foroRepository.findAll(sort);
        }

        model.addAttribute("foros", foros);
        model.addAttribute("categorias", CategoriaEnum.values());
        model.addAttribute("categoriaSeleccionada", categoria);
        model.addAttribute("ordenSeleccionado", orden);

        if (principal != null) {
            Usuario usuarioActual = usuarioService.findByEmail(principal.getName());
            model.addAttribute("urlFotoPerfil", usuarioActual.getFotoPerfil());
            model.addAttribute("usuarioActual", usuarioActual);
        } else {
            model.addAttribute("username", "Invitado");
            model.addAttribute("urlFotoPerfil", "https://static.vecteezy.com/system/resources/thumbnails/005/544/718/small/profile-icon-design-free-vector.jpg");
        }

        return "foros/lista";
    }

    @GetMapping("/crear")
    public String mostrarFormularioCrearForo(Model model, Principal principal) {
        model.addAttribute("foro", new Foro());
        model.addAttribute("categorias", CategoriaEnum.values()); // Pasa las categorías a la vista

        if (principal != null) {
            Usuario usuarioActual = usuarioService.findByEmail(principal.getName());
            model.addAttribute("usuarioActual", usuarioActual);
            model.addAttribute("urlFotoPerfil", usuarioActual.getFotoPerfil());
        } else {
            model.addAttribute("username", "Invitado");
            model.addAttribute("urlFotoPerfil", "https://static.vecteezy.com/system/resources/thumbnails/005/544/718/small/profile-icon-design-free-vector.jpg");
        }

        return "foros/crear";
    }

    @PostMapping("/crear")
    public String crearForo(@ModelAttribute Foro foro, Principal principal) {
        Usuario usuario = usuarioRepository.findByEmail(principal.getName());
        foro.setUsuario(usuario);
        foro.setFechaCreacion(new Date());
        foroRepository.save(foro);
        return "redirect:/foros";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarForo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        foroService.eliminarForo(id);
        redirectAttributes.addFlashAttribute("successMessage", "Foro eliminado con éxito");
        return "redirect:/foros";
    }

    @GetMapping("/{id}")
    public String verForo(@PathVariable Long id, Model model, Principal principal) {
        Foro foro = foroService.obtenerForoPorId(id);
        if (foro != null) {
            model.addAttribute("foro", foro);

            if (principal != null) {
                Usuario usuarioActual = usuarioService.findByEmail(principal.getName());
                model.addAttribute("usuarioActual", usuarioActual);
                model.addAttribute("urlFotoPerfil", usuarioActual.getFotoPerfil());
            } else {
                model.addAttribute("username", "Invitado");
                model.addAttribute("urlFotoPerfil", "https://static.vecteezy.com/system/resources/thumbnails/005/544/718/small/profile-icon-design-free-vector.jpg");
            }

            model.addAttribute("comentarios", foro.getComentarios() != null ? foro.getComentarios() : List.of());

            return "foros/detalle_foro";
        }
        return "redirect:/foros";
    }

    @PostMapping("/{id}/comentarios")
    public String crearComentario(@PathVariable Long id, @ModelAttribute Comentario comentario, Principal principal) {
        // Obtener el usuario actual
        Usuario usuario = usuarioRepository.findByEmail(principal.getName());

        // Obtener el foro correspondiente al ID
        Foro foro = foroService.obtenerForoPorId(id);

        // Verificar que el foro existe
        if (foro != null) {
            // Configurar el comentario
            comentario.setUsuario(usuario);
            comentario.setForo(foro);
            comentario.setFechaComentario(new Date());
            // Guardar el comentario
            foroService.agregarComentario(comentario);
        }

        // Redirigir a la vista del foro
        return "redirect:/foros/" + id; // Redirigir al foro específico
    }


}
