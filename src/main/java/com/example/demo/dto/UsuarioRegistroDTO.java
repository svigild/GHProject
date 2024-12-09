package com.example.demo.dto;

import com.example.demo.model.RolNombre;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class UsuarioRegistroDTO {
    private String nombre;
    private String email;
    private String nombreUsuario;
    private String contrasenia;
    private String apellidos;
    private String pais;
    private String urlFotoPerfil;
    private RolNombre rol;

    // le doy formato a la fecha para evitar el error a la conversion de String a Date
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date fechaNacimiento;
}
