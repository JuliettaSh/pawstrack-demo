package com.example.demoPT.Servicios;

import com.example.demoPT.Modelo.Usuario;
import com.example.demoPT.Repositorios.RepositorioUsuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class ServicioUsuario {
    private final RepositorioUsuario userRepository;
    private final PasswordEncoder passwordEncoder;

    public ServicioUsuario(RepositorioUsuario userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
}
