package com.borisbaldominos.proyectofinal.dto;

public class VideojuegoDTO {

    private Long id;
    private String titulo;
    private String portadaUrl;
    private Double valoracionMedia;
    private String sinopsis;
    private boolean esNovedad;

    // Constructor vacío obligatorio
    public VideojuegoDTO() {}

    // --- GETTERS Y SETTERS MANUALES ---
    // Al escribirlos tú, Java no tiene excusa para no encontrarlos

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getPortadaUrl() { return portadaUrl; }
    public void setPortadaUrl(String portadaUrl) { this.portadaUrl = portadaUrl; }

    public Double getValoracionMedia() { return valoracionMedia; }
    public void setValoracionMedia(Double valoracionMedia) { this.valoracionMedia = valoracionMedia; }

    public String getSinopsis() { return sinopsis; }
    public void setSinopsis(String sinopsis) { this.sinopsis = sinopsis; }

    public boolean isEsNovedad() { return esNovedad; }
    public void setEsNovedad(boolean esNovedad) { this.esNovedad = esNovedad; }
}