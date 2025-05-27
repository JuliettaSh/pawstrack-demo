# PawsTrack - Plataforma de Adopción de Animales

Plataforma web para la publicación y adopción de mascotas, con gestión de estados (Disponible/En proceso/Adoptado) y sistema de filtrado avanzado.

## Índice
1. [Características Principales](#características-principales)
2. [Tecnologías Utilizadas](#tecnologías-utilizadas)
3. [Deployment](#deployment)
4. [Instalación y Configuración](#instalación-y-configuración)
   - [Requisitos](#requisitos)
   - [Configuración](#configuración)
   - [Base de Datos](#configurar-la-base-de-datos)
   - [Ejecución](#ejecutar-la-aplicación)

## Características Principales

- **Publicación de mascotas** con fotos y detalles completos
- **Gestión de estados**: Disponible, En proceso, Adoptado
- **Sistema de búsqueda avanzada** con filtros por:
  - Especie (Perro, Gato)
  - Edad (Cachorro, Joven, Adulto)
  - Tamaño (Pequeño, Mediano, Grande)
  - Departamento/ubicación
  - Estado de adopción
- **CRUD completo** de publicaciones
- **Autenticación de usuarios**
- **CRUD completo** de usuarios
- **Responsive design** - Compatible con dispositivos móviles

## Tecnologías Utilizadas

- **Backend**:
  - Java 17
  - Spring Boot
  - Spring Security
  - Thymeleaf (templating)
  - MySQL/PostgreSQL

- **Frontend**:
  - HTML5, CSS3
  - Bootstrap 5
  - JavaScript básico

- **Herramientas**:
  - Maven
  - Git
 
## Deployment
La aplicación está desplegada en [Render](https://render.com/):  
Enlace de acceso: [https://pawstrack-demo-i0qw.onrender.com](https://pawstrack-demo-i0qw.onrender.com)

## Instalación y Configuración

1. **Requisitos**:
   - Java JDK 17+
   - Maven 3.8+
   - MySQL 8.0+ o PostgreSQL 14+

2. **Configuración**:
   ```bash
   git clone https://github.com/JuliettaSh/pawstrack-demo.git
   cd /pawstrack-demo
3. **Configurar la base de datos**:
   - Crea una base de datos llamada pawstrackdemo
   - Configura las credenciales en application.properties
   ```bash
   spring.datasource.url=jdbc:mysql://localhost:3306/pawstrackdemo
   spring.datasource.username=root
   spring.datasource.password=
   spring.jpa.hibernate.ddl-auto=update
   server.port=8080

4. **Ejecutar la aplicación**:
   - Ejecutar DemoPtApplication.java → Run

5. **Acceder a la aplicación**:
   - Abrir en el navegador: http://localhost:8080
