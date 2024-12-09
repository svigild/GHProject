package com.example.demo.controller;

import com.example.demo.model.Usuario;
import com.example.demo.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class InfoController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/terminos")
    public String terminos(Principal principal, Model model) {

        if (principal != null) {
            Usuario usuarioActual = usuarioService.findByEmail(principal.getName());
            model.addAttribute("urlFotoPerfil", usuarioActual.getFotoPerfil());
            model.addAttribute("usuarioActual", usuarioActual);
        } else {
            model.addAttribute("username", "Invitado");
            model.addAttribute("urlFotoPerfil", "https://static.vecteezy.com/system/resources/thumbnails/005/544/718/small/profile-icon-design-free-vector.jpg");
        }

        return "terminos"; // Devuelve la vista para los términos de servicio
    }

    @GetMapping("/politica-privacidad")
    public String politicaPrivacidad(Principal principal, Model model) {
        if (principal != null) {
            Usuario usuarioActual = usuarioService.findByEmail(principal.getName());
            model.addAttribute("urlFotoPerfil", usuarioActual.getFotoPerfil());
            model.addAttribute("usuarioActual", usuarioActual);
        } else {
            model.addAttribute("username", "Invitado");
            model.addAttribute("urlFotoPerfil", "https://static.vecteezy.com/system/resources/thumbnails/005/544/718/small/profile-icon-design-free-vector.jpg");
        }

        return "politica_privacidad"; // Devuelve la vista para la política de privacidad
    }

    @GetMapping("/contacto")
    public String contacto(Principal principal, Model model) {
        if (principal != null) {
            Usuario usuarioActual = usuarioService.findByEmail(principal.getName());
            model.addAttribute("urlFotoPerfil", usuarioActual.getFotoPerfil());
            model.addAttribute("usuarioActual", usuarioActual);
        } else {
            model.addAttribute("username", "Invitado");
            model.addAttribute("urlFotoPerfil", "https://static.vecteezy.com/system/resources/thumbnails/005/544/718/small/profile-icon-design-free-vector.jpg");
        }

        return "contacto"; // Devuelve la vista para la página de contacto
    }


}
