package com.borisbaldominos.proyectofinal.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Filtro de Rate Limiting para Login
 * -----------------------------------
 * Previene ataques de brute force limitando los intentos de login por IP.
 *
 * Configuración:
 * - MAX_ATTEMPTS: Máximo de intentos fallidos permitidos
 * - LOCK_TIME_MS: Tiempo de bloqueo en milisegundos (5 minutos)
 */
@Component
public class LoginAttemptFilter extends OncePerRequestFilter {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_TIME_MS = 5 * 60 * 1000; // 5 minutos

    // Almacena intentos fallidos por IP: IP -> (contador, timestamp ultimo intento)
    private final Map<String, LoginAttemptCache> attemptCache = new ConcurrentHashMap<>();

    private static class LoginAttemptCache {
        int attempts;
        Instant lockUntil;

        LoginAttemptCache(int attempts, Instant lockUntil) {
            this.attempts = attempts;
            this.lockUntil = lockUntil;
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String ip = getClientIp(request);

        // Solo aplicar rate limiting al endpoint de login
        if ("/login".equals(request.getRequestURI()) && "POST".equals(request.getMethod())) {

            LoginAttemptCache cache = attemptCache.get(ip);

            if (cache != null && cache.lockUntil != null && Instant.now().isBefore(cache.lockUntil)) {
                // IP está bloqueada
                long remainingSeconds = java.time.Duration.between(Instant.now(), cache.lockUntil).getSeconds();
                response.setStatus(429); // HTTP 429 Too Many Requests
                response.setContentType("application/json");
                response.getWriter().write(
                    "{\"error\": \"Demasiados intentos. Intente de nuevo en " + remainingSeconds + " segundos\"}"
                );
                return;
            }

            // Envolver response para capturar errores de autenticación
            LoginResponseWrapper responseWrapper = new LoginResponseWrapper(response);
            filterChain.doFilter(request, responseWrapper);

            // Verificar si el login falló (status 401 o 302 con error)
            int status = responseWrapper.getStatus();
            boolean loginFailed = status == HttpServletResponse.SC_UNAUTHORIZED ||
                    (status == HttpServletResponse.SC_FOUND &&
                     responseWrapper.getHeader("Location") != null &&
                     responseWrapper.getHeader("Location").contains("error"));

            if (loginFailed) {
                handleFailedAttempt(ip);
            } else {
                // Login exitoso - resetear contador
                attemptCache.remove(ip);
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private void handleFailedAttempt(String ip) {
        LoginAttemptCache cache = attemptCache.get(ip);
        int newAttempts = (cache != null) ? cache.attempts + 1 : 1;

        if (newAttempts >= MAX_ATTEMPTS) {
            // Bloquear IP
            attemptCache.put(ip, new LoginAttemptCache(newAttempts, Instant.now().plusMillis(LOCK_TIME_MS)));
            logger.warn("🚫 IP bloqueada por exceso de intentos de login: " + ip);
        } else {
            attemptCache.put(ip, new LoginAttemptCache(newAttempts, null));
            logger.info("Intento fallido de login desde IP: " + ip + " (intentos: " + newAttempts + "/" + MAX_ATTEMPTS + ")");
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Limpia el cache periódicamente (puede llamarse desde un scheduler)
     */
    public void cleanupCache() {
        Instant now = Instant.now();
        attemptCache.entrySet().removeIf(entry ->
            entry.getValue().lockUntil != null && now.isAfter(entry.getValue().lockUntil)
        );
    }
}
