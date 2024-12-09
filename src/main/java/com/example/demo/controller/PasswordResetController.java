package com.example.demo.controller;

import com.example.demo.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PasswordResetController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {
        if (usuarioService.requestPasswordReset(email)) {
            redirectAttributes.addFlashAttribute("message", "Se ha enviado un código de recuperación a tu email.");
            return "redirect:/reset-password";
        } else {
            redirectAttributes.addFlashAttribute("error", "No se encontró una cuenta con ese email.");
            return "redirect:/forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm() {
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("code") String code,
                                       @RequestParam("password") String password,
                                       RedirectAttributes redirectAttributes) {
        if (usuarioService.resetPassword(code, password)) {
            redirectAttributes.addFlashAttribute("successMessage", "Tu contraseña ha sido actualizada.");
            return "redirect:/login";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Código inválido o expirado.");
            return "redirect:/reset-password";
        }
    }
}
