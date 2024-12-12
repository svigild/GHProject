package com.example.demo.controller;

import com.example.demo.model.Usuario;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
public class NewsController {

    @Autowired
    private NewsService newsService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/noticias")
    public String getNoticias(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String datePublished,
            @RequestParam(required = false) String sortBy,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        // Si no hay filtros, se muestran noticias por defecto (videojuegos)
        Map<String, Object> noticias;
        if (keyword == null && datePublished == null && sortBy == null) {
            noticias = newsService.getVideoGameNews();  // Noticias por defecto
        } else {
            // Si se aplican filtros, se llaman a las noticias filtradas
            noticias = newsService.getFilteredNews(keyword, datePublished, sortBy);
        }



        model.addAttribute("noticias", noticias.get("articles"));
        model.addAttribute("keyword", keyword); // Añadir keyword al modelo
        model.addAttribute("datePublished", datePublished); // Añadir datePublished al modelo
        model.addAttribute("sortBy", sortBy); // Añadir sortBy al modelo

        // Si hay usuario autenticado, se muestra su información, de lo contrario se asigna información de invitado
        if (userDetails != null) {
            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername());
            model.addAttribute("username", userDetails.getUsername());
            model.addAttribute("urlFotoPerfil", usuario.getFotoPerfil());
        } else {
            model.addAttribute("username", "Invitado");
            model.addAttribute("urlFotoPerfil", "https://static.vecteezy.com/system/resources/thumbnails/005/544/718/small/profile-icon-design-free-vector.jpg");
        }

        return "noticias";
    }


}