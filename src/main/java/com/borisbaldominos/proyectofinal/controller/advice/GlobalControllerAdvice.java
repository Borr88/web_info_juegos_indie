package com.borisbaldominos.proyectofinal.controller.advice;

import com.borisbaldominos.proyectofinal.model.Usuario;
import com.borisbaldominos.proyectofinal.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.security.Principal;
import java.util.Optional;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // --- 1. CARGA DE DATOS GLOBALES (Avatar, Usuario) ---
    @ModelAttribute
    public void addGlobalUserData(Model model, Principal principal) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && principal != null) {
            String username = principal.getName();
            Optional<Usuario> usuarioOpt = usuarioRepository.findByNombre(username);
            if (usuarioOpt.isPresent()) {
                model.addAttribute("usuarioGlobal", usuarioOpt.get());
            }
        }
    }

    // --- 2. GESTIÓN DE ERRORES ---

    // Caso A: Error al subir un archivo demasiado grande
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(Model model) {
        model.addAttribute("error", "El archivo es demasiado grande. Máximo permitido: 10MB.");
        return "error/500";
    }

    // Caso B: Capturador Genérico (Cualquier otro error inesperado)
    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model) {
        // Loggear el error completo en consola/servidor para debugging
        System.err.println("❌ Error capturado por Advice: " + ex.getMessage());
        ex.printStackTrace();

        // NO exponer el mensaje completo al usuario - podria revelar informacion sensible
        // Solo mostramos un mensaje generico
        model.addAttribute("message", "Ha ocurrido un error inesperado. Por favor, inténtalo de nuevo más tarde.");
        return "error/500";
    }

    // NOTA: El 404 se gestiona automáticamente gracias al archivo templates/error/404.html
}