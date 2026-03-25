package com.borisbaldominos.proyectofinal.service;

import com.borisbaldominos.proyectofinal.dto.ReviewDTO;
import com.borisbaldominos.proyectofinal.model.Review;
import com.borisbaldominos.proyectofinal.model.Usuario;
import com.borisbaldominos.proyectofinal.model.Videojuego;
import com.borisbaldominos.proyectofinal.repository.ReviewRepository;
import com.borisbaldominos.proyectofinal.repository.UsuarioRepository;
import com.borisbaldominos.proyectofinal.repository.VideojuegoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Clase ReviewService
 * -------------------
 * Gestiona las opiniones y valoraciones de los usuarios.
 * Se encarga de guardar reseñas y recalcular la nota media de los juegos.
 */
@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private VideojuegoRepository videojuegoRepository;

    // ==========================================
    // 1. GUARDAR RESEÑA
    // ==========================================
    /**
     * Guarda un comentario y una puntuación de un usuario para un juego.
     * Además, actualiza la nota media del videojuego.
     */
    public void guardarReview(ReviewDTO dto, String nombreUsuario) {

        // 1. Recuperamos Usuario y Juego
        Usuario usuario = usuarioRepository.findByNombre(nombreUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Videojuego videojuego = videojuegoRepository.findById(dto.getVideojuegoId())
                .orElseThrow(() -> new RuntimeException("Juego no encontrado"));

        // 2. Creamos y guardamos la Review
        Review review = new Review();
        review.setUsuario(usuario);
        review.setVideojuego(videojuego);
        review.setComentario(dto.getComentario());
        review.setPuntuacion(dto.getPuntuacion());

        // Guardamos la reseña en BD
        reviewRepository.save(review);

        // 3. EXTRA: Recalcular la media del juego automáticamente
        actualizarNotaMediaJuego(videojuego);
    }

    // ==========================================
    // 2. LISTAR RESEÑAS
    // ==========================================
    public List<Review> obtenerReviewsPorJuego(Long videojuegoId) {
        return reviewRepository.findByVideojuegoIdOrderByFechaDesc(videojuegoId);
    }

    // ==========================================
    // 3. MÉTODOS PRIVADOS (CÁLCULOS)
    // ==========================================

    /**
     * Recalcula el promedio de todas las valoraciones de un juego y lo guarda.
     */
    private void actualizarNotaMediaJuego(Videojuego juego) {
        List<Review> reviews = reviewRepository.findByVideojuegoIdOrderByFechaDesc(juego.getId());

        if (!reviews.isEmpty()) {
            double suma = reviews.stream().mapToDouble(Review::getPuntuacion).sum();
            double media = suma / reviews.size();

            // Redondeamos a 1 decimal (Ej: 8.7)
            double mediaRedondeada = Math.round(media * 10.0) / 10.0;

            juego.setValoracionMedia(mediaRedondeada);
            videojuegoRepository.save(juego);
        }
    }
}