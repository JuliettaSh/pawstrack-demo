package com.example.demoPT.Controladores;

import com.example.demoPT.Modelo.Publicacion;
import com.example.demoPT.Modelo.Publicacion.Edad;
import com.example.demoPT.Modelo.Publicacion.Especie;
import com.example.demoPT.Modelo.Publicacion.Tamanio;
import com.example.demoPT.Modelo.Usuario;
import com.example.demoPT.Repositorios.RepositorioPublicaciones;
import com.example.demoPT.Repositorios.RepositorioUsuario;
import com.example.demoPT.Servicios.ServicioMFA;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JOptionPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller

public class AuthController {

    private final RepositorioUsuario userRepository;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private RepositorioPublicaciones repo;
    @Autowired
    private ServicioMFA mfaService;

    @Autowired
    private AuthenticationManager authenticationManager;

    public AuthController(RepositorioUsuario userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/")
    public String redirigirALogin() {
        return "redirect:/login";
    }

    @GetMapping("/login")//el usuario quiere ir al login y muestra el html
    public String mostrarLogin(Model model, CsrfToken token) {
        if (token != null) {
            model.addAttribute("_csrf", token);
        }
        return "login";
    }
//    @GetMapping("/login")
//    public String mostrarLogin() {
//        return "login";
//    }

    @PostMapping("/login")
    public String processLogin(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(required = false) boolean remember,
            HttpServletRequest request) {

        try {
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    username, password);

            Authentication authentication = authenticationManager.authenticate(auth);

            // Establece la autenticación en el contexto de seguridad
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Crea una nueva sesión
            HttpSession session = request.getSession(true);

            return "redirect:/home";
        } catch (AuthenticationException e) {
            // Maneja el error de autenticación
            return "redirect:/login?error";
        }
    }

    @PostMapping("/login/mfa")
    public String verifyMfaLogin(
            @RequestParam String username,
            @RequestParam String code,
            RedirectAttributes redirectAttrs) {

        Usuario usuario = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        if (mfaService.verifyCode(usuario.getMfaSecret(), code)) {
            return "redirect:/home";
        } else {
            redirectAttrs.addAttribute("username", username);
            redirectAttrs.addAttribute("mfaNeeded", true);
            redirectAttrs.addFlashAttribute("error", "Código incorrecto");
            return "redirect:/login";
        }
    }

    @GetMapping("/register")//el usuario quiere ir al registro y muestra el html
    public String mostrarRegistro() {
        return "register";
    }

    @PostMapping("/register/mfa/verify")
    public String verifyMfaSetup(
            @RequestParam String username,
            @RequestParam String secret,
            @RequestParam String code,
            RedirectAttributes redirectAttrs) {

        if (!mfaService.verifyCode(secret, code)) {
            redirectAttrs.addAttribute("username", username);
            redirectAttrs.addFlashAttribute("error", "Código inválido. Debe ser de 6 dígitos");
            return "redirect:/register/mfa";
        }

        // Guardar el secreto y activar MFA
        Usuario usuario = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        usuario.setMfaSecret(secret);
        usuario.setMfaEnabled(true);
        userRepository.save(usuario);

        return "redirect:/home";
    }

    @GetMapping("/home")
    public String mostrarHome(
            @RequestParam(required = false) String especie,
            @RequestParam(required = false) String edad,
            @RequestParam(required = false) String tamanio,
            @RequestParam(required = false) String departamento,
            Model model) {

        // Inicializa listas vacías por defecto
        List<Publicacion> publicaciones = new ArrayList<>();

        try {
            // Convertir strings a enums con validación
            Especie especieEnum = null;
            if (especie != null && !especie.isEmpty()) {
                especieEnum = Especie.valueOf(especie.toUpperCase());
            }

            Edad edadEnum = null;
            if (edad != null && !edad.isEmpty()) {
                edadEnum = Edad.valueOf(edad.toUpperCase());
            }

            Tamanio tamanioEnum = null;
            if (tamanio != null && !tamanio.isEmpty()) {
                tamanioEnum = Tamanio.valueOf(tamanio.toUpperCase());
            }

            publicaciones = repo.findByFiltros(
                    especieEnum,
                    edadEnum,
                    tamanioEnum,
                    departamento);

        } catch (IllegalArgumentException e) {
            // Maneja error de conversión de enum
            model.addAttribute("error", "Valor de filtro no válido");
        } catch (Exception e) {
            // Maneja otros errores
            model.addAttribute("error", "Error al filtrar publicaciones");
        }

        // Asegúrate de que nunca sea null
        if (publicaciones == null) {
            publicaciones = new ArrayList<>();
        }

        model.addAttribute("publicaciones", publicaciones);
        for (Publicacion p : publicaciones) {
            System.out.println("Publicación: " + p.getId_publicacion() + " - Estado: " + p.getEstado());
        }

        return "home";
    }
//    @GetMapping("/home")//el usuario quiere ir al home (una vez autenticado) y muestra el html
//    public String mostrarHome(Model modelo) {
//        
//        List<Publicacion> publicaciones = repo.findAll();//muestra todas las publicacines subidas
//        //modelo.addAttribute("publicaciones", publicaciones);
//        // Asegurar que la lista no sea nula
//        if (publicaciones == null || publicaciones.isEmpty()) {
//            modelo.addAttribute("publicaciones", new ArrayList<>());
//        } else {
//            modelo.addAttribute("publicaciones", publicaciones);
//        }
//        return "home";
//    }

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
    public String registrarUsuario(Usuario usuario, HttpServletRequest request) {
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setEnabled(true); // IMPORTANTE: Habilitar temporalmente
        userRepository.save(usuario);

        // Autentica automáticamente
        authenticateUserAndSetSession(usuario.getUsername(), usuario.getPassword(), request);

        return "redirect:/register/mfa?username=" + usuario.getUsername();
    }

    private void authenticateUserAndSetSession(String username, String password, HttpServletRequest request) {

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                username, password);
        Authentication authentication = authenticationManager.authenticate(auth);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Crea una nueva sesión
        request.getSession(true);
    }
}
