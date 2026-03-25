package com.borisbaldominos.proyectofinal.repository;

import com.borisbaldominos.proyectofinal.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // CAMBIO IMPORTANTE: Usamos 'findByNombre' porque tu campo se llama 'nombre'
    Optional<Usuario> findByNombre(String nombre);

    // También buscamos por email para evitar duplicados
    Optional<Usuario> findByEmail(String email);
}