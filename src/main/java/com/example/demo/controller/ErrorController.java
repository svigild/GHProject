package com.example.demo.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorController {

    @GetMapping("/403")
    public String error403(Model model) {
        model.addAttribute("errorMessage", "No tienes permisos para acceder a esta página.");
        return "403";
    }

    @GetMapping("/404")
    public String error404(Model model) {
        model.addAttribute("errorMessage", "La página que buscas no existe en GameHub.");
        return "404";
    }


}