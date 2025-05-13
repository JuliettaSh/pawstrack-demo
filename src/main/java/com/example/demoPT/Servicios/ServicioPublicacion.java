/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demoPT.Servicios;

import com.example.demoPT.Modelo.Publicacion;
import com.example.demoPT.Modelo.Usuario;
import com.example.demoPT.Repositorios.RepositorioPublicaciones;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author gomez
 */
@Service
public class ServicioPublicacion {
    @Autowired
    private RepositorioPublicaciones publicacionRepository;

    public List<Publicacion> findAll() {
        return publicacionRepository.findAll();
    }

    public Optional<Publicacion> findById(Long id) {
        return publicacionRepository.findById(id);
    }

    public List<Publicacion> findByUsuario(Usuario usuario) {
        return publicacionRepository.findByUsuarioUsername(usuario.getUsername());
    }

    public Publicacion save(Publicacion publicacion) {
        return publicacionRepository.save(publicacion);
    }

    public void deleteById(Long id, Usuario usuario) throws AccessDeniedException {
        Publicacion publicacion = publicacionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada"));
        
        if (!publicacion.getUsuario().getId().equals(usuario.getId())) {
            throw new AccessDeniedException("No tienes permiso para eliminar esta publicación");
        }
        
        publicacionRepository.deleteById(id);
    }

    public List<Publicacion> findByFiltros(Publicacion.Especie especie, 
                                         Publicacion.Edad edad,
                                         Publicacion.Tamanio tamanio, 
                                         String departamento) {
        return publicacionRepository.findByFiltros(especie, edad, tamanio, departamento);
    }
}
