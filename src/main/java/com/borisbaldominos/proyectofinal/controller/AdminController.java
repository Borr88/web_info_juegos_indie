package com.borisbaldominos.proyectofinal.controller;

import com.borisbaldominos.proyectofinal.model.Videojuego;
import com.borisbaldominos.proyectofinal.repository.UsuarioRepository;
import com.borisbaldominos.proyectofinal.repository.VideojuegoRepository;
import com.borisbaldominos.proyectofinal.service.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin") // Prefijo global para todas las rutas
public class AdminController {

    @Autowired
    private VideojuegoRepository videojuegoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private NotificacionService notificacionService;

    // ==========================================
    // 1. DASHBOARD Y GESTIÓN DE JUEGOS
    // ==========================================

    // VISTA PRINCIPAL (LISTADO)
    @GetMapping
    public String panelControl(Model model) {
        model.addAttribute("videojuegos", videojuegoRepository.findAll());
        return "admin"; // Vista admin.html
    }

    // FORMULARIO: CREAR NUEVO
    @GetMapping("/nuevo")
    public String nuevoJuego(Model model) {
        model.addAttribute("videojuego", new Videojuego());
        model.addAttribute("titulo", "Crear Nuevo Juego");
        return "admin_form"; // Vista admin_form.html
    }

    // FORMULARIO: EDITAR EXISTENTE
    @GetMapping("/editar/{id}")
    public String editarJuego(@PathVariable Long id, Model model) {
        Videojuego juego = videojuegoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Juego no encontrado"));

        model.addAttribute("videojuego", juego);
        model.addAttribute("titulo", "Editar Juego");
        return "admin_form";
    }

    // ACCIÓN: GUARDAR (CREAR O ACTUALIZAR)
    @PostMapping("/guardar")
    public String guardarJuego(@ModelAttribute Videojuego formulario,
                               @RequestParam("archivo") MultipartFile archivo) {

        Videojuego juegoAGuardar;

        // CASO A: ACTUALIZAR (El ID viene en el formulario)
        if (formulario.getId() != null) {
            Videojuego juegoExistente = videojuegoRepository.findById(formulario.getId()).orElseThrow();

            // Actualizamos campos manuales
            juegoExistente.setTitulo(formulario.getTitulo());
            juegoExistente.setSinopsis(formulario.getSinopsis());
            juegoExistente.setGenero(formulario.getGenero());
            juegoExistente.setValoracionMedia(formulario.getValoracionMedia());
            juegoExistente.setEsNovedad(formulario.isEsNovedad());

            juegoAGuardar = juegoExistente;
        }
        // CASO B: CREAR NUEVO
        else {
            juegoAGuardar = formulario;
        }

        // GESTIÓN DE IMAGEN (Si subieron una nueva)
        if (!archivo.isEmpty()) {
            String nombreImagen = guardarImagen(archivo); // Llamada al metodo helper privado
            if (nombreImagen != null) {
                juegoAGuardar.setPortadaUrl(nombreImagen);
            }
        }

        videojuegoRepository.save(juegoAGuardar);
        return "redirect:/admin";
    }

    // ACCIÓN: BORRAR (POST para evitar CSRF y accidentes)
    @PostMapping("/borrar/{id}")
    public String borrarJuego(@PathVariable Long id) {
        videojuegoRepository.deleteById(id);
        return "redirect:/admin";
    }

    // ==========================================
    // 2. GESTIÓN DE NOTIFICACIONES (EMAIL)
    // ==========================================

    // FORMULARIO DE ENVÍO
    @GetMapping("/notificar")
    public String formularioNotificacion(Model model) {
        model.addAttribute("usuarios", usuarioRepository.findAll());
        return "admin_notificacion"; // Vista admin_notificacion.html
    }

    // ACCIÓN: ENVIAR EMAIL
    @PostMapping("/enviar-notificacion")
    public String enviarNotificacion(@RequestParam Long usuarioId,
                                     @RequestParam String asunto,
                                     @RequestParam String mensaje) {

        notificacionService.enviarNotificacion(usuarioId, asunto, mensaje);
        return "redirect:/admin?mensajeEnviado";
    }

