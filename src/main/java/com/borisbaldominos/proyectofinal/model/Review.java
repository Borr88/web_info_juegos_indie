package com.borisbaldominos.proyectofinal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Min(0) @Max(10)
    private int puntuacion;

    @NotBlank (message = "El comentario no puede estar vacio")
    @Size(min=10, max = 500)
    private String comentario;

    @ManyToOne
    @JoinColumn(name = "videojuego_id")
    private Videojuego videojuego;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private LocalDateTime fecha;

    @PrePersist
    public void asignarFecha() {
        this.fecha = LocalDateTime.now();
    }

}
