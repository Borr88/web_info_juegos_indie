package com.borisbaldominos.proyectofinal.service;

import com.borisbaldominos.proyectofinal.model.Notificacion;
import com.borisbaldominos.proyectofinal.model.Usuario;
import com.borisbaldominos.proyectofinal.repository.NotificacionRepository;
import com.borisbaldominos.proyectofinal.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Clase NotificacionService
 * -------------------------
 * Gestiona el sistema de avisos de la plataforma.
 * Realiza una doble función:
 * 1. Guarda el mensaje en BBDD para que el usuario lo vea en la web.
 * 2. Envía un correo electrónico real usando el servidor SMTP de Gmail.
 */
@Service
public class NotificacionService {

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JavaMailSender mailSender; // Dependencia inyectada para enviar emails

    // ==========================================
    // 1. ENVÍO DE NOTIFICACIONES (CORE)
    // ==========================================

    /**
     * Crea una notificación interna y envía una copia por email.
     * @param usuarioDestinoId ID del usuario que recibe el mensaje.
     * @param asunto Título del mensaje.
     * @param mensaje Cuerpo del texto.
     */
    public void enviarNotificacion(Long usuarioDestinoId, String asunto, String mensaje) {

        // 1. Recuperar Usuario destinatario
        Usuario usuario = usuarioRepository.findById(usuarioDestinoId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. Guardar en Base de Datos (Para la campana de notificaciones web)
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(usuario);
        notificacion.setAsunto(asunto);
        notificacion.setMensaje(mensaje);
        // La fecha se asigna automáticamente en la Entidad con @PrePersist
        notificacionRepository.save(notificacion);

        // 3. Enviar Correo Electrónico (Para aviso externo)
        // Usamos un try-catch para que, si falla el servidor de correo (Gmail),
        // la notificación web se guarde de todas formas y no rompa la ejecución.
        try {
            SimpleMailMessage email = new SimpleMailMessage(); // Objeto estándar de Spring Mail
            email.setTo(usuario.getEmail());
            email.setSubject("IndieLovers: " + asunto);
            email.setText("Hola " + usuario.getNombre() + ",\n\n" +
                    mensaje + "\n\nAtentamente,\nEl equipo de IndieLovers.");

            mailSender.send(email); // Envío real
            System.out.println("📧 Email enviado a: " + usuario.getEmail());

        } catch (Exception e) {
            System.err.println("❌ Error enviando email: " + e.getMessage());
            // No lanzamos excepción para permitir que el proceso continúe
        }
    }

    // ==========================================
    // 2. LECTURA DE NOTIFICACIONES
    // ==========================================

    /**
     * Obtiene el historial de notificaciones de un usuario, ordenadas por fecha.
     */
    public List<Notificacion> obtenerMisNotificaciones(Long usuarioId) {
        return notificacionRepository.findByUsuarioIdOrderByFechaDesc(usuarioId);
    }
}