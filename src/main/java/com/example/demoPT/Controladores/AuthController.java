package com.example.demoPT.Controladores;

import com.example.demoPT.Modelo.Publicacion;
import com.example.demoPT.Modelo.Usuario;
import com.example.demoPT.Repositorios.RepositorioPublicaciones;
import com.example.demoPT.Repositorios.RepositorioUsuario;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller

public class AuthController {

    private final RepositorioUsuario userRepository;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private RepositorioPublicaciones repo;

    public AuthController(RepositorioUsuario userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")//el usuario quiere ir al login y muestra el html
    public String mostrarLogin() {
        return "login";
    }

    @GetMapping("/register")//el usuario quiere ir al registro y muestra el html
    public String mostrarRegistro() {
        return "register";
    }

    @GetMapping("/home")//el usuario quiere ir al home (una vez autenticado) y muestra el html
    public String mostrarHome(Model modelo) {
        List<Publicacion> publicaciones = repo.findAll();//muestra todas las publicacines subidas
        //modelo.addAttribute("publicaciones", publicaciones);
        // Asegurar que la lista no sea nula
        if (publicaciones == null || publicaciones.isEmpty()) {
            modelo.addAttribute("publicaciones", new ArrayList<>());
        } else {
            modelo.addAttribute("publicaciones", publicaciones);
        }
        return "home";
    }

    @GetMapping("/index")//este es el apartado de "mis publicaciones" donde solo se ven las publicaciones que ese usuario hizo
    public String mostrarIndex(Model modelo, Principal principal) {
        // Obtener el nombre de usuario autenticado
        String username = principal.getName();

        // Buscar publicaciones solo del usuario autenticado
        List<Publicacion> publicaciones = repo.findByUsuarioUsername(username);

        modelo.addAttribute("publicaciones", publicaciones);
        //y devuelve la pantalla
        return "index";
    }

    @PostMapping("/register")
    public String registrarUsuario(Usuario usuario) {
        //aca se registra el usuario y se guardan sus datos para redirigirlo al login
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        userRepository.save(usuario);
        //mostramos el registro
        return "register";
    }
}
