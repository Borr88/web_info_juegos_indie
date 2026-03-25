package com.borisbaldominos.proyectofinal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "El usuario tiene que tener nombre")
    @Size(min=5,max=30)
    @Column(unique = true)
    private String nombre;

    @NotBlank
    private String password;

    @Email(message = "Debe ser email valido")
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
