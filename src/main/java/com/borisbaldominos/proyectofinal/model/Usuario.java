package com.borisbaldominos.proyectofinal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * Patron de contraseña segura:
 * - Al menos 8 caracteres
 * - Al menos 1 letra mayúscula
 * - Al menos 1 número
 * - Al menos 1 carácter especial (!@#$%^&*(),.?":{}|<>)
 */
@Entity
@Data
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "El usuario tiene que tener nombre")
    @Size(min=5, max=30, message = "El nombre debe tener entre 5 y 30 caracteres")
    @Column(unique = true)
    private String nombre;

    @NotBlank(message = "La contraseña es obligatoria")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$",
        message = "La contraseña debe tener al menos 8 caracteres, 1 mayúscula, 1 número y 1 carácter especial"
    )
    private String password;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ser un email válido")
    private String email;

    @Enumerated(EnumType.STRING)
    private Rol rol;

    private boolean activo;

    @OneToMany(mappedBy = "usuario")
    private List<Review> reviews;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "usuario_favoritos", // Nombre de la tabla intermedia que se creará sola
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "videojuego_id")
    )
    private java.util.List<Videojuego> favoritos = new java.util.ArrayList<>();

    // Metodo para añadir uno solo
    public void addFavorito(Videojuego juego) {
        this.favoritos.add(juego);
    }

    // Metodo para quitar uno solo
    public void removeFavorito(Videojuego juego) {
        this.favoritos.remove(juego);
    }

    private String imagenUrl;

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

}
