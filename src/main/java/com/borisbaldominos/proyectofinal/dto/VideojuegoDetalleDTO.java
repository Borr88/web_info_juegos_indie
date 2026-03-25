package com.borisbaldominos.proyectofinal.dto;

import lombok.Data;

@Data
public class VideojuegoDetalleDTO {
    private Long id;
    private String titulo;
    private String portadaUrl;
    private String sinopsis;
    private Double valoracionMedia;

}
