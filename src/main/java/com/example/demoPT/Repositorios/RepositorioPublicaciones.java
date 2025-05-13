package com.example.demoPT.Repositorios;

import com.example.demoPT.Modelo.Publicacion;
import com.example.demoPT.Modelo.Publicacion.Edad;
import com.example.demoPT.Modelo.Publicacion.Especie;
import com.example.demoPT.Modelo.Publicacion.Tamanio;
import com.example.demoPT.Modelo.Usuario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RepositorioPublicaciones extends JpaRepository<Publicacion, Long> {

    //para encontrar todas las publicaciones (por nombre del usuario)
    List<Publicacion> findByUsuarioUsername(String username);
    List<Publicacion> findByUsuario(Usuario usuario);

    void deleteByUsuario(Usuario usuario);

    List<Publicacion> findByEspecie(Publicacion.Especie especie);

    List<Publicacion> findByDepartamento(String departamento);

    List<Publicacion> findByEspecie(Publicacion.Edad edad);

    List<Publicacion> findByEspecie(Publicacion.Tamanio tamanio);

    //aca se crean las consultas para filtrar en el home
    @Query("SELECT p FROM Publicacion p WHERE "
            + "(:especie IS NULL OR p.especie = :especie) AND "
            + "(:edad IS NULL OR p.edad = :edad) AND "
            + "(:tamanio IS NULL OR p.tamanio = :tamanio) AND "
            + "(:departamento IS NULL OR p.departamento LIKE %:departamento%)")
    List<Publicacion> findByFiltros(
            @Param("especie") Especie especie,
            @Param("edad") Edad edad,
            @Param("tamanio") Tamanio tamanio,
            @Param("departamento") String departamento);

}
