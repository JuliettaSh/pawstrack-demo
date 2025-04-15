package com.example.demoPT.Controladores;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ControladoraSeguimiento {

    @GetMapping("/seguimiento")
    public String seguimiento() {
        return "Seguimiento"; // Nombre del archivo HTML sin extensión
    }
}
