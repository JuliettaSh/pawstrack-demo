package com.example.demoPT.Controladores;

import com.example.demoPT.Modelo.ImplUsuarioDetails;
import com.example.demoPT.Modelo.Publicacion;
import com.example.demoPT.Modelo.Seguimiento;
import com.example.demoPT.Modelo.Usuario;
import com.example.demoPT.Repositorios.RepositorioPublicaciones;
import com.example.demoPT.Repositorios.RepositorioSeguimiento;
import com.example.demoPT.Repositorios.RepositorioUsuario;
import com.example.demoPT.Servicios.SeguimientoServicio;
import com.example.demoPT.Servicios.ServicioPublicacion;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/seguimiento")
public class ControladorSeguimiento {

    private static final Logger logger = LoggerFactory.getLogger(ControladorSeguimiento.class);
    @Autowired
    private SeguimientoServicio seguimientoService;

    @Autowired
    private ServicioPublicacion publicacionService;

    @Autowired
    private RepositorioSeguimiento repoSegui;

    @Autowired
    private RepositorioUsuario repoUsu;

    @GetMapping
    public String verSeguimientos(HttpServletRequest request, Model model, @RequestParam(required = false) String section) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return "redirect:/login";
            }

            ImplUsuarioDetails userDetails = (ImplUsuarioDetails) authentication.getPrincipal();
            Usuario usuario = repoUsu.findById(userDetails.getUsuario().getId())
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

            model.addAttribute("usuario", usuario);

            List<Seguimiento> misSeguimientos = seguimientoService.obtenerSeguimientosUsuario(usuario);
            model.addAttribute("misSeguimientos", misSeguimientos);

            if ("todos-seguimientos".equals(section)) {
                model.addAttribute("todosSeguimientos", seguimientoService.obtenerTodosSeguimientos());
            }

            model.addAttribute("misPublicaciones", publicacionService.findByUsuario(usuario));
            model.addAttribute("section", section);
            model.addAttribute("usuarios", repoUsu.findAll()); // Agregar la lista de usuarios

            // Logging para depuración
            logger.info("Número de seguimientos: {}", misSeguimientos.size());
            for (Seguimiento seguimiento : misSeguimientos) {
                logger.info("Seguimiento ID: {}, Adoptante: {}", seguimiento.getId(), seguimiento.getAdoptante());
            }

            return "seguimiento/index";
        } catch (Exception e) {
            return "redirect:/login?error";
        }
    }

    @GetMapping("/editar/{id}")
    public String editarSeguimiento(@PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario,
            Model model) throws AccessDeniedException {
        Seguimiento seguimiento = seguimientoService.buscarPorIdYUsuario(id, usuario)
                .orElseThrow(() -> new AccessDeniedException("Acceso denegado"));

        model.addAttribute("seguimientoEditar", seguimiento);
        model.addAttribute("estadosPosibles", Arrays.asList("Pendiente", "En proceso", "Completado"));
        model.addAttribute("section", "editar-seguimiento"); // Agregar esto
        return "seguimiento/index";
    }

    @GetMapping("/lista")
    public String listarSeguimientos(Model model,
            @AuthenticationPrincipal Usuario usuario) {
        model.addAttribute("seguimientos",
                seguimientoService.obtenerSeguimientosUsuario(usuario));
        return "seguimiento/lista";
    }

    @PostMapping("/iniciar/{publicacionId}")
    public String iniciarSeguimiento(
            @PathVariable Long publicacionId,
            @AuthenticationPrincipal Usuario adoptante,
            RedirectAttributes redirectAttributes) {

        logger.info("Iniciando seguimiento para la publicación ID: {}", publicacionId);
        logger.info("Adoptante: {}", adoptante != null ? adoptante.getUsername() : "null");

        try {
            Publicacion publicacion = publicacionService.findById(publicacionId)
                    .orElseThrow(() -> {
                        logger.error("Publicación no encontrada con ID: {}", publicacionId);
                        return new ResourceNotFoundException("Publicación no encontrada");
                    });

            logger.info("Publicación encontrada: {}", publicacion.getNombreMascota());
            if (seguimientoService.existeSeguimiento(publicacion, adoptante)) {
                redirectAttributes.addFlashAttribute("error", "Ya existe un seguimiento para esta publicación");
                logger.warn("Ya existe un seguimiento para esta publicación");
                return "redirect:/home";
            }

            Seguimiento seguimiento = new Seguimiento();
            seguimiento.setPublicacion(publicacion);
            seguimiento.setAdoptante(adoptante);
            seguimiento.setResponsable(publicacion.getUsuario());
            seguimiento.setEstado("Pendiente");

            seguimientoService.guardarSeguimiento(seguimiento);

            logger.info("Seguimiento guardado");

            String whatsappLink = construirEnlaceWhatsApp(publicacion);
            logger.info("Enlace de WhatsApp: {}", whatsappLink);
            // Redirige directamente a WhatsApp
            return "redirect:" + whatsappLink;

        } catch (Exception e) {
            logger.error("Error al crear seguimiento: ", e);
            redirectAttributes.addFlashAttribute("error", "Error al crear seguimiento: " + e.getMessage());
            return "redirect:/home";
        }

    }

    private String construirEnlaceWhatsApp(Publicacion publicacion) {
    String nombreMascota = publicacion.getNombreMascota() != null
            ? publicacion.getNombreMascota() : "tu mascota";

    String telefono = "54" + publicacion.getTelefono();
    String mensaje = "Hola! Vi tu publicación de " + nombreMascota + " en PawsTrack y me interesa adoptarla. ¿Podríamos conversar al respecto?";
    String mensajeCodificado = URLEncoder.encode(mensaje, StandardCharsets.UTF_8);

    String whatsappLink = "https://wa.me/" + telefono + "?text=" + mensajeCodificado;

    logger.info("Teléfono: {}", telefono);
    logger.info("Mensaje: {}", mensaje);
    logger.info("Mensaje Codificado: {}", mensajeCodificado);
    logger.info("Enlace de WhatsApp: {}", whatsappLink);

    return whatsappLink;
}

    @PostMapping("/actualizar/{id}")
    public String actualizarSeguimiento(
            @PathVariable Long id,
            @RequestParam String estado,
            @RequestParam(required = false) String observaciones,
            @AuthenticationPrincipal Usuario usuario,
            RedirectAttributes redirectAttributes) {

        try {
            seguimientoService.actualizarSeguimiento(id, estado, observaciones, usuario);
            redirectAttributes.addFlashAttribute("success", "Seguimiento actualizado");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar: " + e.getMessage());
        }

        return "redirect:/seguimiento";
    }
}
//    @GetMapping
//    public String verSeguimientos(HttpServletRequest request, Model model) {
//        try {
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//            if (authentication == null || !authentication.isAuthenticated()) {
//                return "redirect:/login";
//            }
//
//            ImplUsuarioDetails userDetails = (ImplUsuarioDetails) authentication.getPrincipal();
//            Usuario usuario = repoUsu.findById(userDetails.getUsuario().getId())
//                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
//
//            // Cargar datos necesarios
//            model.addAttribute("misPublicaciones", publicacionService.findByUsuario(usuario));
//            model.addAttribute("misSeguimientos", seguimientoService.obtenerSeguimientosUsuario(usuario));
//            model.addAttribute("todosSeguimientos", seguimientoService.obtenerTodosSeguimientos());
//            model.addAttribute("usuario", usuario); // Añadir usuario al modelo
//
//            return "seguimiento/index";
//        } catch (Exception e) {
//            return "redirect:/login?error";
//        }
//    }
//
//
//@GetMapping("/editar/{id}")
//public String editarSeguimiento(@PathVariable Long id,
//            @AuthenticationPrincipal Usuario usuario,
//            Model model) throws AccessDeniedException {
//        Seguimiento seguimiento = seguimientoService.buscarPorIdYUsuario(id, usuario)
//                .orElseThrow(() -> new AccessDeniedException("Acceso denegado"));
//
//        model.addAttribute("seguimientoEditar", seguimiento);
//        model.addAttribute("estadosPosibles", Arrays.asList("Pendiente", "En proceso", "Completado"));
//        return "seguimiento/index";
//    }
//
//    @GetMapping("/lista")
//public String listarSeguimientos(Model model,
//            @AuthenticationPrincipal Usuario usuario) {
//        model.addAttribute("seguimientos",
//                seguimientoService.obtenerSeguimientosUsuario(usuario));
//        return "seguimiento/lista";
//    }
//
// @PostMapping("/iniciar/{publicacionId}")
//@ResponseBody
//public ResponseEntity<?> iniciarSeguimiento(
//        @PathVariable Long publicacionId,
//        @AuthenticationPrincipal Usuario adoptante) {
//    
//    try {
//        Publicacion publicacion = publicacionService.findById(publicacionId)
//                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada"));
//
//        // Verificar si ya existe un seguimiento
//        if (seguimientoService.existeSeguimiento(publicacion, adoptante)) {
//            return ResponseEntity.status(HttpStatus.CONFLICT)
//                    .body(Map.of(
//                        "success", false,
//                        "message", "Ya existe un seguimiento para esta publicación"
//                    ));
//        }
//        
//        // Crear nuevo seguimiento
//        Seguimiento seguimiento = new Seguimiento();
//        seguimiento.setPublicacion(publicacion);
//        seguimiento.setAdoptante(adoptante);
//        seguimiento.setResponsable(publicacion.getUsuario());
//        seguimiento.setFecha(LocalDate.now());
//        seguimiento.setEstado("Pendiente");
//        
//        seguimiento = seguimientoService.guardarSeguimiento(seguimiento);
//        
//        // Construir enlace de WhatsApp
//        String whatsappLink = construirEnlaceWhatsApp(publicacion);
//        
//        return ResponseEntity.ok(Map.of(
//            "success", true,
//            "message", "Seguimiento creado exitosamente",
//            "whatsappLink", whatsappLink,
//            "seguimientoId", seguimiento.getId()
//        ));
//        
//    } catch (Exception e) {
//        return ResponseEntity.internalServerError()
//                .body(Map.of(
//                    "success", false,
//                    "message", "Error al crear seguimiento: " + e.getMessage()
//                ));
//    }
//}
//
//private String construirEnlaceWhatsApp(Publicacion publicacion) {
//    String nombreMascota = publicacion.getNombreMascota() != null ? 
//            publicacion.getNombreMascota() : "tu mascota";
//    
//    return "https://wa.me/54" + publicacion.getTelefono() + 
//           "?text=Hola!%20Vi%20tu%20publicación%20de%20" + 
//           URLEncoder.encode(nombreMascota, StandardCharsets.UTF_8) + 
//           "%20en%20PawsTrack%20y%20me%20interesa%20adoptarla.%20¿Podríamos%20conversar%20al%20respecto?";
//}
////    @GetMapping("/editar/{id}")
////    public String mostrarFormularioEdicion(@PathVariable Long id,
////                                         @AuthenticationPrincipal Usuario usuario,
////                                         Model model) throws AccessDeniedException {
////        Seguimiento seguimiento = seguimientoService.buscarPorIdYUsuario(id, usuario)
////            .orElseThrow(() -> new AccessDeniedException("Acceso denegado"));
////        
////        model.addAttribute("seguimiento", seguimiento);
////        return "seguimiento/editar";
////    }
//
//    @PostMapping("/actualizar/{id}")
//public String actualizarSeguimiento(
//        @PathVariable Long id,
//        @RequestParam String estado,
//        @RequestParam(required = false) String observaciones,
//        @AuthenticationPrincipal Usuario usuario,
//        RedirectAttributes redirectAttributes) {
//    
//    try {
//        Seguimiento seguimiento = seguimientoService.buscarPorIdYUsuario(id, usuario)
//                .orElseThrow(() -> new AccessDeniedException("Acceso denegado"));
//        
//        seguimiento.setEstado(estado);
//        if (observaciones != null) {
//            seguimiento.setObservaciones(observaciones);
//        }
//        
//        seguimientoService.guardarSeguimiento(seguimiento);
//        redirectAttributes.addFlashAttribute("success", "Seguimiento actualizado");
//    } catch (Exception e) {
//        redirectAttributes.addFlashAttribute("error", "Error al actualizar: " + e.getMessage());
//    }
//    
//    return "redirect:/seguimiento/lista";
//}
//}
