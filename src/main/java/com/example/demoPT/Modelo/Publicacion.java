package com.example.demoPT.Modelo;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity //clase publicacion con sus atributos y constructores
@Table(name = "publicaciones")
public class Publicacion implements Serializable{
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id_publicacion;
    
    @Basic
    private String nombreMascota;
    //private String direccion;
    private String telefono;
    
    @Column(columnDefinition = "TEXT")
    private String descripcion;
    private String archivoFoto;
    
    @ManyToOne(fetch = FetchType.LAZY)//relacion con el usuario
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    
    @Enumerated(EnumType.STRING)
    private Especie especie;
    
    @Enumerated(EnumType.STRING)
    private Edad edad;
    
    @Enumerated(EnumType.STRING)
    private Tamanio tamanio;
    
    private String departamento; // Para los departamentos de Mendoza
    
    // Enums para los tipos
    public enum Especie { PERRO, GATO, OTRO }
    public enum Edad { CACHORRO, JOVEN, ADULTO }
    public enum Tamanio { PEQUENIO, MEDIANO, GRANDE }
    
    
    public Publicacion() {
    }

    public Long getId_publicacion() {
        return id_publicacion;
    }

    public void setId_publicacion(Long id_publicacion) {
        this.id_publicacion = id_publicacion;
    }



    public String getNombreMascota() {
        return nombreMascota;
    }

    public void setNombreMascota(String nombreMascota) {
        this.nombreMascota = nombreMascota;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getArchivoFoto() {
        return archivoFoto;
    }

    public void setArchivoFoto(String archivoFoto) {
        this.archivoFoto = archivoFoto;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Especie getEspecie() {
        return especie;
    }

    public void setEspecie(Especie especie) {
        this.especie = especie;
    }

    public Edad getEdad() {
        return edad;
    }

    public void setEdad(Edad edad) {
        this.edad = edad;
    }

    public Tamanio getTamanio() {
        return tamanio;
    }

    public void setTamanio(Tamanio tamanio) {
        this.tamanio = tamanio;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }
    
    
}
