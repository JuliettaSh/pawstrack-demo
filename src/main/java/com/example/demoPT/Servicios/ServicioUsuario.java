package com.example.demoPT.Servicios;

import com.example.demoPT.Modelo.CambioContrasenaDTO;
import com.example.demoPT.Modelo.ImplUsuarioDetails;
import com.example.demoPT.Modelo.Usuario;
import com.example.demoPT.Repositorios.RepositorioPublicaciones;
import com.example.demoPT.Repositorios.RepositorioUsuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicioUsuario {

    private final RepositorioUsuario userRepository;
    private final RepositorioPublicaciones publiRepositorio;
    private final PasswordEncoder passwordEncoder;

    public ServicioUsuario(RepositorioUsuario userRepository, PasswordEncoder passwordEncoder, RepositorioPublicaciones publicacionRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.publiRepositorio = publicacionRepository;
    }

    //funcion para encontrar a un usario por el nombre
    public Optional<Usuario> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    //funcion para guardar al usario y ponerle un rol
    public void saveUser(Usuario user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        userRepository.save(user);
    }

    public Usuario getUsuarioActual() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElseThrow();
    }

    @Transactional
    public void actualizarUsuario(Usuario usuario) {
        Usuario usuarioActual = getUsuarioActual();
        usuarioActual.setUsername(usuario.getUsername());
        userRepository.save(usuarioActual);

        // 3. Guardar cambios
        Usuario usuarioActualizado = userRepository.save(usuarioActual);

        // 4. Actualizar contexto de seguridad
        UserDetails userDetails = new ImplUsuarioDetails(usuarioActualizado);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                userDetails.getPassword(),
                userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Transactional
    public boolean cambiarContrasena(CambioContrasenaDTO cambioContrasenaDTO) {
        Usuario usuario = getUsuarioActual();
        if (passwordEncoder.matches(cambioContrasenaDTO.getCurrentPassword(), usuario.getPassword())) {
            usuario.setPassword(passwordEncoder.encode(cambioContrasenaDTO.getNewPassword()));
            Usuario usuarioActualizado = userRepository.save(usuario);

            // 2. Actualizar contexto de seguridad
            UserDetails userDetails = new ImplUsuarioDetails(usuarioActualizado);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    userDetails.getPassword(),
                    userDetails.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return true;
        }
        return false;
    }

//    @Transactional
//    public void actualizarPrivacidad(boolean perfilPublico, boolean mostrarCorreo, boolean permitirMensajes) {
//        Usuario usuario = getUsuarioActual();
//        usuario.setPerfilPublico(perfilPublico);
//        usuario.setMostrarCorreo(mostrarCorreo);
//        usuario.setPermitirMensajes(permitirMensajes);
//        usuarioRepository.save(usuario);
//    }
    @Transactional
    public void eliminarCuenta(HttpServletRequest request, HttpServletResponse response) {
        Usuario usuario = getUsuarioActual();
        // Eliminar todas las publicaciones del usuario
        if (publiRepositorio != null) {
            publiRepositorio.deleteByUsuario(usuario);
        } else {
            throw new IllegalStateException("Repositorio de publicaciones no disponible");
        }
        userRepository.delete(usuario);
        userRepository.flush(); // Forzar la operación
        // Cerrar sesión programáticamente
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
    }

}
