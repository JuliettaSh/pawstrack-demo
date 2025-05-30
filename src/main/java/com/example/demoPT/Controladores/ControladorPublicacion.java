package com.example.demoPT.Controladores;

import com.example.demoPT.Modelo.Publicacion;
import com.example.demoPT.Modelo.PublicacionDto;
import com.example.demoPT.Modelo.Usuario;
import com.example.demoPT.Repositorios.RepositorioPublicaciones;
import com.example.demoPT.Repositorios.RepositorioUsuario;
import jakarta.validation.Valid;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.UUID;
import javax.swing.JOptionPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/publicaciones")
public class ControladorPublicacion {

    @Autowired
    private RepositorioPublicaciones repo;
    @Autowired
    private RepositorioUsuario repoU;

    @GetMapping("/crear")//cuando seleccionamos el boton para crear publicaciones, se redirige a esta pantalla
    public String mostrarPaginaCrear(Model modelo) {
        // Verifica si el usuario está autenticado
        if (SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            PublicacionDto publiDto = new PublicacionDto();
            modelo.addAttribute("publiDto", publiDto);
            return "publicaciones/CrearPublicacion";  // Si está autenticado, muestra la página para crear publicación
        } else {
            JOptionPane.showMessageDialog(null, "Usuario no autenticado");//mensajede error
            return "redirect:/login";  // Si no está autenticado, redirige al login
        }
    }

