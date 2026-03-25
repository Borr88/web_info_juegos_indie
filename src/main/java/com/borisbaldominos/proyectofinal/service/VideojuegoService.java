package com.borisbaldominos.proyectofinal.service;

import com.borisbaldominos.proyectofinal.dto.VideojuegoDTO;
import com.borisbaldominos.proyectofinal.dto.VideojuegoDetalleDTO;
import com.borisbaldominos.proyectofinal.model.Usuario;
import com.borisbaldominos.proyectofinal.model.Videojuego;
import com.borisbaldominos.proyectofinal.repository.UsuarioRepository;
import com.borisbaldominos.proyectofinal.repository.VideojuegoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VideojuegoService {

    @Autowired
    private VideojuegoRepository repository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // ==========================================
    // 1. MÉTODOS DE CONVERSIÓN (DTOs)
    // ==========================================

    // Listar TODOS para el Index
    public List<VideojuegoDTO> listarTodosDTO() {
        return repository.findAll().stream()
                .map(this::convertirADTO) // Usa el método privado de abajo
                .collect(Collectors.toList());
    }

    // Buscar por nombre para el Index
    public List<VideojuegoDTO> buscarPorNombreDTO(String nombre) {
        return repository.findByTituloContainingIgnoreCase(nombre).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    // Detalle del Juego (DTO "Gordo")
    public VideojuegoDetalleDTO obtenerDetalleDTO(Long id) {
        Videojuego juego = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Juego no encontrado"));

        VideojuegoDetalleDTO dto = new VideojuegoDetalleDTO();
        dto.setId(juego.getId());
        dto.setTitulo(juego.getTitulo());
        dto.setPortadaUrl(juego.getPortadaUrl());
        dto.setValoracionMedia(juego.getValoracionMedia());
        dto.setSinopsis(juego.getSinopsis());

        // NOTA: No añadimos dto.setGenero() aquí para evitar errores si el DTO no tiene ese campo.

        return dto;
    }

    // ==========================================
    // 2. LÓGICA DE RECOMENDACIONES
    // ==========================================
    public List<Videojuego> obtenerRecomendaciones(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();
        List<Videojuego> favoritos = usuario.getFavoritos();

        // Si no tiene favoritos, randoms
        if (favoritos.isEmpty()) {
            return repository.findRandomGames();
        }

        // Agrupamos por género (La Entidad Videojuego SÍ tiene getGenero, así que esto funciona)
        Map<String, Long> conteoGeneros = favoritos.stream()
                .collect(Collectors.groupingBy(Videojuego::getGenero, Collectors.counting()));

        if (conteoGeneros.isEmpty()) return repository.findRandomGames();

        // Obtenemos el género más repetido
        String generoFavorito = Collections.max(conteoGeneros.entrySet(), Map.Entry.comparingByValue()).getKey();
        List<Long> idsFavoritos = favoritos.stream().map(Videojuego::getId).toList();

        // Buscamos juegos de ese género que no tenga ya
        List<Videojuego> recomendaciones = repository.findByGeneroAndIdNotIn(generoFavorito, idsFavoritos);

        if (recomendaciones.isEmpty()) {
            return repository.findRandomGames();
        }

        return recomendaciones;
    }

    // ==========================================
    // 3. MÉTODOS DE SOPORTE Y LEGACY
    // ==========================================

    public Optional<Videojuego> buscarPorId(Long id) {
        return repository.findById(id);
    }

    // Método privado para convertir a DTO ligero
    private VideojuegoDTO convertirADTO(Videojuego juego) {
        VideojuegoDTO dto = new VideojuegoDTO();
        dto.setId(juego.getId());
        dto.setTitulo(juego.getTitulo());
        dto.setPortadaUrl(juego.getPortadaUrl());
        dto.setValoracionMedia(juego.getValoracionMedia());
        // Control de nulos seguro
        dto.setSinopsis(juego.getSinopsis() != null ? juego.getSinopsis() : "");
        dto.setEsNovedad(juego.isEsNovedad());

        // ¡IMPORTANTE!: No hacemos dto.setGenero(...) aquí porque tu DTO no tiene el campo
        // y eso es lo que te daba error en rojo.

        return dto;
    }

    // Obtener lista de géneros únicos (String)
    public List<String> obtenerTodosGeneros() {
        return repository.findGenerosUnicos();
    }

    // Devuelve los juegos filtrados por género (DTO)
    public List<VideojuegoDTO> buscarPorGeneroDTO(String genero) {
        return repository.findByGenero(genero).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
}