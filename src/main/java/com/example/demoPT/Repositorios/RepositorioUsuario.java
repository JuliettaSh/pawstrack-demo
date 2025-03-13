package com.example.demoPT.Repositorios;

import com.example.demoPT.Modelo.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface RepositorioUsuario extends JpaRepository<Usuario, Long> {
    //para encontrar a todos los usuarios (por nombre)
    Optional<Usuario> findByUsername(String username);
    
}
