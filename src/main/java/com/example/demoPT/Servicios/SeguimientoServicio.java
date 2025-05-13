package com.example.demoPT.Servicios;

import com.example.demoPT.Modelo.*;
import com.example.demoPT.Repositorios.*;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SeguimientoServicio {

    private static final Logger logger = LoggerFactory.getLogger(SeguimientoServicio.class);
    @Autowired
    private RepositorioSeguimiento seguimientoRepository;
    @Autowired
    private RepositorioUsuario repoUsu;

    @Autowired
    private ServicioPublicacion publicacionService;

    // SeguimientoServicio.java
    @Transactional
    public Seguimiento guardarSeguimiento(Seguimiento seguimiento) {
        if (seguimiento.getPublicacion() == null || seguimiento.getAdoptante() == null) {
            throw new IllegalArgumentException("Publicación y adoptante son requeridos");
        }
        return seguimientoRepository.save(seguimiento);
    }

    public List<Seguimiento> obtenerSeguimientosUsuario(Usuario usuario) {
        List<Seguimiento> comoAdoptante = seguimientoRepository.findByAdoptante(usuario);
        List<Seguimiento> comoResponsable = seguimientoRepository.findByResponsable(usuario);

        List<Seguimiento> todos = new ArrayList<>();
        todos.addAll(comoAdoptante);
        todos.addAll(comoResponsable);

        // Logging para depuración
        for (Seguimiento seguimiento : todos) {
            logger.info("Seguimiento ID: {}, Adoptante: {}", seguimiento.getId(), seguimiento.getAdoptante());
        }

        return todos;
    }

    public Seguimiento actualizarSeguimiento(Long id, String estado, String observaciones, Usuario usuario) throws AccessDeniedException {
        Seguimiento seguimiento = seguimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seguimiento no encontrado con id: " + id));

        if (!seguimiento.getResponsable().getId().equals(usuario.getId())) {
            throw new AccessDeniedException("No tienes permiso para editar este seguimiento");
        }

        seguimiento.setEstado(estado);
        seguimiento.setObservaciones(observaciones);

        return seguimientoRepository.save(seguimiento);
    }

    public Optional<Seguimiento> buscarPorIdYUsuario(Long id, Usuario usuario) {
        return seguimientoRepository.findById(id)
                .filter(s -> s.getAdoptante().getId().equals(usuario.getId())
                || s.getResponsable().getId().equals(usuario.getId()));
    }

    public List<Seguimiento> obtenerTodosSeguimientos() {
        return seguimientoRepository.findAll();
    }

    public boolean existeSeguimiento(Publicacion publicacion, Usuario adoptante) {
        return seguimientoRepository.existsByPublicacionAndAdoptante(publicacion, adoptante);
    }

    @Transactional
    public void eliminarSeguimientosPorPublicacion(Long publicacionId) {
        seguimientoRepository.deleteByPublicacionId(publicacionId);
    }
//    public Seguimiento iniciarSeguimiento(Publicacion publicacion, Usuario adoptante) {
//        // Verificar si ya existe un seguimiento
//        if (existeSeguimiento(publicacion, adoptante)) {
//            throw new IllegalStateException("Ya existe un seguimiento para esta publicación y usuario");
//        }
//
//        // Crear nuevo seguimiento
//        Seguimiento seguimiento = new Seguimiento();
//        seguimiento.setPublicacion(publicacion);
//        seguimiento.setAdoptante(adoptante);
//        seguimiento.setResponsable(publicacion.getUsuario()); // El dueño original de la publicación
//        seguimiento.setFecha(LocalDate.now());
//        seguimiento.setEstado("Pendiente"); // Estado inicial
//
//        return seguimientoRepository.save(seguimiento);
//    }
//
//    public Seguimiento guardarSeguimiento(Seguimiento seguimiento) {
//        if (seguimiento.getPublicacion() == null || seguimiento.getAdoptante() == null) {
//            throw new IllegalArgumentException("Publicación y adoptante son requeridos");
//        }
//
//        // Validar que la publicación existe
//        Publicacion publicacion = publicacionService.findById(seguimiento.getPublicacion().getId_publicacion())
//                .orElseThrow(() -> new IllegalArgumentException("Publicación no encontrada"));
//
//        // Validar que el adoptante existe
//        Usuario adoptante = repoUsu.findById(seguimiento.getAdoptante().getId())
//                .orElseThrow(() -> new IllegalArgumentException("Adoptante no encontrado"));
//
//        seguimiento.setPublicacion(publicacion);
//        seguimiento.setAdoptante(adoptante);
//
//        return seguimientoRepository.save(seguimiento);
//    }
//
//    public boolean existeSeguimiento(Publicacion publicacion, Usuario adoptante) {
//        return seguimientoRepository.existsByPublicacionAndAdoptante(publicacion, adoptante);
//    }
//
//    public List<Seguimiento> obtenerSeguimientosUsuario(Usuario usuario) {
//        if (usuario == null) {
//            throw new IllegalArgumentException("El usuario no puede ser null");
//        }
//        return seguimientoRepository.findByAdoptanteOrResponsable(usuario, usuario);
//    }
////    public List<Seguimiento> obtenerSeguimientosUsuario(Usuario usuario) {
////        // Seguimientos donde el usuario es adoptante
////        List<Seguimiento> comoAdoptante = seguimientoRepository.findByAdoptante(usuario);
////        // Seguimientos donde el usuario es responsable (publicador)
////        List<Seguimiento> comoResponsable = seguimientoRepository.findByResponsable(usuario);
////        
////        // Combinar ambas listas
////        List<Seguimiento> todos = new ArrayList<>();
////        todos.addAll(comoAdoptante);
////        todos.addAll(comoResponsable);
////        return todos;
////    }
//
//    public Seguimiento actualizarSeguimiento(Seguimiento seguimiento, Usuario usuario) throws AccessDeniedException {
//        // Solo el responsable puede actualizar
//        if (seguimiento.getResponsable().getId().equals(usuario.getId())) {
//            return seguimientoRepository.save(seguimiento);
//        }
//        throw new AccessDeniedException("No tienes permiso para editar este seguimiento");
//    }
//
//    public Optional<Seguimiento> buscarPorIdYUsuario(Long id, Usuario usuario) {
//        return seguimientoRepository.findById(id)
//                .filter(s -> s.getAdoptante().getId().equals(usuario.getId())
//                || s.getResponsable().getId().equals(usuario.getId()));
//    }
//
//    public List<Seguimiento> obtenerTodosSeguimientos() {
//        return seguimientoRepository.findAll();
//    }

}
