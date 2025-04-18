package com.example.demoPT.Repositorios;

import com.example.demoPT.Modelo.Publicacion;
import com.example.demoPT.Modelo.Usuario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface RepositorioPublicaciones extends JpaRepository<Publicacion, Integer>{
    //para encontrar todas las publicaciones (por nombre del usuario)
    List<Publicacion> findByUsuarioUsername(String username);
    void deleteByUsuario(Usuario usuario);
}