    // ==========================================
    // 3. METODOS PRIVADOS (HELPERS)
    // ==========================================

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    private static final List<String> ALLOWED_EXTENSIONS = List.of(
            ".jpg", ".jpeg", ".png", ".gif", ".webp"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB max

    // Magic bytes para validar el contenido real de imágenes
    private static final byte[] JPEG_MAGIC = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_MAGIC = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] GIF_MAGIC = {0x47, 0x49, 0x46}; // GIF
    private static final byte[] WEBP_MAGIC = {0x52, 0x49, 0x46, 0x46}; // RIFF header para WebP

    /**
     * Valida que el archivo sea una imagen válida (tipo, extensión y magic bytes)
     */
    private void validarImagen(MultipartFile archivo) {
        if (archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        if (archivo.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("El archivo excede el tamaño máximo permitido (5MB)");
        }

        String contentType = archivo.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Solo se permiten imágenes JPG, PNG, GIF o WebP");
        }

        String nombreOriginal = archivo.getOriginalFilename();
        if (nombreOriginal == null) {
            throw new IllegalArgumentException("Nombre de archivo inválido");
        }

        String extension = "";
        int lastDot = nombreOriginal.lastIndexOf('.');
        if (lastDot > 0) {
            extension = nombreOriginal.substring(lastDot).toLowerCase();
        }

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Extensión de archivo no permitida: " + extension);
        }

        // Validar magic bytes (contenido real del archivo)
        validarMagicBytes(archivo, extension);
    }

    /**
     * Valida que los primeros bytes del archivo coincidan con el formato esperado
     */
    private void validarMagicBytes(MultipartFile archivo, String extension) {
        try {
            byte[] header = archivo.getBytes();
            if (header.length < 4) {
                throw new IllegalArgumentException("Archivo demasiado pequeño para ser una imagen válida");
            }

            boolean esValido = switch (extension) {
                case ".jpg", ".jpeg" -> {
                    yield header.length >= 3 &&
                            header[0] == JPEG_MAGIC[0] &&
                            header[1] == JPEG_MAGIC[1] &&
                            header[2] == JPEG_MAGIC[2];
                }
                case ".png" -> {
                    if (header.length < 8) yield false;
                    for (int i = 0; i < PNG_MAGIC.length; i++) {
                        if (header[i] != PNG_MAGIC[i]) yield false;
                    }
                    yield true;
                }
                case ".gif" -> {
                    yield header.length >= 3 &&
                            header[0] == GIF_MAGIC[0] &&
                            header[1] == GIF_MAGIC[1] &&
                            header[2] == GIF_MAGIC[2];
                }
                case ".webp" -> {
                    if (header.length < 12) yield false;
                    if (header[0] != WEBP_MAGIC[0] || header[1] != WEBP_MAGIC[1] ||
                        header[2] != WEBP_MAGIC[2] || header[3] != WEBP_MAGIC[3]) yield false;
                    yield header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P';
                }
                default -> false;
            };

            if (!esValido) {
                throw new IllegalArgumentException("El contenido del archivo no coincide con su extensión. Se requiere una imagen válida.");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Error al validar el archivo: " + e.getMessage());
        }
    }

    /**
     * Sanitiza el nombre del archivo para prevenir Path Traversal
     */
    private String sanitizarNombreArchivo(String nombreOriginal) {
        String nombreSinPath = Paths.get(nombreOriginal).getFileName().toString();
        String nombreLimpio = nombreSinPath.replaceAll("[^a-zA-Z0-9._-]", "_");
        return nombreLimpio;
    }

    /**
     * Sube la imagen al servidor y devuelve el nombre del archivo generado.
     */
    private String guardarImagen(MultipartFile archivo) {
        try {
            // Validar el archivo antes de procesarlo
            validarImagen(archivo);

            String carpetaDestino = "uploads";
            Path rutaCarpeta = Paths.get(carpetaDestino);

            if (!Files.exists(rutaCarpeta)) {
                Files.createDirectories(rutaCarpeta);
            }

            // Sanitizar nombre y generar nombre único
            String nombreOriginalSanitizado = sanitizarNombreArchivo(archivo.getOriginalFilename());
            String nombreArchivo = UUID.randomUUID().toString() + "_" + nombreOriginalSanitizado;
            Path rutaCompleta = rutaCarpeta.resolve(nombreArchivo);

            Files.copy(archivo.getInputStream(), rutaCompleta, StandardCopyOption.REPLACE_EXISTING);

            return nombreArchivo;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}