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

    // ACCIÓN: BORRAR
    @GetMapping("/borrar/{id}")
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

    /**
     * Sube la imagen al servidor y devuelve el nombre del archivo generado.
     */
    private String guardarImagen(MultipartFile archivo) {
        try {
            String carpetaDestino = "uploads";
            Path rutaCarpeta = Paths.get(carpetaDestino);

            if (!Files.exists(rutaCarpeta)) {
                Files.createDirectories(rutaCarpeta);
            }

            String nombreArchivo = UUID.randomUUID().toString() + "_" + archivo.getOriginalFilename();
            Path rutaCompleta = rutaCarpeta.resolve(nombreArchivo);

            Files.copy(archivo.getInputStream(), rutaCompleta, StandardCopyOption.REPLACE_EXISTING);

            return nombreArchivo;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}