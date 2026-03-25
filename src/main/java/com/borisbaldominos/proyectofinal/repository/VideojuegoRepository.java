package com.borisbaldominos.proyectofinal.repository;

import com.borisbaldominos.proyectofinal.model.Videojuego;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VideojuegoRepository extends JpaRepository<Videojuego, Long> {

    // Para el buscador (Ya lo tenías)
    List<Videojuego> findByTituloContainingIgnoreCase(String titulo);

    // PLAN A: Recomendación inteligente por género, excluyendo los que ya tiene
    List<Videojuego> findByGeneroAndIdNotIn(String genero, java.util.Collection<Long> idsExcluidos);

    // PLAN B: Juegos aleatorios (Si falla el plan A o no hay favoritos)
    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM videojuego ORDER BY RAND() LIMIT 3", nativeQuery = true)
    List<Videojuego> findRandomGames();

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT v.genero FROM Videojuego v")
    List<String> findGenerosUnicos();

    // 2. Buscar por género exacto
    List<Videojuego> findByGenero(String genero);
}