package com.example.demoPT.Controladores;

import com.example.demoPT.Modelo.CambioContrasenaDTO;
import com.example.demoPT.Modelo.Usuario;
import com.example.demoPT.Servicios.ServicioUsuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/perfil")
public class ControladorPerfil {

    private final ServicioUsuario servicioUsuario;
    
    public ControladorPerfil(ServicioUsuario servicioUsuario) {
        this.servicioUsuario = servicioUsuario;
    }

    @GetMapping
    public String mostrarPerfil(Model model) {
        Usuario usuario = servicioUsuario.getUsuarioActual();
        model.addAttribute("usuario", usuario);
        return "Perfil";
    }

    @PostMapping("/actualizar")
    public String actualizarPerfil(@ModelAttribute Usuario usuario, RedirectAttributes redirectAttributes) {
        try {
            servicioUsuario.actualizarUsuario(usuario);
            redirectAttributes.addFlashAttribute("success", "Perfil actualizado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar: " + e.getMessage());
        }
        return "redirect:/perfil";
    }

    @PostMapping("/cambiar-contrasena")
    public String cambiarContrasena(@ModelAttribute CambioContrasenaDTO datos, RedirectAttributes redirectAttributes) {
        if (!datos.getNewPassword().equals(datos.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden");
            return "redirect:/perfil";
        }

        if (servicioUsuario.cambiarContrasena(datos)) {
            redirectAttributes.addFlashAttribute("success", "Contraseña cambiada correctamente");
        } else {
            redirectAttributes.addFlashAttribute("error", "Contraseña actual incorrecta");
        }
        return "redirect:/perfil";
    }

//    @PostMapping("/privacidad")
//    public String actualizarPrivacidad(
//            @RequestParam(required = false) boolean perfilPublico,
//            @RequestParam(required = false) boolean mostrarCorreo,
//            @RequestParam(required = false) boolean permitirMensajes) {
//        usuarioService.actualizarPrivacidad(perfilPublico, mostrarCorreo, permitirMensajes);
//        return "redirect:/perfil?exitoPrivacidad";
//    }
    @PostMapping("/borrar")
    public String eliminarCuenta(HttpServletRequest request, HttpServletResponse response,
            RedirectAttributes redirectAttributes) {
        try {
            servicioUsuario.eliminarCuenta(request, response);
            redirectAttributes.addFlashAttribute("cuentaEliminada", true);
            return "redirect:/login?cuentaEliminada";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar la cuenta"+ e.getMessage());
            return "redirect:/perfil";
        }
    }
    
}