    @PostMapping("/crear")//una vez que vimos que esta autenticado pasamos a la creacion de la publicacion
    public String crearPublicacion(@ModelAttribute("publiDto") PublicacionDto publiDto, @RequestParam("archivoFoto") MultipartFile archivoFoto, Principal principal) {
        // Obtener el usuario autenticado

//        if (resultado.hasErrors()) {
//            return "publicaciones/CrearPublicacion";
//        }
        try {
            String username = principal.getName();
            Usuario usuario = repoU.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Publicacion publicacion = new Publicacion();
            publicacion.setNombreMascota(publiDto.getNombre_mascota());
            //publicacion.setDireccion(publiDto.getDireccion());
            publicacion.setTelefono(publiDto.getTelefono());
            publicacion.setDescripcion(publiDto.getDescripcion());
            publicacion.setUsuario(usuario);

            // Nuevos campos
            publicacion.setEspecie(Publicacion.Especie.valueOf(publiDto.getEspecie()));
            publicacion.setEdad(Publicacion.Edad.valueOf(publiDto.getEdad()));
            publicacion.setTamanio(Publicacion.Tamanio.valueOf(publiDto.getTamanio()));
            publicacion.setDepartamento(publiDto.getDepartamento());
            publicacion.setEstado(Publicacion.Estado.valueOf(publiDto.getEstado()));

            // Guardar la foto 
            if (publiDto.getArchivoFoto() != null && !publiDto.getArchivoFoto().isEmpty()) {
                MultipartFile foto = publiDto.getArchivoFoto();
                //String nombreFoto = foto.getOriginalFilename();
                String nombreFoto = UUID.randomUUID().toString() + "_" + foto.getOriginalFilename();

                publicacion.setArchivoFoto(nombreFoto);

                // Guardar la imagen en la ruta
                Path rutaFoto = Paths.get("public/imagenes/" + nombreFoto);
                try (InputStream inputStream = foto.getInputStream()) {
                    Files.copy(inputStream, rutaFoto, StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    System.out.println("Error al guardar la imagen: " + e.getMessage());
                }
            }
//        if (!archivoFoto.isEmpty()) {
//            String nombreArchivo = archivoFoto.getOriginalFilename();
//            publicacion.setArchivoFoto(nombreArchivo);
//        }

            repo.save(publicacion);
            //redirigimoa a la pantalla de "mis publicaciones"
            return "redirect:/index";

        } catch (Exception e) {
            System.out.println("Error al crear publicación: " + e.getMessage());
            return "redirect:/publicaciones/crear";
        }
    }

    @GetMapping("/editar")
    public String mostrarPaginaEditar(Model modelo, @RequestParam Long id) {
        try {
            Publicacion publicacion = repo.findById(id).get();
            modelo.addAttribute("publicacion", publicacion);

            PublicacionDto publiDto = new PublicacionDto();
            publiDto.setNombre_mascota(publicacion.getNombreMascota());
            publiDto.setTelefono(publicacion.getTelefono());
            publiDto.setDescripcion(publicacion.getDescripcion());

            // Mapear los nuevos campos al DTO
            publiDto.setEspecie(publicacion.getEspecie().name());
            publiDto.setEdad(publicacion.getEdad().name());
            publiDto.setTamanio(publicacion.getTamanio().name());
            publiDto.setDepartamento(publicacion.getDepartamento());
            publiDto.setEstado(publicacion.getEstado().name());

            modelo.addAttribute("publiDto", publiDto);
        } catch (Exception e) {
            System.out.println("Excepcion: " + e.getMessage());
            return "redirect:/index";
        }
        return "publicaciones/EditarPublicacion";
    }

    @PostMapping("/editar")
    public String actualizarPublicacion(
            Model modelo,
            @RequestParam Long id,
            @Valid @ModelAttribute PublicacionDto publicDto,
            BindingResult resultado,
            RedirectAttributes redirectAttributes) {

        try {
            Publicacion publicacion = repo.findById(id).get();
            modelo.addAttribute("publicacion", publicacion);

            if (resultado.hasErrors()) {
                return "publicaciones/EditarPublicacion";
            }

            if (publicDto.getArchivoFoto() != null && !publicDto.getArchivoFoto().isEmpty()) {
                MultipartFile foto = publicDto.getArchivoFoto();
                String nombreFoto = foto.getOriginalFilename();
                publicacion.setArchivoFoto(nombreFoto);

                Path rutaFoto = Paths.get("public/imagenes/" + nombreFoto);
                try (InputStream inputStream = foto.getInputStream()) {
                    Files.copy(inputStream, rutaFoto, StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    System.out.println("Error al guardar la imagen: " + e.getMessage());
                    redirectAttributes.addFlashAttribute("error", "Error al guardar la imagen");
                    return "redirect:/publicaciones/editar?id=" + id;
                }
            }

            publicacion.setNombreMascota(publicDto.getNombre_mascota());
            publicacion.setTelefono(publicDto.getTelefono());
            publicacion.setDescripcion(publicDto.getDescripcion());

            // Actualizar los nuevos campos
            publicacion.setEspecie(Publicacion.Especie.valueOf(publicDto.getEspecie()));
            publicacion.setEdad(Publicacion.Edad.valueOf(publicDto.getEdad()));
            publicacion.setTamanio(Publicacion.Tamanio.valueOf(publicDto.getTamanio()));
            publicacion.setDepartamento(publicDto.getDepartamento());
            publicacion.setEstado(Publicacion.Estado.valueOf(publicDto.getEstado()));

            repo.save(publicacion);
            redirectAttributes.addFlashAttribute("success", "Publicación actualizada exitosamente");

        } catch (Exception e) {
            System.out.println("Excepcion: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al actualizar la publicación");
        }
        return "redirect:/index";
    }

    @GetMapping("/borrar")
    public String borrarPublicacion(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        try {
            Publicacion publicacion = repo.findById(id).get();

            Path rutaFoto = Paths.get("public/imagenes/" + publicacion.getArchivoFoto());
            try {
                Files.delete(rutaFoto);
            } catch (Exception e) {
                System.out.println("Excepcion: " + e.getMessage());
            }

            repo.delete(publicacion);
            redirectAttributes.addFlashAttribute("success", "Publicación eliminada exitosamente");

        } catch (Exception e) {
            System.out.println("Excepcion: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al eliminar la publicación");
        }
        return "redirect:/index";
    }
}

//package com.example.demoPT.Controladores;
//
//import com.example.demoPT.Modelo.Publicacion;
//import com.example.demoPT.Modelo.PublicacionDto;
//import com.example.demoPT.Modelo.Usuario;
//import com.example.demoPT.Repositorios.RepositorioPublicaciones;
//import com.example.demoPT.Repositorios.RepositorioUsuario;
//import jakarta.validation.Valid;
//import java.io.InputStream;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.nio.file.StandardCopyOption;
//import java.security.Principal;
//import javax.swing.JOptionPane;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.validation.BindingResult;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.ModelAttribute;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.multipart.MultipartFile;
//
//@Controller
//@RequestMapping("/publicaciones")//como pedido principal, esta el de publicaciones
////por ejemplo /publicaciones/crear para crear una publicacion
//public class ControladorPublicacion {
//
//    @Autowired
//    private RepositorioPublicaciones repo;
//    @Autowired
//    private RepositorioUsuario repoU;
//
//    @GetMapping("/crear")//cuando seleccionamos el boton para crear publicaciones, se redirige a esta pantalla
//    public String mostrarPaginaCrear(Model modelo) {
//        // Verifica si el usuario está autenticado
//        if (SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
//            PublicacionDto publiDto = new PublicacionDto();
//            modelo.addAttribute("publiDto", publiDto);
//            return "publicaciones/CrearPublicacion";  // Si está autenticado, muestra la página para crear publicación
//        } else {
//            JOptionPane.showMessageDialog(null, "Usuario no autenticado");//mensajede error
//            return "redirect:/login";  // Si no está autenticado, redirige al login
//        }
//    }
//
//    @PostMapping("/crear")//una vez que vimos que esta autenticado pasamos a la creacion de la publicacion
//    public String crearPublicacion(@ModelAttribute("publiDto") PublicacionDto publiDto, @RequestParam("archivoFoto") MultipartFile archivoFoto, Principal principal) {
//        // Obtener el usuario autenticado
//        String username = principal.getName();
//
//        Usuario usuario = repoU.findByUsername(username)
//                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
//
//        // Crear la publicación seteando los atributos que nos dieron en el form
//        Publicacion publicacion = new Publicacion();
//        publicacion.setNombreMascota(publiDto.getNombre_mascota());
//        publicacion.setDireccion(publiDto.getDireccion());
//        publicacion.setTelefono(publiDto.getTelefono());
//        publicacion.setDescripcion(publiDto.getDescripcion());
//        publicacion.setUsuario(usuario); // Asignar usuario autenticado para hacer la relacion
//
//        // Guardar la foto 
//        if (publiDto.getArchivoFoto() != null && !publiDto.getArchivoFoto().isEmpty()) {
//            MultipartFile foto = publiDto.getArchivoFoto();
//            String nombreFoto = foto.getOriginalFilename();
//            publicacion.setArchivoFoto(nombreFoto);
//
//            // Guardar la imagen en la ruta
//            Path rutaFoto = Paths.get("public/imagenes/" + nombreFoto);
//            try (InputStream inputStream = foto.getInputStream()) {
//                Files.copy(inputStream, rutaFoto, StandardCopyOption.REPLACE_EXISTING);
//            } catch (Exception e) {
//                System.out.println("Error al guardar la imagen: " + e.getMessage());
//            }
//        }
////        if (!archivoFoto.isEmpty()) {
////            String nombreArchivo = archivoFoto.getOriginalFilename();
////            publicacion.setArchivoFoto(nombreArchivo);
////        }
//
//        // Guardar en la base de datos
//        repo.save(publicacion);
//        //redirigimoa a la pantalla de "mis publicaciones"
//        return "redirect:/index";
//    }
//
//    //funcion para editar la publicacion
//    @GetMapping("/editar")
//    public String mostrarPaginaEditar(Model modelo, @RequestParam int id) {// model permite enviar datos a la vista
//        try {
//            Publicacion publicacion = repo.findById(id).get();//Busca en la base de datos una Publicacion con el id que le mandamos y lo convierte en tipo publicacion
//            modelo.addAttribute("publicacion", publicacion);//Guarda la publicación en el modelo para que la vista (EditarPublicacion.html) pueda acceder a ella
//            //crea una publicacion nueva para no tener que usar la original por las dudas
//            PublicacionDto publiDto = new PublicacionDto();
//            publiDto.setNombre_mascota(publicacion.getNombreMascota());
//            publiDto.setTelefono(publicacion.getTelefono());
//            publiDto.setDireccion(publicacion.getDireccion());
//            publiDto.setDescripcion(publicacion.getDescripcion());
//
//            modelo.addAttribute("publiDto", publiDto);
//        } catch (Exception e) {
//            System.out.println("Excepcion: " + e.getMessage());
//            return "redirect:/index";
//        }
//        //Retorna la plantilla publicaciones/EditarPublicacion.html. Esta vista muestra el formulario de edición
//        return "publicaciones/EditarPublicacion";
//
//    }
//
//    @PostMapping("/editar")
//    public String actualizarPublicacion(Model modelo, @RequestParam int id, @Valid @ModelAttribute PublicacionDto publicDto, BindingResult resultado) {
//
//        try {
//            Publicacion publicacion = repo.findById(id).get();
//            modelo.addAttribute("publicacion", publicacion);
//
//            //validar si hay errores
//            if (resultado.hasErrors()) {
//                return "publicaciones/EditarPublicacion";
//            }
//            if (publicDto.getArchivoFoto() != null && !publicDto.getArchivoFoto().isEmpty()) {
//                MultipartFile foto = publicDto.getArchivoFoto();
//                String nombreFoto = foto.getOriginalFilename();
//                publicacion.setArchivoFoto(nombreFoto);
//
//                // Guardar la imagen en la ruta
//                Path rutaFoto = Paths.get("public/imagenes/" + nombreFoto);
//                try (InputStream inputStream = foto.getInputStream()) {
//                    Files.copy(inputStream, rutaFoto, StandardCopyOption.REPLACE_EXISTING);
//                } catch (Exception e) {
//                    System.out.println("Error al guardar la imagen: " + e.getMessage());
//                }
//            }
//
////            if (!publicDto.getArchivoFoto().isEmpty()) {
////                //se borra la imagen vieja
////                String cargarRuta = "public/imagenes/";
////                Path viejaRutaFoto = Paths.get(cargarRuta + publicacion.getArchivoFoto());
////                try {
////                    Files.delete(viejaRutaFoto);
////                } catch (Exception e) {
////                    System.out.println("Excepcion: " + e.getMessage());
////                }
////                //guardar la imagen nueva
////                MultipartFile foto = publicDto.getArchivoFoto();
////                String nombreFoto = foto.getOriginalFilename();
////                try (InputStream inputStream = foto.getInputStream()) {
////                    Files.copy(inputStream, Paths.get(cargarRuta + nombreFoto),
////                            StandardCopyOption.REPLACE_EXISTING);
////                } catch (Exception e) {
////                }
////                publicacion.setArchivoFoto(nombreFoto);
////            }
//            //actualizamos lo demas
//            publicacion.setNombreMascota(publicDto.getNombre_mascota());
//            publicacion.setTelefono(publicDto.getTelefono());
//            publicacion.setDireccion(publicDto.getDireccion());
//            publicacion.setDescripcion(publicDto.getDescripcion());
//            repo.save(publicacion);
//        } catch (Exception e) {
//            System.out.println("Excepcion: " + e.getMessage());
//        }
//        //redirigimos al apartado de "mis publicaciones"
//        return "redirect:/index";
//    }
//
//    //eliminar publicacion
//    @GetMapping("/borrar")
//    public String borrarPublicacion(@RequestParam int id) {
//        try {
//            Publicacion publicacion = repo.findById(id).get();
//
//            //borrar la foto
//            Path rutaFoto = Paths.get("public/imagenes/" + publicacion.getArchivoFoto());
//            try {
//                Files.delete(rutaFoto);
//            } catch (Exception e) {
//                System.out.println("Excepcion: " + e.getMessage());
//            }
//            //borramos la publicacion
//            repo.delete(publicacion);
//        } catch (Exception e) {
//            System.out.println("Excepcion: " + e.getMessage());
//        }
//        //redirigimos al apartado de "mis publicaciones"
//        return "redirect:/index";
//    }
//
//}
