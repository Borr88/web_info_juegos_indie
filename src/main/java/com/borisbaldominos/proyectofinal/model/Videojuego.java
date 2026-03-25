package com.borisbaldominos.proyectofinal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Videojuego {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El título es obligatorio")
    @Size(min = 2, max = 100)
    private String titulo;

    @NotBlank
    @jakarta.persistence.Column(length = 1000) // <--- ESTO ES LA CLAVE
    private String sinopsis;

    private String portadaUrl;

    @Min(0) @Max(10)
    private Double valoracionMedia;

    @OneToMany(mappedBy = "videojuego", cascade = CascadeType.ALL)
    private List<Review> reviews;

    private String genero;

    private boolean esNovedad;

}
