# 🎮 IndieLovers - Plataforma de Videojuegos Indie

## 📋 Descripcion del Proyecto
**IndieLovers** es una aplicacion web desarrollada con **Spring Boot** siguiendo el patron de arquitectura **MVC**. Su objetivo es crear una comunidad donde los amantes de los videojuegos independientes puedan descubrir novedades, consultar detalles de juegos, gestionar sus favoritos y compartir resenas.

Este proyecto representa un **MVP (Producto Minimo Viable)** completamente funcional que cumple con los requisitos de gestion de usuarios, seguridad, validaciones y persistencia de datos.

---

## 🚀 Funcionalidades Principales

### 👤 Parte Publica y Usuarios (Rol REGISTERED)
- **Catalogo y Busqueda:** visualizacion de juegos destacados, novedades y populares. Buscador por titulo y filtrado por categorias.
- **Gestion de Perfil:** registro de usuarios, login y personalizacion de perfil con subida de **Avatar** (imagen).
- **Interaccion:**
    - Sistema de **Favoritos** (Me gusta) para guardar juegos.
    - Publicacion de **Resenas** y valoracion (nota) de los juegos.
- **Algoritmo de Recomendacion:** sugerencias automaticas de juegos basadas en los generos favoritos del usuario.
- **Notificaciones:** sistema de alertas interno en la web y envio de **Correos Electronicos** reales (SMTP).

### 🛡️ Parte Administrativa (Rol ADMIN)
- **CRUD Completo:** crear, leer, editar y eliminar videojuegos de la base de datos.
- **Gestion de Contenido:** marcar juegos como "Novedad / Proximo Lanzamiento".
- **Panel de Comunicaciones:** formulario para enviar notificaciones masivas a los usuarios (email + web).

---

## 🛠️ Tecnologias Utilizadas
- **Lenguaje:** Java 21
- **Framework Backend:** Spring Boot 3.5.x
- **Motor de Plantillas:** Thymeleaf (con Spring Security Extras)
- **Base de Datos:** H2 Database (base de datos embebida/fichero)
- **Seguridad:** Spring Security (BCrypt, roles, CSRF)
- **Email:** Spring Boot Starter Mail (JavaMailSender)
- **Frontend:** Bootstrap 5 + CSS personalizado (estilo neon/dark)
- **Herramientas:** Maven, Lombok

---

## ⚙️ Configuracion e Instalacion

### 1. Requisitos Previos
- Java JDK 17 o superior (recomendado JDK 21).
- IntelliJ IDEA (u otro IDE compatible).
- Maven.

### 2. Configuracion del Email (Opcional)
Para que el envio de correos funcione correctamente, debes configurar tus credenciales de Gmail (contrasena de aplicacion) en `src/main/resources/application.properties`:

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=TU_EMAIL@gmail.com
spring.mail.password=TU_CONTRASENA_DE_APLICACION
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### 3. Ejecucion
- Abre el proyecto en IntelliJ IDEA.
- Ejecuta la clase principal `ProyectoFinalApplication`.
- Abre tu navegador en: http://localhost:9001

### 🔑 Usuarios de Prueba
El sistema incluye un `DataSeeder` que carga datos iniciales y usuarios de prueba al arrancar la aplicacion.

- 👮‍♂️ Administrador (Acceso Total)
    - **Usuario:** Admin
    - **Contrasena:** admin123
    - **Permisos:** acceso al panel `/admin`, gestion de juegos y envio de notificaciones.

- 👤 Usuario Estandar (Pruebas)
    - **Usuario:** RoseM
    - **Contrasena:** 1234
    - **Permisos:** acceso a perfil, favoritos, resenas y recomendaciones.

### 🗄️ Acceso a Base de Datos
- La aplicacion utiliza una base de datos H2 en fichero.
- Consola H2: http://localhost:9001/h2-console
- JDBC URL: jdbc:h2:file:./data/indiedb
- User: sa
- Password: password

### ✒️ Autor
- Trabajo realizado por Boris Baldominos Gonzalez. Desarrollo Web con Spring Boot para 2º DAM PDAU. 09/02/2026