package com.borisbaldominos.proyectofinal.dto;

import lombok.Data;

@Data
public class ReviewDTO {
    // Necesitamos saber a qué juego corresponde la reseña
    private Long videojuegoId;

    // La nota (del 0 al 10)
    private int puntuacion;

    // El texto de la opinión
    private String comentario;
}