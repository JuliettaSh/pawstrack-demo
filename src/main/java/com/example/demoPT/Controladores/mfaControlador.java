/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demoPT.Controladores;

import com.example.demoPT.Modelo.Usuario;
import com.example.demoPT.Repositorios.RepositorioUsuario;
import com.example.demoPT.Servicios.ServicioMFA;
import jakarta.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import static org.springframework.web.server.ServerWebExchangeExtensionsKt.principal;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author gomez
 */
@Controller
public class mfaControlador {

    @Autowired
    private RepositorioUsuario userRepository;

    @Autowired
    private ServicioMFA mfaService;

    @GetMapping("/login/mfa-verify")
    public String showMfaVerify(@RequestParam String username, Model model) {
        model.addAttribute("username", username);
        return "mfa-verify";
    }

    @PostMapping("/login/mfa-verify")
    public String verifyMfaCode(
            @RequestParam String username,
            @RequestParam String code,
            RedirectAttributes redirectAttrs) {

        Usuario usuario = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        if (mfaService.verifyCode(usuario.getMfaSecret(), code)) {
            return "redirect:/home";
        } else {
            redirectAttrs.addAttribute("username", username);
            redirectAttrs.addFlashAttribute("error", "Código MFA incorrecto");
            return "redirect:/login/mfa-verify";
        }
    }

    @GetMapping("/register/mfa")
    public String showMfaSetup(@RequestParam String username, Model model) {
        String secret = mfaService.generateNewSecret();
        String qrUrl = mfaService.generateQRUrl(username, secret);

        model.addAttribute("qrUrl", qrUrl);
        model.addAttribute("secret", secret);
        model.addAttribute("username", username);
        return "mfa-setup";
    }
//    @GetMapping("/register/mfa")
//    public String showMfaSetup(@RequestParam String username, Model model) {
//        String secret = mfaService.generateNewSecret(); // Genera clave secreta
//
//        // Genera la URL para el QR (formato estándar TOTP)
//        String qrContent = String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s",
//                "PawsTrack",
//                username,
//                secret,
//                "PawsTrack");
//
//        // Codifica para URL y genera enlace para Google Charts
//        String qrUrl = "https://chart.googleapis.com/chart?chs=200x200&cht=qr&chl="
//                + URLEncoder.encode(qrContent, StandardCharsets.UTF_8);
//
//        model.addAttribute("qrUrl", qrUrl);
//        model.addAttribute("secret", secret);
//        return "mfa-setup";
//    }

    public String generateHumanFriendlySecret() {
        // Genera un código de 16 caracteres en bloques de 4
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"; // Base32
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 16; i++) {
            if (i > 0 && i % 4 == 0) {
                sb.append(" ");
            }
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }

        return sb.toString().replace(" ", "");
    }

    private String formatSecretForDisplay(String secret) {
        // Formato: XXXX XXXX XXXX XXXX
        return secret.replaceAll("(.{4})(?!$)", "$1 ");
    }
//    @PostMapping("/register/mfa/verify")
//public String verifyMfaSetup(
//    @RequestParam String username,
//    @RequestParam String secret,
//    @RequestParam int code,
//    HttpSession session,
//    RedirectAttributes redirectAttrs) {
//    
//    // Verifica que la sesión coincida con el usuario
//    if (!username.equals(session.getAttribute("mfa_setup_user"))) {
//        return "redirect:/register";
//    }
//    
//    if (mfaService.verifyCode(secret, code)) {
//        Usuario usuario = userRepository.findByUsername(username)
//            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
//        
//        usuario.setMfaSecret(secret);
//        usuario.setMfaEnabled(true);
//        userRepository.save(usuario);
//        
//        session.removeAttribute("mfa_setup_user");
//        redirectAttrs.addFlashAttribute("success", "MFA configurado correctamente");
//        return "redirect:/login";
//    } else {
//        redirectAttrs.addAttribute("username", username);
//        redirectAttrs.addFlashAttribute("error", "Código incorrecto");
//        return "redirect:/register/mfa";
//    }
//}
}
