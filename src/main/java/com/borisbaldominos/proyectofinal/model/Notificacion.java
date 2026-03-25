package com.borisbaldominos.proyectofinal.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Notificacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String asunto;

    @Column(length = 1000)
    private String mensaje;

    private LocalDateTime fecha;

    private boolean leida = false; // Para marcar si la ha visto (opcional)

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario; // El destinatario

    @PrePersist
    public void asignarFecha() {
        this.fecha = LocalDateTime.now();
    }
}