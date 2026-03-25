package com.borisbaldominos.proyectofinal.service;

import com.borisbaldominos.proyectofinal.model.Rol;
import com.borisbaldominos.proyectofinal.model.Usuario;
import com.borisbaldominos.proyectofinal.model.Videojuego;
import com.borisbaldominos.proyectofinal.repository.UsuarioRepository;
import com.borisbaldominos.proyectofinal.repository.VideojuegoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Clase UsuarioService
 * --------------------
 * Gestiona la lógica de negocio relacionada con los usuarios:
 * - Registro de nuevas cuentas.
 * - Gestión de favoritos (Me gusta).
 * - Subida de avatares (imágenes de perfil).
 */
@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private VideojuegoRepository videojuegoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==========================================
    // 1. REGISTRO DE USUARIOS
    // ==========================================
    public void registrarUsuario(Usuario usuario) {
        // 1. Encriptamos la contraseña antes de guardarla (Vital para seguridad)
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        // 2. Asignamos el rol por defecto REGISTERED y activamos la cuenta
        usuario.setRol(Rol.REGISTERED); // Asegúrate de que en tu Enum Rol tienes 'USER'
        usuario.setActivo(true);

        // 3. Guardamos en BD
        usuarioRepository.save(usuario);
    }

    // ==========================================
    // 2. GESTIÓN DE FAVORITOS (TOGGLE)
    // ==========================================
    /**
     * Añade o quita un juego de la lista de favoritos del usuario.
     * Funciona como un interruptor: Si lo tiene -> lo borra. Si no -> lo añade.
     */
    public void alternarFavorito(Long usuarioId, Long videojuegoId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Videojuego juego = videojuegoRepository.findById(videojuegoId)
                .orElseThrow(() -> new RuntimeException("Juego no encontrado"));

        if (usuario.getFavoritos().contains(juego)) {
            // Si ya es favorito, lo quitamos
            usuario.getFavoritos().remove(juego);
        } else {
            // Si no lo es, lo añadimos
            usuario.getFavoritos().add(juego);
        }

        usuarioRepository.save(usuario);
    }

    /**
     * Comprueba si un usuario tiene un juego concreto en favoritos.
     * Útil para pintar el corazón relleno o vacío en el HTML.
     */
    public boolean esFavorito(Long usuarioId, Long videojuegoId) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();
        Videojuego juego = videojuegoRepository.findById(videojuegoId).orElseThrow();

        return usuario.getFavoritos().contains(juego);
    }

    // ==========================================
    // 3. GESTIÓN DE AVATAR (IMAGEN PERFIL)
    // ==========================================
    public void subirAvatar(Long usuarioId, MultipartFile archivo) {
        if (archivo.isEmpty()) return;

        try {
            // 1. Preparar ruta de destino
            String carpetaDestino = "uploads";
            Path rutaCarpeta = Paths.get(carpetaDestino);

            if (!Files.exists(rutaCarpeta)) {
                Files.createDirectories(rutaCarpeta);
            }

            // 2. Generar nombre único para evitar colisiones (Sobrescribir fotos de otros)
            // Ejemplo: usuario_5_avatar.jpg
            String nombreArchivo = "usuario_" + usuarioId + "_" + archivo.getOriginalFilename();

            // 3. Guardar archivo físico
            Path rutaCompleta = rutaCarpeta.resolve(nombreArchivo);
            Files.copy(archivo.getInputStream(), rutaCompleta, StandardCopyOption.REPLACE_EXISTING);

            // 4. Actualizar referencia en la BD
            Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();
            usuario.setImagenUrl(nombreArchivo);
            usuarioRepository.save(usuario);

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al subir la imagen de perfil");
        }
    }
}