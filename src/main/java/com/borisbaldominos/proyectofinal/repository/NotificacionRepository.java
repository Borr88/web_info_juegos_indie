package com.borisbaldominos.proyectofinal.repository;

import com.borisbaldominos.proyectofinal.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    // Buscar notificaciones de un usuario ordenadas por las más nuevas primero
    List<Notificacion> findByUsuarioIdOrderByFechaDesc(Long usuarioId);
}