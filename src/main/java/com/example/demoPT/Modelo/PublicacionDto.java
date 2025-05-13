package com.example.demoPT.Modelo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public class PublicacionDto {//clase DTO es para transportar datos entre diferentes capas de una aplicación
    //Evitamos exponer directamente las entidades del modelo asi
    
    //si no se llenan los campos sale el mensaje
    @NotEmpty(message = "Se requiere el nombre")
    private String nombre_mascota;
    
    @NotEmpty(message = "Se requiere el telefono")
    private String telefono;
    
    @NotBlank(message = "La especie es requerida")
    private String especie;
    
    @NotBlank(message = "La edad es requerida")
    private String edad;
    
    @NotBlank(message = "El tamaño es requerido")
    private String tamanio;
    
    @NotBlank(message = "El departamento es requerido")
    private String departamento;
    @Size(min = 10, message = "La descripción deberia tener al menos 10 caracteres")
    @Size(max = 2000, message = "La descripción no puede exceder los 2000 caracteres")
    private String descripcion;
    
    private MultipartFile archivoFoto;

    public PublicacionDto() {
    }

    public String getNombre_mascota() {
        return nombre_mascota;
    }

    public void setNombre_mascota(String nombre_mascota) {
        this.nombre_mascota = nombre_mascota;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public MultipartFile getArchivoFoto() {
        return archivoFoto;
    }

    public void setArchivoFoto(MultipartFile archivoFoto) {
        this.archivoFoto = archivoFoto;
    }

    public String getEspecie() {
        return especie;
    }

    public void setEspecie(String especie) {
        this.especie = especie;
    }

    public String getEdad() {
        return edad;
    }

    public void setEdad(String edad) {
        this.edad = edad;
    }

    public String getTamanio() {
        return tamanio;
    }

    public void setTamanio(String tamanio) {
        this.tamanio = tamanio;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    
    
}
