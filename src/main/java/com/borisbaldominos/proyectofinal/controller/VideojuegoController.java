package com.borisbaldominos.proyectofinal.controller;

import com.borisbaldominos.proyectofinal.dto.VideojuegoDTO;
import com.borisbaldominos.proyectofinal.dto.VideojuegoDetalleDTO;
import com.borisbaldominos.proyectofinal.service.VideojuegoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Clase VideojuegoController (API REST)
 * -------------------------------------
 * Este controlador NO devuelve vistas HTML (Thymeleaf).
 * Devuelve datos en formato JSON para ser consumidos por clientes externos,
 * aplicaciones móviles o llamadas AJAX desde el frontend.
 */
@RestController
@RequestMapping("/api/videojuegos") // Prefijo para la API
public class VideojuegoController {

    @Autowired
    private VideojuegoService service;

    // ==========================================
    // 1. LISTADO GENERAL (JSON)
    // ==========================================
    /**
     * Devuelve la lista completa de videojuegos (versión resumida DTO).
     * URL: GET /api/videojuegos
     */
    @GetMapping
    public ResponseEntity<List<VideojuegoDTO>> listarTodos() {
        return ResponseEntity.ok(service.listarTodosDTO());
    }

    // ==========================================
    // 2. DETALLE DE UN JUEGO (JSON)
    // ==========================================
    /**
     * Devuelve el detalle completo de un videojuego específico por su ID.
     * URL: GET /api/videojuegos/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<VideojuegoDetalleDTO> obtenerDetalle(@PathVariable Long id) {
        // El servicio se encarga de buscar y convertir a DTO
        return ResponseEntity.ok(service.obtenerDetalleDTO(id));
    }
}