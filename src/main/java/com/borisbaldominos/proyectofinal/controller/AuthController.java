package com.borisbaldominos.proyectofinal.controller;

import com.borisbaldominos.proyectofinal.model.Usuario;
import com.borisbaldominos.proyectofinal.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Clase AuthController
 * --------------------
 * Gestiona las vistas y acciones relacionadas con la autenticación:
 * - Mostrar formulario de Login.
 * - Mostrar formulario de Registro.
 * - Procesar la creación de un nuevo usuario.
 */
@Controller
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    // ==========================================
    // 1. GESTIÓN DE LOGIN
    // ==========================================

    /**
     * Muestra la pantalla de inicio de sesión.
     * Nota: La lógica del POST (verificar credenciales) la hace Spring Security automáticamente.
     */
    @GetMapping("/login")
    public String login() {
        return "login"; // Renderiza login.html
    }

    // ==========================================
    // 2. GESTIÓN DE REGISTRO
    // ==========================================

    /**
     * Muestra el formulario para crear una cuenta nueva.
     */
    @GetMapping("/registro")
    public String registro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro"; // Renderiza registro.html
    }

    /**
     * Recibe los datos del formulario de registro y crea el usuario en BD.
     */
    @PostMapping("/guardar-usuario")
    public String guardarUsuario(@ModelAttribute Usuario usuario) {
        // El servicio se encarga de encriptar la contraseña y asignar el rol
        usuarioService.registrarUsuario(usuario);

        // Redirigimos al login con un parámetro ?registrado para mostrar una alerta de éxito
        return "redirect:/login?registrado";
    }
}