/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demoPT.Repositorios;

import com.example.demoPT.Modelo.Publicacion;
import com.example.demoPT.Modelo.Seguimiento;
import com.example.demoPT.Modelo.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author gomez
 */
public interface RepositorioSeguimiento extends JpaRepository<Seguimiento, Long> {

    List<Seguimiento> findByUsuario_Username(String username);

    //List<Seguimiento> findByAdoptante(Usuario usuario);
    List<Seguimiento> findByPublicacion(Publicacion publicacion);

    //List<Seguimiento> findByResponsable(Usuario responsable);
    @Query("SELECT s FROM Seguimiento s JOIN FETCH s.adoptante WHERE s.adoptante = :adoptante")
    List<Seguimiento> findByAdoptante(@Param("adoptante") Usuario adoptante);

    @Query("SELECT s FROM Seguimiento s JOIN FETCH s.adoptante WHERE s.responsable = :responsable")
    List<Seguimiento> findByResponsable(@Param("responsable") Usuario responsable);

    List<Seguimiento> findByAdoptanteOrResponsable(Usuario adoptante, Usuario responsable);

    Optional<Seguimiento> findByPublicacionAndAdoptante(Publicacion publicacion, Usuario adoptante);

    boolean existsByPublicacionAndAdoptante(Publicacion publicacion, Usuario adoptante);

    @EntityGraph(attributePaths = {"responsable", "adoptante", "publicacion"})
    @Override
    List<Seguimiento> findAll();

    @Transactional
    @Query("DELETE FROM Seguimiento s WHERE s.publicacion.id_publicacion = :publicacionId")
    void deleteByPublicacionId(@Param("publicacionId") Long publicacionId);
}
