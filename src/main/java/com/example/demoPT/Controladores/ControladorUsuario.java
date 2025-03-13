package com.example.demoPT.Controladores;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuarios")
public class ControladorUsuario {
//esto creo que no hace falta
    @GetMapping("/user")
    public String user() {
        return "publicaciones/index";
    }

    @GetMapping("/admin")
    public String admin() {
        return "Bienvenido administrador";
    }

}
