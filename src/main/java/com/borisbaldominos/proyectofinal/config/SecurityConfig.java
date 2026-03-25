package com.borisbaldominos.proyectofinal.config;

import com.borisbaldominos.proyectofinal.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Clase SecurityConfig
 * --------------------
 * Define las reglas de seguridad de la aplicación:
 * 1. Qué rutas son públicas y cuáles privadas.
 * 2. Configuración del formulario de Login/Logout.
 * 3. Encriptación de contraseñas.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    // ==========================================
    // 1. FILTRO DE SEGURIDAD (LAS REGLAS)
    // ==========================================
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // --- A. CONFIGURACIÓN TÉCNICA (Necesario para H2 Console) ---
                .csrf(csrf -> csrf.disable()) // Desactivamos CSRF para facilitar pruebas y consola H2
                .headers(headers -> headers.frameOptions(frame -> frame.disable())) // Permite ver H2 en frames

                // --- B. REGLAS DE AUTORIZACIÓN (El Portero) ---
                .authorizeHttpRequests(auth -> auth

                        // 1. RECURSOS ESTÁTICOS (Siempre permitidos para que cargue el CSS/JS/Fotos)
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**").permitAll()

                        // 2. VISTAS PÚBLICAS (Accesibles sin login)
                        // Añadimos aquí todas las páginas que cualquiera puede ver (Inicio, Registro, Detalles, Categorías...)
                        .requestMatchers(
                                "/",
                                "/login",
                                "/registro",
                                "/guardar-usuario",
                                "/videojuego/**",   // Ver detalle de un juego
                                "/categorias/**",   // Ver listado por categorías
                                "/api/videojuegos/**" // Tu API (si la usas externamente)
                        ).permitAll()

                        // 3. CONSOLA DE BASE DE DATOS H2
                        .requestMatchers("/h2-console/**").permitAll()

                        // 4. ZONA DE ADMINISTRACIÓN (¡SOLO ADMIN!) 🛡️
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")

                        // 5. ZONA DE USUARIOS REGISTRADOS
                        .anyRequest().authenticated()
                )

                // --- C. FORMULARIO DE LOGIN ---
                .formLogin(login -> login
                        .loginPage("/login")        // Nuestra vista personalizada
                        .defaultSuccessUrl("/", true) // A dónde ir si el login es correcto
                        .permitAll()
                )

                // --- D. CIERRE DE SESIÓN ---
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")      // Al salir, volvemos al inicio
                        .permitAll()
                );

        return http.build();
    }

    // ==========================================
    // 2. PROVEEDOR DE AUTENTICACIÓN
    // ==========================================
    /**
     * Conecta Spring Security con tu base de datos (a través de UserDetailsService)
     * y define cómo comprobar las contraseñas.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService); // Quién busca al usuario
        provider.setPasswordEncoder(passwordEncoder());     // Quién comprueba la clave
        return provider;
    }

    // ==========================================
    // 3. ENCRIPTADOR DE CONTRASEÑAS
    // ==========================================
    /**
     * Define el algoritmo de encriptación (BCrypt es el estándar actual).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}